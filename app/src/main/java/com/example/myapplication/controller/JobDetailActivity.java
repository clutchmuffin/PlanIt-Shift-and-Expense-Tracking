package com.example.myapplication.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.Shift;
import com.example.myapplication.view.adapter.ShiftListAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";
    private Job job;
    private RecyclerView shiftRecyclerView;
    private ShiftListAdapter shiftListAdapter;
    private FloatingActionButton fabAddShift;

    private LocalDate selectedDate;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

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

        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnSelectStartTime = dialogView.findViewById(R.id.btnSelectStartTime);
        Button btnSelectEndTime = dialogView.findViewById(R.id.btnSelectEndTime);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        TextView tvSelectedStartTime = dialogView.findViewById(R.id.tvSelectedStartTime);
        TextView tvSelectedEndTime = dialogView.findViewById(R.id.tvSelectedEndTime);

        btnSelectDate.setOnClickListener(v -> showDatePicker(tvSelectedDate));
        btnSelectStartTime.setOnClickListener(v -> showTimePicker(tvSelectedStartTime, true));
        btnSelectEndTime.setOnClickListener(v -> showTimePicker(tvSelectedEndTime, false));

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (selectedDate == null) {
                    tvSelectedDate.setError("Select a date");
                    return;
                }
                if (selectedStartTime == null) {
                    tvSelectedStartTime.setError("Select a start time");
                    return;
                }
                if (selectedEndTime == null) {
                    tvSelectedEndTime.setError("Select an end time");
                    return;
                }

                // Create a new Shift object
                Shift newShift = new Shift(selectedDate, selectedStartTime, selectedEndTime);
                job.addShift(newShift);

                // Notify adapter
                shiftListAdapter.notifyItemInserted(job.getShifts().size() - 1);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showDatePicker(TextView tvSelectedDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            tvSelectedDate.setText(selectedDate.format(DATE_FORMATTER));
        });
    }

    private void showTimePicker(TextView tvSelectedTime, boolean isStartTime) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTitleText(isStartTime ? "Select Start Time" : "Select End Time")
                .build();

        timePicker.show(getSupportFragmentManager(), isStartTime ? "START_TIME_PICKER" : "END_TIME_PICKER");
        timePicker.addOnPositiveButtonClickListener(v -> {
            LocalTime selectedTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            if (isStartTime) {
                selectedStartTime = selectedTime;
            } else {
                selectedEndTime = selectedTime;
            }
            tvSelectedTime.setText(selectedTime.format(TIME_FORMATTER));
        });
    }
}
