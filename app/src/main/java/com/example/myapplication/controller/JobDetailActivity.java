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
    private static final String TAG = "JobDetailActivity";
    public static final String EXTRA_JOB = "com.example.myapplication.JOB";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Job job;
    private RecyclerView eventRecyclerView;
    private RecyclerView expenseRecyclerView;
    private EventListAdapter eventListAdapter;
    private ExpenseListAdapter expenseListAdapter;
    private FloatingActionButton fabAddButton;
    private FloatingActionButton fabAddExpense;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        job = getIntent().getSerializableExtra(EXTRA_JOB, Job.class);

        initializeViews();
        populateJobDetails();
        setupEventRecyclerView();
        setupExpenseRecyclerView();
        setupActionButtons();
    }

    private void initializeViews() {
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        fabAddButton = findViewById(R.id.fabAdd);
        fabAddExpense = findViewById(R.id.fabAddExp);
    }

    private void populateJobDetails() {
        if (job == null) {
            return;
        }

        TextView tvTitle = findViewById(R.id.detailJobTitle);
        TextView tvSubtitle = findViewById(R.id.detailJobSubtitle);
        TextView tvEmployer = findViewById(R.id.detailJobEmployer);
        TextView tvLocation = findViewById(R.id.detailJobLocation);
        TextView tvPayRate = findViewById(R.id.detailJobPayRate);
        TextView tvColor = findViewById(R.id.detailJobColor);

        tvTitle.setText(job.getTitle());
        tvSubtitle.setText(job.getSubTitle());
        tvEmployer.setText("Employer: " + job.getEmployer());
        tvLocation.setText("Location: " + job.getLocation());
        String colorHex = String.format("#%06X", (0xFFFFFF & job.getColor()));
        tvColor.setText("Color: " + colorHex);
        tvColor.setTextColor(job.getColor());
        tvPayRate.setText("$" + job.getPayRate());
    }

    private void setupEventRecyclerView() {

        // Set up the RecyclerView.
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
                        Log.e(TAG, "Error getting events: ", task.getException());
                    }
                });

        // Set the adapter.
        eventListAdapter = new EventListAdapter(job.getEvents());
        eventRecyclerView.setAdapter(eventListAdapter);
    }


    private void setupExpenseRecyclerView() {

        // Set up the RecyclerView.
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the list of expenses from Firestore.
        db.collection("Jobs").document(job.getTitle()).collection("EXP")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Expense expense = document.toObject(Expense.class);
                            job.addExpense(expense);
                        }
                        expenseListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting expenses: ", task.getException());
                    }
                });

        // Set the adapter.
        expenseListAdapter = new ExpenseListAdapter(job.getExpenses());
        expenseRecyclerView.setAdapter(expenseListAdapter);
    }

    private void setupActionButtons() {
        fabAddButton.setOnClickListener(v -> showAddEventDialog());
        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }


    /** EVENTS **/
    private void showAddEventDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
        AlertDialog dialog = createEventDialog(dialogView);
        dialog.show();
    }

    private AlertDialog createEventDialog(View dialogView) {

        // Create an AlertDialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Shift")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        // Find and set up the dialog controls.
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

        // Dialog Button Listeners
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateEventInput(dialogView)) {
                    createAndSaveEvent(dialogView, dialog);
                }
            });
        });

        return dialog;
    }

    private boolean validateEventInput(View dialogView) {
        EditText etName = dialogView.findViewById(R.id.editEventName);
        TextView tvBeginDate = dialogView.findViewById(R.id.tvBeginDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);
        TextView tvSelectedStartTime = dialogView.findViewById(R.id.tvSelectedStartTime);
        TextView tvSelectedEndTime = dialogView.findViewById(R.id.tvSelectedEndTime);

        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }

        if (beginDate == null) {
            tvBeginDate.setError("Select a date");
            return false;
        }
        if (endDate == null) {
            tvEndDate.setError("Select a date");
            return false;
        }
        if (beginTime == null) {
            tvSelectedStartTime.setError("Select a start time");
            return false;
        }
        if (endTime == null) {
            tvSelectedEndTime.setError("Select an end time");
            return false;
        }

        return true;
    }

    private void createAndSaveEvent(View dialogView, AlertDialog dialog) {
        EditText etName = dialogView.findViewById(R.id.editEventName);
        RadioGroup radioGroupRepeatType = dialogView.findViewById(R.id.radioGroupRepeatType);

        String name = etName.getText().toString().trim();
        RepeatType repeatType = getSelectedRepeatType(radioGroupRepeatType);

        CalendarEvent newEvent = new CalendarEvent(
                name,
                0,
                job.getPayRate(),
                beginDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                beginTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER),
                repeatType
        );

        checkForConflictsAndSaveEvent(newEvent, dialog);
    }

    private RepeatType getSelectedRepeatType(RadioGroup radioGroupRepeatType) {
        int selectedRadioButtonId = radioGroupRepeatType.getCheckedRadioButtonId();

        if (selectedRadioButtonId == R.id.radioNeverRepeat) {
            return RepeatType.NEVER;
        } else if (selectedRadioButtonId == R.id.radioDaily) {
            return RepeatType.DAILY;
        } else if (selectedRadioButtonId == R.id.radioWeekly) {
            return RepeatType.WEEKLY;
        } else if (selectedRadioButtonId == R.id.radioMonthly) {
            return RepeatType.MONTHLY;
        } else if (selectedRadioButtonId == R.id.radioYearly) {
            return RepeatType.ANNUALLY;
        }

        return RepeatType.NEVER; // Default value
    }

    private void checkForConflictsAndSaveEvent(CalendarEvent newEvent, AlertDialog dialog) {
        db.collectionGroup("Events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean conflict = false;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CalendarEvent otherEvent = document.toObject(CalendarEvent.class);
                    Log.i("JobDetailActivity", "Checking for overlap with " + otherEvent.getName());
                    if (hasConflict(newEvent, otherEvent)) {
                        conflict = true;
                        break;
                    }
                }

                if (conflict) {
                    new AlertDialog.Builder(this)
                            .setTitle("Scheduling Conflict")
                            .setMessage("This shift overlaps with an existing shift")
                            .setPositiveButton("Edit", (d, which) -> d.dismiss())
                            .show();
                } else {
                    job.addEvent(newEvent);
                    eventListAdapter.notifyItemInserted(job.getEvents().size() - 1);
                    saveEventToFirestore(newEvent);
                    dialog.dismiss();
                }
            }
        });
    }


    private void saveEventToFirestore(CalendarEvent event) {
        db.collection("Jobs").document(job.getTitle())
                .collection("Events")
                .document(event.getName())
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Event saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving event", e));
    }


    /** EXPENSES **/
    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        AlertDialog dialog = createExpenseDialog(dialogView);
        dialog.show();
    }

    private AlertDialog createExpenseDialog(View dialogView) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Expense")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateExpenseInput(dialogView)) {
                    createAndSaveExpense(dialogView, dialog);
                }
            });
        });

        return dialog;
    }


    private boolean validateExpenseInput(View dialogView) {
        EditText etDescription = dialogView.findViewById(R.id.editExpenseDescription);
        EditText etAmount = dialogView.findViewById(R.id.editExpenseAmount);

        String description = etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            return false;
        }

        String amount = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amount)) {
            etAmount.setError("Amount is required");
            return false;
        }

        return true;
    }

    private void createAndSaveExpense(View dialogView, AlertDialog dialog) {
        EditText etDescription = dialogView.findViewById(R.id.editExpenseDescription);
        EditText etAmount = dialogView.findViewById(R.id.editExpenseAmount);

        String description = etDescription.getText().toString().trim();
        int amount = 0;

        try {
            amount = Integer.parseInt(etAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }

        Expense newExpense = new Expense(description, amount);
        job.addExpense(newExpense);
        expenseListAdapter.notifyItemInserted(job.getExpenses().size() - 1);
        saveExpenseToFirestore(newExpense);
        dialog.dismiss();
    }

    private void saveExpenseToFirestore(Expense expense) {
        db.collection("Jobs").document(job.getTitle())
                .collection("EXP")
                .document()
                .set(expense)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Expense saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving expense", e));
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

    /**
     * Checks if two events overlap in time
     */
    private boolean hasConflict(CalendarEvent newEvent, CalendarEvent existingEvent) {
        LocalDateTime newStart = parseEventDateTime(newEvent, true);
        LocalDateTime newEnd = parseEventDateTime(newEvent, false);
        LocalDateTime existingStart = parseEventDateTime(existingEvent, true);
        LocalDateTime existingEnd = parseEventDateTime(existingEvent, false);

        if (!newEnd.isAfter(newStart) || !existingEnd.isAfter(existingStart)) {
            throw new IllegalArgumentException("Event end time must be after start time.");
        }
        // Check for overlap: an overlap exists if new event starts before the existing event ends
        // and new event ends after the existing event starts.
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }

    /**
     * Parses event date and time into a LocalDateTime object
     */
    private LocalDateTime parseEventDateTime(CalendarEvent event, boolean isStartTime) {
        if (isStartTime) {
            return LocalDateTime.of(
                    LocalDate.parse(event.getBegin_date(), DATE_FORMATTER),
                    LocalTime.parse(event.getBegin_time(), TIME_FORMATTER)
            );
        } else {
            return LocalDateTime.of(
                    LocalDate.parse(event.getEnd_date(), DATE_FORMATTER),
                    LocalTime.parse(event.getEnd_time(), TIME_FORMATTER)
            );
        }
    }

}