package com.example.myapplication.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Expense;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.Notification;
import com.example.myapplication.model.RepeatType;
import com.example.myapplication.view.adapter.EventListAdapter;
import com.example.myapplication.view.adapter.ExpenseListAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";
    private Job job;
    private RecyclerView eventRecyclerView;
    private RecyclerView expenseRecyclerView;
    private EventListAdapter eventListAdapter;
    private ExpenseListAdapter expenseListAdapter;
    private FloatingActionButton fabAddButton;
    private FloatingActionButton fabAddExpense;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;
    private Notification notif;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        fabAddButton = findViewById(R.id.fabAdd);
        fabAddExpense = findViewById(R.id.fabAddExp);

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

        // Set up the  Event RecyclerView.
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Get the list of events from Firestore.
        db.collection("Jobs").document(job.getTitle()).collection("Events")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CalendarEvent event = document.toObject(CalendarEvent.class);
                            job.addEvent(event);
                        }
                        eventListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("JobDetailActivity", "Error getting documents: ", task.getException());
                    }
                });
        eventListAdapter = new EventListAdapter(job.getEvents());
        eventRecyclerView.setAdapter(eventListAdapter);

        // Set up the Expenses RecyclerViews
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        db.collection("Jobs").document(job.getTitle()).collection("EXP")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Expense expense = document.toObject(Expense.class);
                            job.addExpense(expense);
                        }
                        expenseListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("JobDetailActivity", "Error getting documents: ", task.getException());
                    }
                });
        expenseListAdapter = new ExpenseListAdapter(job.getExpenses());
        expenseRecyclerView.setAdapter(expenseListAdapter);

        // Set up the FAB to add a new shift.
        fabAddButton.setOnClickListener(v -> showAddShiftDialog());
        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
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
        RadioGroup radioGroupRepeatType = dialogView.findViewById(R.id.radioGroupRepeatType);

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
                if (beginTime == null) {
                    tvSelectedStartTime.setError("Select a start time");
                    return;
                }
                if (endTime == null) {
                    tvSelectedEndTime.setError("Select an end time");
                    return;
                }

                // Determine the selected repeat type
                RepeatType repeatType = RepeatType.NEVER; // Default value
                int selectedRadioButtonId = radioGroupRepeatType.getCheckedRadioButtonId();

                if (selectedRadioButtonId == R.id.radioNeverRepeat) {
                    repeatType = RepeatType.NEVER;
                } else if (selectedRadioButtonId == R.id.radioDaily) {
                    repeatType = RepeatType.DAILY;
                } else if (selectedRadioButtonId == R.id.radioWeekly) {
                    repeatType = RepeatType.WEEKLY;
                } else if (selectedRadioButtonId == R.id.radioMonthly) {
                    repeatType = RepeatType.MONTHLY;
                } else if (selectedRadioButtonId == R.id.radioYearly) {
                    repeatType = RepeatType.ANNUALLY;
                }

                // Create a new Shift.
                CalendarEvent newEvent = new CalendarEvent(name,
                                                            0,
                                                            job.getPayRate(),
                                                            beginDate.format(DATE_FORMATTER),
                                                            endDate.format(DATE_FORMATTER),
                                                            beginTime.format(TIME_FORMATTER),
                                                            endTime.format(TIME_FORMATTER),
                                                            repeatType);

                // Check for conflicts across all jobs, and only add if no conflict.
                db.collectionGroup("Events").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasConflict = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CalendarEvent otherEvent = document.toObject(CalendarEvent.class);
                            Log.i("JobDetailActivity", "Checking for overlap with " + otherEvent.getName());
                            if (hasConflict(newEvent, otherEvent)) {
                                hasConflict = true;
                                break;
                            }
                        }

                        if (hasConflict) {
                            // Show conflict alert
                            new AlertDialog.Builder(this)
                                    .setTitle("Scheduling Conflict")
                                    .setMessage("This shift overlaps with an existing shift")
                                    .setPositiveButton("Edit", (d, which) -> d.dismiss())
                                    .show();
                        } else {
                            // No conflict - save the event
                            db.collection("Jobs").document(job.getTitle())
                                    .collection("Events")
                                    .document(newEvent.getName())
                                    .set(newEvent);

                            job.addEvent(newEvent);
                            eventListAdapter.notifyItemInserted(job.getEvents().size() - 1);
                            dialog.dismiss();
                            notif = new Notification(this);
                            notif.addDailyNotification(newEvent, this);
                        }
                    }
                });
            });
        });
        dialog.show();
    }

    private void showAddExpenseDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Expense")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        EditText etDescription = dialogView.findViewById(R.id.editExpenseDescription);
        EditText etAmount = dialogView.findViewById(R.id.editExpenseAmount);

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                String description = etDescription.getText().toString().trim();
                if (TextUtils.isEmpty(description)) {
                    etDescription.setError("Description is required");
                    return;
                }

                String amount = etAmount.getText().toString().trim();
                if (TextUtils.isEmpty(amount)) {
                    etAmount.setError("Amount is required");
                    return;
                }

                // Create a new Expense.
                Expense newExpense = new Expense(description, Integer.parseInt(amount));

                // Add the expense to the job.
                db.collection("Jobs").document(job.getTitle()).collection("EXP").document().set(newExpense);
                job.addExpense(newExpense);

                // Notify the adapter to update the RecyclerView.
                expenseListAdapter.notifyItemInserted(job.getExpenses().size() - 1);
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
                    .atZone(ZoneId.of("UTC"))
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
                beginTime = selectedTime;
            } else {
                endTime = selectedTime;
            }
            tvTime.setText(selectedTime.format(TIME_FORMATTER));
        });
    }

    private boolean hasConflict(CalendarEvent newEvent, CalendarEvent existingEvent) {
        // Convert new event date and time into LocalDateTime objects.
        LocalDateTime newStart = LocalDateTime.of(
                LocalDate.parse(newEvent.getBegin_date(), DATE_FORMATTER),
                LocalTime.parse(newEvent.getBegin_time(), TIME_FORMATTER)
        );
        LocalDateTime newEnd = LocalDateTime.of(
                LocalDate.parse(newEvent.getEnd_date(), DATE_FORMATTER),
                LocalTime.parse(newEvent.getEnd_time(), TIME_FORMATTER)
        );

        // Convert existing event date and time into LocalDateTime objects.
        LocalDateTime existingStart = LocalDateTime.of(
                LocalDate.parse(existingEvent.getBegin_date(), DATE_FORMATTER),
                LocalTime.parse(existingEvent.getBegin_time(), TIME_FORMATTER)
        );
        LocalDateTime existingEnd = LocalDateTime.of(
                LocalDate.parse(existingEvent.getEnd_date(), DATE_FORMATTER),
                LocalTime.parse(existingEvent.getEnd_time(), TIME_FORMATTER)
        );

        // Validate that event intervals are valid.
        if (!newEnd.isAfter(newStart) || !existingEnd.isAfter(existingStart)) {
            throw new IllegalArgumentException("Event end time must be after start time.");
        }

        // Check for overlap: an overlap exists if new event starts before the existing event ends
        // and new event ends after the existing event starts.
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }


}
