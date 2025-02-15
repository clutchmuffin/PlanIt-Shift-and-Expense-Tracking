package com.example.myapplication.controller;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.view.adapter.JobListAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private RecyclerView jobRecyclerView;
    private JobListAdapter jobListAdapter;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the top app bar.
        topAppBar = findViewById(R.id.topAppBar);

        // Set up the RecyclerView.
        jobRecyclerView = findViewById(R.id.jobRecyclerView);
        jobRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create some dummy job data.
        List<Job> dummyJobs = new ArrayList<>();
        dummyJobs.add(new Job("Job A", "Subhead A", "Employer A", "Location A", Color.parseColor("#BB86FC")));
        dummyJobs.add(new Job("Job B", "Subhead B", "Employer B", "Location B", Color.parseColor("#6200EE")));
        dummyJobs.add(new Job("Job C", "Subhead C", "Employer C", "Location C", Color.parseColor("#3700B3")));

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
    }
}

