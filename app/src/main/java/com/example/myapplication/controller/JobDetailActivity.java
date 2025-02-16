package com.example.myapplication.controller;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.Job;

public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Retrieve the Job object passed in the intent.
        Job job = getIntent().getSerializableExtra(EXTRA_JOB, Job.class);

        // Get references to the UI elements.
        TextView tvTitle = findViewById(R.id.detailJobTitle);
        TextView tvSubtitle = findViewById(R.id.detailJobSubtitle);
        TextView tvEmployer = findViewById(R.id.detailJobEmployer);
        TextView tvLocation = findViewById(R.id.detailJobLocation);
        TextView tvColor = findViewById(R.id.detailJobColor);

        // Populate the UI with job details.
        if (job != null) {
            tvTitle.setText(job.getTitle());
            tvSubtitle.setText(job.getSubTitle());
            tvEmployer.setText("Employer: " + job.getEmployer());
            tvLocation.setText("Location: " + job.getLocation());
            String colorHex = String.format("#%06X", (0xFFFFFF & job.getColor()));
            tvColor.setText("Color: " + colorHex);
            tvColor.setTextColor(job.getColor());
        }
    }
}
