package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.AlarmType;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.EXP;
import com.example.myapplication.model.Expense;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.NotificationSender;
import com.example.myapplication.model.RepeatType;
import com.example.myapplication.view.adapter.EventListAdapter;
import com.example.myapplication.view.adapter.ExpenseListAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private ExtendedFloatingActionButton fabAddButton;
    private ExtendedFloatingActionButton fabAddExpense;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;
    private String currentUserId;

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
        tvPayRate.setText("Payrate: $" + job.getPayRate() + "/hr");
    }

    private void setupEventRecyclerView() {
        // Set up the RecyclerView.
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String jobId = job.getJobId();

        // Set the adapter.
        eventListAdapter = new EventListAdapter(job.getEvents(), job, this);
        eventRecyclerView.setAdapter(eventListAdapter);

        // First check if job has documentId
        if (jobId != null && !jobId.isEmpty()) {

            job.getEvents().clear();

            db.collection("Jobs").document(jobId).collection("Events")
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

        }

    }


    private void setupExpenseRecyclerView() {
        // Set up the RecyclerView.
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String jobId = job.getJobId();

        // Set the adapter.
        expenseListAdapter = new ExpenseListAdapter(job.getExpenses(), job);
        expenseRecyclerView.setAdapter(expenseListAdapter);

        // First check if job has documentId
        if (jobId != null && !jobId.isEmpty()) {

            job.getExpenses().clear();

            db.collection("Jobs").document(jobId).collection("EXP")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EXP expense = document.toObject(EXP.class);
                                job.addExpense(expense);
                            }
                            expenseListAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting expenses: ", task.getException());
                        }
                    });

        }

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

        List<String> alarmOptions = new ArrayList<>();
        alarmOptions.add("NONE");
        alarmOptions.add("1 hour before start");
        alarmOptions.add("2 hours before start");
        alarmOptions.add("3 hours before start");

        Spinner alarmPicker = dialogView.findViewById(R.id.alarmPicker);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.alarm_spinner,
                alarmOptions
        );
        alarmPicker.setAdapter(adapter);


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
        Spinner alarmPicker = dialogView.findViewById(R.id.alarmPicker);

        String name = etName.getText().toString().trim();
        RepeatType repeatType = getSelectedRepeatType(radioGroupRepeatType);
        int notifID = getNewEventID();
        int alarmID = getNewAlarmID();
        AlarmType alarmType = getAlarmType(alarmPicker);
        System.out.println(alarmType);

        CalendarEvent newEvent = new CalendarEvent(
                name,
                currentUserId,
                job.getPayRate(),
                beginDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                beginTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER),
                repeatType,
                notifID,
                alarmID,
                alarmType,
                job.getColor()
        );

        checkForConflictsAndSaveEvent(newEvent, dialog);
    }

    private AlarmType getAlarmType(Spinner alarmPicker) {
        String picked = alarmPicker.getSelectedItem().toString();
        switch (picked) {
            case "NONE":
                return AlarmType.NONE;
            case "1 hour before start":
                return AlarmType.ONE_HOUR;
            case "2 hours before start":
                return AlarmType.TWO_HOUR;
            case "3 hours before start":
                return AlarmType.THREE_HOUR;
            default:
                return AlarmType.NONE;
        }
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
        db.collection("Jobs")
                .whereEqualTo("userId", currentUserId) // Get only jobs owned by current user
                .get()
                .addOnCompleteListener(jobTask -> {
                    if (jobTask.isSuccessful()) {
                        Log.i(TAG, "Fetched Jobs for User");
                        List<Task<QuerySnapshot>> eventTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot jobDoc : jobTask.getResult()) {
                            Task<QuerySnapshot> eventTask = jobDoc.getReference().collection("Events")
                                    .get();
                            eventTasks.add(eventTask);
                        }

                        Tasks.whenAllComplete(eventTasks).addOnCompleteListener(allEventsTask -> {
                            boolean conflict = false;

                            for (Task<QuerySnapshot> eventTask : eventTasks) {
                                if (eventTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                        CalendarEvent otherEvent = eventDoc.toObject(CalendarEvent.class);
                                        Log.i(TAG, "Checking for overlap with " + otherEvent.getName());

                                        if (hasConflict(newEvent, otherEvent)) {
                                            conflict = true;
                                            break;
                                        }
                                    }
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
                    NotificationSender notificationSender = new NotificationSender(this);
                    notificationSender.scheduleDailyNotification(newEvent, job.getEmployer());
                    notificationSender.updateWeeklyNotif();
                    notificationSender.scheduleAlarm(newEvent, job.getEmployer());
                }
            });
            }
        });
    }

    private void saveEventToFirestore(CalendarEvent event) {
        if (job.getJobId() != null && !job.getJobId().isEmpty()) {
            // Use the stored document ID
            db.collection("Jobs").document(job.getJobId())
                    .collection("Events")
                    .document()
                    .set(event)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Event saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving event", e));
        }
    }

    /** EDIT EVENT **/
    public void showEditEventDialog(CalendarEvent event, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
        AlertDialog dialog = createEditEventDialog(dialogView, event, position);
        dialog.show();
    }

    private AlertDialog createEditEventDialog(View dialogView, CalendarEvent event, int position) {
        // Create an AlertDialog similar to createEventDialog but for editing
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Shift")
                .setView(dialogView)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        // Find and set up the dialog controls (same as in createEventDialog)
        Button btnSelectBeginDate = dialogView.findViewById(R.id.btnSelectBeginDate);
        Button btnSelectEndDate = dialogView.findViewById(R.id.btnSelectEndDate);
        Button btnSelectStartTime = dialogView.findViewById(R.id.btnSelectStartTime);
        Button btnSelectEndTime = dialogView.findViewById(R.id.btnSelectEndTime);
        TextView tvBeginDate = dialogView.findViewById(R.id.tvBeginDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);
        TextView tvSelectedStartTime = dialogView.findViewById(R.id.tvSelectedStartTime);
        TextView tvSelectedEndTime = dialogView.findViewById(R.id.tvSelectedEndTime);
        EditText etName = dialogView.findViewById(R.id.editEventName);
        RadioGroup radioGroupRepeatType = dialogView.findViewById(R.id.radioGroupRepeatType);
        Spinner alarmPicker = dialogView.findViewById(R.id.alarmPicker);

        // Set up alarm options same as in createEventDialog
        List<String> alarmOptions = new ArrayList<>();
        alarmOptions.add("NONE");
        alarmOptions.add("1 hour before start");
        alarmOptions.add("2 hours before start");
        alarmOptions.add("3 hours before start");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.alarm_spinner,
                alarmOptions
        );
        alarmPicker.setAdapter(adapter);

        // Pre-fill dialog with event data
        etName.setText(event.getName());
        tvBeginDate.setText(event.getBegin_date());
        tvEndDate.setText(event.getEnd_date());
        tvSelectedStartTime.setText(event.getBegin_time());
        tvSelectedEndTime.setText(event.getEnd_time());

        // Parse dates and times for the date/time picker
        beginDate = LocalDate.parse(event.getBegin_date(), DATE_FORMATTER);
        endDate = LocalDate.parse(event.getEnd_date(), DATE_FORMATTER);
        beginTime = LocalTime.parse(event.getBegin_time(), TIME_FORMATTER);
        endTime = LocalTime.parse(event.getEnd_time(), TIME_FORMATTER);

        // Set repeat type
        switch (event.getRepeated()) {
            case NEVER:
                radioGroupRepeatType.check(R.id.radioNeverRepeat);
                break;
            case DAILY:
                radioGroupRepeatType.check(R.id.radioDaily);
                break;
            case WEEKLY:
                radioGroupRepeatType.check(R.id.radioWeekly);
                break;
            case MONTHLY:
                radioGroupRepeatType.check(R.id.radioMonthly);
                break;
            case ANNUALLY:
                radioGroupRepeatType.check(R.id.radioYearly);
                break;
        }

        // Set alarm type
        int alarmPosition = 0;
        switch (event.getAlarmType()) {
            case NONE:
                alarmPosition = 0;
                break;
            case ONE_HOUR:
                alarmPosition = 1;
                break;
            case TWO_HOUR:
                alarmPosition = 2;
                break;
            case THREE_HOUR:
                alarmPosition = 3;
                break;
        }
        alarmPicker.setSelection(alarmPosition);

        // Set up button listeners same as in createEventDialog
        btnSelectBeginDate.setOnClickListener(v -> showDatePicker(tvBeginDate, true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(tvEndDate, false));
        btnSelectStartTime.setOnClickListener(v -> showTimePicker(tvSelectedStartTime, true));
        btnSelectEndTime.setOnClickListener(v -> showTimePicker(tvSelectedEndTime, false));

        // Set up positive button click listener for updating the event
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateEventInput(dialogView)) {
                    updateEvent(dialogView, dialog, event, position);
                }
            });
        });

        return dialog;
    }

    private void updateEvent(View dialogView, AlertDialog dialog, CalendarEvent originalEvent, int position) {
        EditText etName = dialogView.findViewById(R.id.editEventName);
        RadioGroup radioGroupRepeatType = dialogView.findViewById(R.id.radioGroupRepeatType);
        Spinner alarmPicker = dialogView.findViewById(R.id.alarmPicker);

        String name = etName.getText().toString().trim();
        RepeatType repeatType = getSelectedRepeatType(radioGroupRepeatType);
        AlarmType alarmType = getAlarmType(alarmPicker);

        // Create an updated event with the same IDs but new data
        CalendarEvent updatedEvent = new CalendarEvent(
                name,
                originalEvent.getUserId(),
                job.getPayRate(),
                beginDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                beginTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER),
                repeatType,
                originalEvent.getNotifID(),
                originalEvent.getAlarmID(),
                alarmType,
                job.getColor()
        );

        // Check for conflicts with other events (excluding the current event being edited)
        checkForConflictsAndUpdateEvent(originalEvent, updatedEvent, position, dialog);
    }

    private void checkForConflictsAndUpdateEvent(CalendarEvent originalEvent, CalendarEvent updatedEvent, int position, AlertDialog dialog) {
        db.collection("Jobs")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(jobTask -> {
                    if (jobTask.isSuccessful()) {
                        List<Task<QuerySnapshot>> eventTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot jobDoc : jobTask.getResult()) {
                            Task<QuerySnapshot> eventTask = jobDoc.getReference().collection("Events")
                                    .get();
                            eventTasks.add(eventTask);
                        }

                        Tasks.whenAllComplete(eventTasks).addOnCompleteListener(allEventsTask -> {
                            boolean conflict = false;

                            for (Task<QuerySnapshot> eventTask : eventTasks) {
                                if (eventTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                        CalendarEvent otherEvent = eventDoc.toObject(CalendarEvent.class);

                                        // Skip the current event being edited when checking for conflicts
                                        if (otherEvent.getBegin_date().equals(originalEvent.getBegin_date()) &&
                                                otherEvent.getBegin_time().equals(originalEvent.getBegin_time()) &&
                                                otherEvent.getEnd_date().equals(originalEvent.getEnd_date()) &&
                                                otherEvent.getEnd_time().equals(originalEvent.getEnd_time())) {
                                            continue;
                                        }

                                        if (hasConflict(updatedEvent, otherEvent)) {
                                            conflict = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (conflict) {
                                new AlertDialog.Builder(this)
                                        .setTitle("Scheduling Conflict")
                                        .setMessage("This shift overlaps with an existing shift")
                                        .setPositiveButton("Edit", (d, which) -> d.dismiss())
                                        .show();
                            } else {
                                // Update local array
                                job.getEvents().set(position, updatedEvent);
                                eventListAdapter.notifyItemChanged(position);

                                // Update in Firestore
                                updateEventInFirestore(originalEvent, updatedEvent);

                                // Update notifications
                                NotificationSender notificationSender = new NotificationSender(this);
                                notificationSender.cancelNotification(originalEvent);
                                notificationSender.scheduleDailyNotification(updatedEvent, job.getEmployer());
                                notificationSender.updateWeeklyNotif();
                                notificationSender.scheduleAlarm(updatedEvent, job.getEmployer());

                                dialog.dismiss();
                            }
                        });
                    }
                });
    }

    private void updateEventInFirestore(CalendarEvent originalEvent, CalendarEvent updatedEvent) {
        if (job.getJobId() != null && !job.getJobId().isEmpty()) {
            // Find the event document by matching fields
            db.collection("Jobs").document(job.getJobId())
                    .collection("Events")
                    .whereEqualTo("begin_date", originalEvent.getBegin_date())
                    .whereEqualTo("begin_time", originalEvent.getBegin_time())
                    .whereEqualTo("end_date", originalEvent.getEnd_date())
                    .whereEqualTo("end_time", originalEvent.getEnd_time())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the document ID of the matching event
                            String eventDocId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            // Update the event in Firestore
                            db.collection("Jobs").document(job.getJobId())
                                    .collection("Events")
                                    .document(eventDocId)
                                    .set(updatedEvent)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Event updated successfully"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error updating event", e));
                        } else {
                            Log.e(TAG, "Could not find event document to update");
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error querying for event to update", e));
        }
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

        setupDatePickers(dialogView);

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateExpenseInput(dialogView)) {
                    createAndSaveExpense(dialogView, dialog);
                }
            });
        });

        return dialog;
    }

    private void setupDatePickers(View dialogView) {
        Button btnSelectBeginDate = dialogView.findViewById(R.id.btnSelectBeginDate);
        Button btnSelectEndDate = dialogView.findViewById(R.id.btnSelectEndDate);
        TextView tvBeginDate = dialogView.findViewById(R.id.tvBeginDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);

        btnSelectBeginDate.setOnClickListener(v -> showDatePicker(tvBeginDate, true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(tvEndDate, false));
    }


    private boolean validateExpenseInput(View dialogView) {
        EditText etDescription = dialogView.findViewById(R.id.editExpenseDescription);
        EditText etAmount = dialogView.findViewById(R.id.editExpenseAmount);
        Button btnSelectBeginDate = dialogView.findViewById(R.id.btnSelectBeginDate);
        Button btnSelectEndDate = dialogView.findViewById(R.id.btnSelectEndDate);
        TextView tvBeginDate = dialogView.findViewById(R.id.tvBeginDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);

        btnSelectBeginDate.setOnClickListener(v -> showDatePicker(tvBeginDate, true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(tvEndDate, false));

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
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return false;
        }

        if (beginDate == null) {
            tvBeginDate.setError("Select a start date");
            return false;
        }
        if (endDate == null) {
            tvEndDate.setError("Select an end date");
            return false;
        }


        return true;
    }

    private void createAndSaveExpense(View dialogView, AlertDialog dialog) {

        EditText etName = dialogView.findViewById(R.id.editExpenseDescription);
        EditText etAmount = dialogView.findViewById(R.id.editExpenseAmount);
        RadioGroup radioGroupRepeatType = dialogView.findViewById(R.id.radioGroupRepeatType);

        String name = etName.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());

        RepeatType repeatType = getSelectedRepeatType(radioGroupRepeatType);
        EXP newExpense = new EXP(name, amount, repeatType, beginDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));

        saveExpenseToFirestore(newExpense);
        job.addExpense(newExpense);
        expenseListAdapter.notifyItemInserted(job.getExpenses().size() - 1);
        dialog.dismiss();

    }

    private void saveExpenseToFirestore(EXP expense) {
        if (job.getJobId() != null && !job.getJobId().isEmpty()) {
            // Use the stored document ID
            db.collection("Jobs").document(job.getJobId())
                    .collection("EXP")
                    .document()
                    .set(expense)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Expense saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving expense", e));
        }
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

    private int getNewEventID(){
        // Find a notification ID for the new shift to be added
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(sharedPref.getAll().isEmpty()){
            editor.putInt("dailyNotif", 3);
            editor.apply();
        }
        int newID = sharedPref.getInt("dailyNotif", 3);
        editor.putInt("dailyNotif", newID + 1);
        editor.apply();
        return newID;
    }

    private int getNewAlarmID(){
        // Find a notification ID for the new shift to be added
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(sharedPref.getAll().isEmpty()){
            editor.putInt("alarmID", 1);
            editor.apply();
        }
        int newID = sharedPref.getInt("alarmID", 1);
        editor.putInt("alarmID", newID + 1);
        editor.apply();
        return newID;
    }
}

