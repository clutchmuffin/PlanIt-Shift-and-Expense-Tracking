package com.example.myapplication.controller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.view.adapter.JobListAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String DEFAULT_COLOR = "#6200EE";

    private MaterialToolbar topAppBar;
    private RecyclerView jobRecyclerView;
    private JobListAdapter jobListAdapter;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAddJob;
    private List<Job> jobs;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        setupRecyclerView();
        setupBottomNavigation();
    }

    private void initialize() {
        topAppBar = findViewById(R.id.topAppBar);
        jobRecyclerView = findViewById(R.id.jobRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        fabAddJob = findViewById(R.id.fabAddJob);
        fabAddJob.setOnClickListener(v -> showAddJobDialog());
    }

    private void setupRecyclerView() {

        // Set up the RecyclerView.
        jobRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobs = new ArrayList<>();

        // Get the list of jobs from Firestore.
        db.collection("Jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                jobs.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    jobs.add(job);
                }
                jobListAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }
        });

        // Set the adapter.
        jobListAdapter = new JobListAdapter(jobs);
        jobRecyclerView.setAdapter(jobListAdapter);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_jobs) {
                return true; // Already displaying the job list
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (itemId == R.id.nav_budget) {
                startActivity(new Intent(MainActivity.this, BudgetMainActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showAddJobDialog() {
        AlertDialog dialog = createAddJobDialog();
        dialog.show();
    }

    private AlertDialog createAddJobDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_job, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Job")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateAndAddJob(dialogView)) {
                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }

    private boolean validateAndAddJob(View dialogView) {
        // Extract values from dialog
        JobFormData formData = extractJobFormData(dialogView);

        // Validate required fields
        if (TextUtils.isEmpty(formData.title)) {
            ((EditText) dialogView.findViewById(R.id.editJobTitle)).setError("Title is required");
            return false;
        }

        // Parse and validate color
        int colorValue;
        try {
            colorValue = TextUtils.isEmpty(formData.colorInput)
                    ? Color.parseColor(DEFAULT_COLOR)
                    : Color.parseColor(formData.colorInput);
        } catch (IllegalArgumentException e) {
            ((EditText) dialogView.findViewById(R.id.editJobColor)).setError("Invalid color code");
            return false;
        }

        // Create and save job
        Job newJob = new Job(formData.title, formData.subtitle, formData.employer, formData.location, colorValue);

        // Parse and Validate Pay rate
        if (!TextUtils.isEmpty(formData.payRate)) {
            try {
                newJob.setPayRate(Integer.parseInt(formData.payRate));
            } catch (NumberFormatException e) {
                // Use default pay rate if parsing fails
                newJob.setPayRate(0);
            }
        }

        saveJobToFirestore(newJob);

        return true;
    }

    private JobFormData extractJobFormData(View dialogView) {
        EditText etTitle = dialogView.findViewById(R.id.editJobTitle);
        EditText etSubtitle = dialogView.findViewById(R.id.editJobSubtitle);
        EditText etEmployer = dialogView.findViewById(R.id.editJobEmployer);
        EditText etLocation = dialogView.findViewById(R.id.editJobLocation);
        EditText etColor = dialogView.findViewById(R.id.editJobColor);
        EditText etPay = dialogView.findViewById(R.id.editPayRate);

        JobFormData data = new JobFormData();
        data.title = etTitle.getText().toString().trim();
        data.subtitle = etSubtitle.getText().toString().trim();
        data.employer = etEmployer.getText().toString().trim();
        data.location = etLocation.getText().toString().trim();
        data.colorInput = etColor.getText().toString().trim();
        data.payRate = etPay.getText().toString().trim();

        return data;
    }

    private void saveJobToFirestore(Job newJob) {
        // Add the job to the local list
        jobs.add(newJob);

        // Save to Firestore
        db.collection("Jobs").document(newJob.getTitle()).set(newJob)
                .addOnSuccessListener(aVoid -> {
                    // Notify the adapter that a new item was inserted
                    jobListAdapter.notifyItemInserted(jobs.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding job to Firestore", e);
                    // Remove from local list if Firestore save fails
                    jobs.remove(newJob);
                });
    }

    // Helper class to hold form data
    private static class JobFormData {
        String title;
        String subtitle;
        String employer;
        String location;
        String colorInput;
        String payRate;
    }
}