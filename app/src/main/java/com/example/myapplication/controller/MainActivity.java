package com.example.myapplication.controller;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.NotificationSender;
import com.example.myapplication.view.adapter.JobListAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private ExtendedFloatingActionButton fabAddJob;
    private List<Job> jobs;
    private String currentUserId;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        setupRecyclerView();
        setupBottomNavigation();
        createNotificationChannels();
    }

    private void initialize() {
        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        // If no user is logged in, redirect to login
        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // Initialize views
        topAppBar = findViewById(R.id.topAppBar);
        jobRecyclerView = findViewById(R.id.jobRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        fabAddJob = findViewById(R.id.fabAddJob);
        fabAddJob.setOnClickListener(v -> showAddJobDialog());

        // Set click listener on user avatar
        ImageView userAvatar = findViewById(R.id.userAvatar);
        userAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, JobSummaryActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        // Set up the RecyclerView.
        jobRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobs = new ArrayList<>();

        // Get the list of jobs for current user.
        db.collection("Jobs")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
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
        jobListAdapter = new JobListAdapter(jobs, this);
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
            } else if (itemId == R.id.nav_sharing) {
                Intent intent = new Intent(MainActivity.this, SharingMainActivity.class);
                startActivity(intent);
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

        // Create the Material Alert Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
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
        // Set the user ID for the job
        newJob.setUserId(currentUserId);

        // Use a transaction to generate a counter-based job ID
        db.runTransaction(transaction -> {
            DocumentSnapshot counterDoc = transaction.get(db.collection("counters").document("jobs"));

            int jobCounterId;
            if (counterDoc.exists()) {
                jobCounterId = counterDoc.getLong("nextId").intValue();
                transaction.update(db.collection("counters").document("jobs"), "nextId", jobCounterId + 1);
            } else {
                // First job, initialize counter
                jobCounterId = 1;
                transaction.set(db.collection("counters").document("jobs"),
                        java.util.Collections.singletonMap("nextId", 2));
            }

            String jobId = "job_" + jobCounterId;
            newJob.setJobId(jobId);

            // Save the job with its ID
            transaction.set(db.collection("Jobs").document(jobId), newJob);

            return jobId;
        }).addOnSuccessListener(jobId -> {
            // Add the job to the local list
            jobs.add(newJob);
            jobListAdapter.notifyItemInserted(jobs.size() - 1);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding job to Firestore", e);
        });
    }
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            // Create a notification channel for notifications that get sent every shift
            NotificationChannel dailyChannel = new NotificationChannel(NotificationSender.daily_channel_name,
                    "dailyNotif",
                    importance);
            dailyChannel.setDescription(NotificationSender.daily_channel_desc);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(dailyChannel);

            // Create a notification channel for notifications that get sent every Sunday
            NotificationChannel weeklyChannel = new NotificationChannel(NotificationSender.weekly_channel_name,
                    "weeklyNotif",
                    importance);
            dailyChannel.setDescription(NotificationSender.weekly_channel_desc);
            manager.createNotificationChannel(weeklyChannel);

            NotificationChannel alarmChannel = new NotificationChannel(NotificationSender.alarm_channel,
                    "alarmChannel",
                    importance);
            alarmChannel.setDescription(NotificationSender.alarm_channel_desc);
            manager.createNotificationChannel(alarmChannel);
        }

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
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