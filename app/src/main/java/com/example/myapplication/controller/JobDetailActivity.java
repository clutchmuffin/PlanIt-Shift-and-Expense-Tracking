package com.example.myapplication.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.Shift;
import com.example.myapplication.view.adapter.ShiftListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";
    private Job job;
    private RecyclerView shiftRecyclerView;
    private ShiftListAdapter shiftListAdapter;
    private FloatingActionButton fabAddShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Retrieve the Job object passed in the intent.
        job = getIntent().getSerializableExtra(EXTRA_JOB, Job.class);

        // Get references to the UI elements.
        TextView tvTitle = findViewById(R.id.detailJobTitle);
        TextView tvSubtitle = findViewById(R.id.detailJobSubtitle);
        TextView tvEmployer = findViewById(R.id.detailJobEmployer);
        TextView tvLocation = findViewById(R.id.detailJobLocation);
        TextView tvPayRate = findViewById(R.id.detailJobPayRate);
        TextView tvColor = findViewById(R.id.detailJobColor);
        shiftRecyclerView = findViewById(R.id.shiftRecyclerView);
        fabAddShift = findViewById(R.id.fabAddShift);

        // Populate the UI with job details.
        if (job != null) {
            tvTitle.setText(job.getTitle());
            tvSubtitle.setText(job.getSubTitle());
            tvEmployer.setText("Employer: " + job.getEmployer());
            tvLocation.setText("Location: " + job.getLocation());
            String colorHex = String.format("#%06X", (0xFFFFFF & job.getColor()));
            tvColor.setText("Color: " + colorHex);
            tvColor.setTextColor(job.getColor());
            tvPayRate.setText("$" + job.getPayRate());
        }

        // Set up the RecyclerView.
        shiftRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        shiftListAdapter = new ShiftListAdapter(job.getShifts());
        shiftRecyclerView.setAdapter(shiftListAdapter);

        // Set up the FAB to add a new shift.
        fabAddShift.setOnClickListener(v -> showAddShiftDialog());
    }

    private void showAddShiftDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_shift, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Shift")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                EditText etDate = dialogView.findViewById(R.id.editShiftDate);
                EditText etStartTime = dialogView.findViewById(R.id.editShiftStartTime);
                EditText etEndTime = dialogView.findViewById(R.id.editShiftEndTime);

                String date = etDate.getText().toString().trim();
                String startTime = etStartTime.getText().toString().trim();
                String endTime = etEndTime.getText().toString().trim();

                // Minimal validation (e.g., date and time fields required).
                if (TextUtils.isEmpty(date)) {
                    etDate.setError("Date is required");
                    return;
                }
                if (TextUtils.isEmpty(startTime)) {
                    etStartTime.setError("Start time is required");
                    return;
                }
                if (TextUtils.isEmpty(endTime)) {
                    etEndTime.setError("End time is required");
                    return;
                }

                // Create a new Shift.
                Shift newShift = new Shift(date, startTime, endTime);

                // Add the shift to the job.
                job.addShift(newShift);

                // Notify the adapter to update the RecyclerView.
                shiftListAdapter.notifyItemInserted(job.getShifts().size() - 1);
                dialog.dismiss();
            });
        });
        dialog.show();
    }
}
