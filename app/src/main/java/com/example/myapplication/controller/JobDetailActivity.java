package com.example.myapplication.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.Shift;
import com.example.myapplication.view.adapter.EventListAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.google.firebase.firestore.FirebaseFirestore;

public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";
    private Job job;
    private RecyclerView eventRecyclerView;
    private EventListAdapter eventListAdapter;
    private FloatingActionButton fabAddButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LocalDate beginDate;
    private LocalDate endDate;
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
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        fabAddButton = findViewById(R.id.fabAdd);

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
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventListAdapter = new EventListAdapter(job.getEvents());
        eventRecyclerView.setAdapter(eventListAdapter);

        // Set up the FAB to add a new shift.
        fabAddButton.setOnClickListener(v -> showAddShiftDialog());
    }

    private void showAddShiftDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Shift")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        EditText etName = dialogView.findViewById(R.id.editEventName);
        Button btnSelectBeginDate = dialogView.findViewById(R.id.btnSelectBeginDate);
        Button btnSelectEndDate = dialogView.findViewById(R.id.btnSelectEndDate);
        Button btnSelectStartTime = dialogView.findViewById(R.id.btnSelectStartTime);
        Button btnSelectEndTime = dialogView.findViewById(R.id.btnSelectEndTime);
        TextView tvBeginDate = dialogView.findViewById(R.id.tvBeginDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);
        TextView tvSelectedStartTime = dialogView.findViewById(R.id.tvSelectedStartTime);
        TextView tvSelectedEndTime = dialogView.findViewById(R.id.tvSelectedEndTime);

        btnSelectBeginDate.setOnClickListener(v -> showDatePicker(tvBeginDate, true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(tvEndDate, false));
        btnSelectStartTime.setOnClickListener(v -> showTimePicker(tvSelectedStartTime, true));
        btnSelectEndTime.setOnClickListener(v -> showTimePicker(tvSelectedEndTime, false));

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                String name = etName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Name is required");
                    return;
                }

                if (beginDate == null) {
                    tvEndDate.setError("Select a date");
                    return;
                }
                if (endDate == null) {
                    tvEndDate.setError("Select a date");
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

                // Create a new Shift.
                CalendarEvent newEvent = new CalendarEvent(name, 0, beginDate, endDate, selectedStartTime, selectedEndTime);

                // Add the shift to the job.
                db.collection("Jobs").document(job.getTitle()).collection("Events").document(newEvent.getName()).set(newEvent);
                job.addEvent(newEvent);

                // Notify the adapter to update the RecyclerView.
                eventListAdapter.notifyItemInserted(job.getEvents().size() - 1);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showDatePicker(TextView tvDate, boolean isBeginDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.show(getSupportFragmentManager(), isBeginDate ? "BEGIN_DATE_PICKER" : "END_DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (isBeginDate) {
                beginDate = selectedDate;
            } else {
                endDate = selectedDate;
            }
            tvDate.setText(selectedDate.format(DATE_FORMATTER));
        });
    }


    private void showTimePicker(TextView tvTime, boolean isStartTime) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
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
            tvTime.setText(selectedTime.format(TIME_FORMATTER));
        });
    }

}
