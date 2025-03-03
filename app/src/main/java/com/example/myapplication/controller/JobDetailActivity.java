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
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.EventSlot;
import com.example.myapplication.model.Job;
import com.example.myapplication.view.adapter.EventListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";
    private Job job;
    private RecyclerView eventRecyclerView;
    private EventListAdapter eventListAdapter;
    private FloatingActionButton fabAddButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                EditText etName = dialogView.findViewById(R.id.editEventName);
                EditText etBeginDate = dialogView.findViewById(R.id.editBeginDate);
                EditText etBeginTime = dialogView.findViewById(R.id.editBeginTime);
                EditText etEndDate = dialogView.findViewById(R.id.editEndDate);
                EditText etEndTime = dialogView.findViewById(R.id.editEndTime);

                String name = etName.getText().toString().trim();
                String beginDate = etBeginDate.getText().toString().trim();
                String endDate = etEndDate.getText().toString().trim();
                String beginTime = etBeginTime.getText().toString().trim();
                String endTime = etEndTime.getText().toString().trim();

                // Minimal validation (e.g., date and time fields required).
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Name is required");
                    return;
                }
                if (TextUtils.isEmpty(beginDate)) {
                    etBeginDate.setError("Date is required");
                    return;
                }
                if (TextUtils.isEmpty(endDate)) {
                    etEndDate.setError("Date is required");
                    return;
                }
                if (TextUtils.isEmpty(beginTime)) {
                    etBeginTime.setError("Start time is required");
                    return;
                }
                if (TextUtils.isEmpty(endTime)) {
                    etEndTime.setError("End time is required");
                    return;
                }

                // Create a new Shift.
                CalendarEvent newEvent = new CalendarEvent(name, 0, beginDate, endDate);

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
}
