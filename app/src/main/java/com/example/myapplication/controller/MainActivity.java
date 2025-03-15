package com.example.myapplication.controller;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.Notification;
import com.example.myapplication.model.NotificationReceiver;
import com.example.myapplication.view.adapter.JobListAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private RecyclerView jobRecyclerView;
    private JobListAdapter jobListAdapter;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAddJob;
    private List<Job> jobs;

    private NotificationReceiver reciever = new NotificationReceiver();

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createNotificationChannel();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views.
        topAppBar = findViewById(R.id.topAppBar);
        jobRecyclerView = findViewById(R.id.jobRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        fabAddJob = findViewById(R.id.fabAddJob);

        // Set up the RecyclerView.
        jobRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a local list jobs.
        jobs = new ArrayList<>();

        // Get the list of jobs from Firestore. TODO: Implement this.
        db.collection("Jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    jobs.add(job);
                }
                jobListAdapter.notifyDataSetChanged();
            } else {
                Log.e("MainActivity", "Error getting documents: ", task.getException());
            }
        });

        // Set the adapter.
        jobListAdapter = new JobListAdapter(jobs);
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
            } else if (itemId == R.id.nav_budget) {
                //Go to Budget Feature
                Intent intent = new Intent(MainActivity.this, BudgetMainActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Set up Floating Action Button to add a new job.
        fabAddJob.setOnClickListener(v -> showAddJobDialog());
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Notification.channel_name,
                    "dailyNotif",
                    importance);
            channel.setDescription(Notification.channel_desc);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
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
                EditText etPay = dialogView.findViewById(R.id.editPayRate);

                String title = etTitle.getText().toString().trim();
                String subtitle = etSubtitle.getText().toString().trim();
                String employer = etEmployer.getText().toString().trim();
                String location = etLocation.getText().toString().trim();
                String colorInput = etColor.getText().toString().trim();
                String pay_rate = etPay.getText().toString().trim();

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
                newJob.setPayRate(Integer.parseInt(pay_rate));

                // Add the new job to the list.
                jobs.add(newJob);
                db.collection("Jobs").document(newJob.getTitle()).set(newJob);

                // Notify the adapter that a new item was inserted.
                jobListAdapter.notifyItemInserted(jobs.size() - 1);

                // Dismiss the dialog.
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}

