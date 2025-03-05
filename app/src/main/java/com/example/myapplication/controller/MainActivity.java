package com.example.myapplication.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private RecyclerView jobRecyclerView;
    private JobListAdapter jobListAdapter;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAddJob;
    List<Job> dummyJobs;

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views.
        topAppBar = findViewById(R.id.topAppBar);
        jobRecyclerView = findViewById(R.id.jobRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        fabAddJob = findViewById(R.id.fabAddJob);

        // Set up the RecyclerView.
        jobRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create some dummy job data.
        dummyJobs = new ArrayList<>();
        dummyJobs.add(new Job("Job A", "Subhead A", "Employer A", "Location A", Color.parseColor("#BB86FC")));
        dummyJobs.add(new Job("Job B", "Subhead B", "Employer B", "Location B", Color.parseColor("#6200EE")));
        dummyJobs.add(new Job("Job C", "Subhead C", "Employer C", "Location C", Color.parseColor("#3700B3")));

        db.collection("Jobs").document("Job A").set(dummyJobs.get(0));
        // Set the adapter.
        jobListAdapter = new JobListAdapter(dummyJobs);
        jobRecyclerView.setAdapter(jobListAdapter);

        // Set up the bottom navigation.
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_jobs) {
                // Already displaying the job list.
                return true;
            } else if (itemId == R.id.nav_calendar) {
                // Calendar view not implemented yet.
                return true;
            }
            return false;
        });

        // Set up Floating Action Button to add a new job.
        fabAddJob.setOnClickListener(v -> showAddJobDialog());
    }

    private void showAddJobDialog() {
        // Inflate the custom dialog view.
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_job, null);

        // Create an AlertDialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Job")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        // Set a click listener for the positive button after the dialog is shown
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                // Retrieve data from the dialog's EditText fields.
                EditText etTitle = dialogView.findViewById(R.id.editJobTitle);
                EditText etSubtitle = dialogView.findViewById(R.id.editJobSubtitle);
                EditText etEmployer = dialogView.findViewById(R.id.editJobEmployer);
                EditText etLocation = dialogView.findViewById(R.id.editJobLocation);
                EditText etColor = dialogView.findViewById(R.id.editJobColor);

                String title = etTitle.getText().toString().trim();
                String subtitle = etSubtitle.getText().toString().trim();
                String employer = etEmployer.getText().toString().trim();
                String location = etLocation.getText().toString().trim();
                String colorInput = etColor.getText().toString().trim();

                // Validate required fields (at minimum, title).
                if (TextUtils.isEmpty(title)) {
                    etTitle.setError("Title is required");
                    return;
                }

                // If color is not provided, use a default color.
                int colorValue;
                try {
                    colorValue = !TextUtils.isEmpty(colorInput)
                            ? Color.parseColor(colorInput)
                            : Color.parseColor("#6200EE");
                } catch (IllegalArgumentException e) {
                    etColor.setError("Invalid color code");
                    return;
                }

                // Create a new Job object.
                Job newJob = new Job(title, subtitle, employer, location, colorValue);

                // Add the new job to the list.
                dummyJobs.add(newJob);
                db.collection("Jobs").document(newJob.getTitle()).set(newJob);

                // Notify the adapter that a new item was inserted.
                jobListAdapter.notifyItemInserted(dummyJobs.size() - 1);

                // Dismiss the dialog.
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}

