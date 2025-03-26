package com.example.myapplication.controller;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.SharedCal;
import com.example.myapplication.view.adapter.SharedAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class NewSharedActivity extends AppCompatActivity {
    private static final String TAG = "NewSharedActivity";

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNav;
    private TextInputEditText nameInput;
    private ListView jobList;
    private Button button;
    private TextView dropDownText;
    private boolean[] selectedEvents;
    private ArrayList<CalendarEvent> events;
    private String[] eventNames;
    private ArrayList<Integer> eventList;
    public static final String EXTRA_SHARED = "com.example.myapplication.SHARED";
    private String currentUserId;
    private SharedAdapter sharedAdapter;
    private SharedCal sharedCal;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sharing);

        bottomNav = findViewById(R.id.bottomNav);
        topAppBar = findViewById(R.id.topAppBar);
        nameInput = findViewById(R.id.nameInput);
        button = findViewById(R.id.button2);
        dropDownText = findViewById(R.id.dropDown);

        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);
        // If no user is logged in, redirect to login
        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setupBottomNavigation();
        sharedCal = getIntent().getSerializableExtra(EXTRA_SHARED, SharedCal.class);
        if (sharedCal != null) {
            editExisting(sharedCal);
        }
        else {
            createNew();
        }
    }
    private void loadEventsFromFirestore() {
        events.clear();

        db.collection("Jobs")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(jobsSnapshot -> {
                    for (DocumentSnapshot jobDoc : jobsSnapshot.getDocuments()) {
                        // Fetch events for each job one after another
                        db.collection("Jobs")
                                .document(jobDoc.getId())
                                .collection("Events")
                                .get()
                                .addOnSuccessListener(eventsSnapshot -> {
                                    for (DocumentSnapshot eventDoc : eventsSnapshot.getDocuments()) {
                                        CalendarEvent event = eventDoc.toObject(CalendarEvent.class);
                                        events.add(event);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error loading events", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading jobs", e));
    }
    public void listEvents() {
        events = new ArrayList<>();
        loadEventsFromFirestore();
        eventList = new ArrayList<>();
        eventNames = new String[events.size()];
        int i = 0;
        for (CalendarEvent e : events) {
            eventNames[i] = e.getName();
            i++;
        }
        selectedEvents = new boolean[eventNames.length];

        dropDownText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewSharedActivity.this);
                builder.setTitle("Select Events");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(eventNames, selectedEvents, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        eventList.add(which);
                        Collections.sort(eventList);
                    }
                    else {
                        eventList.remove(Integer.valueOf(which));
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < eventList.size(); j++) {
                            stringBuilder.append(eventNames[j]);
                            if (j != eventList.size() - 1) {
                                stringBuilder.append(",");
                            }
                        }
                        dropDownText.setText(stringBuilder.toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int j = 0; j < selectedEvents.length; j++) {
                            selectedEvents[j] = false;
                            eventList.clear();
                            dropDownText.setText("");
                        }
                    }
                });
                builder.show();
            }
        });
    }
    public void createNew() {
        listEvents();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use a transaction to generate a counter-based shared ID
                final SharedCal[] newShared = new SharedCal[1];
                db.runTransaction(transaction -> {
                    DocumentSnapshot counterDoc = transaction.get(db.collection("counters").document("shared"));

                    int sharedCounterId;
                    if (counterDoc.exists()) {
                        sharedCounterId = counterDoc.getLong("nextId").intValue();
                        transaction.update(db.collection("counters").document("shared"), "nextId", sharedCounterId + 1);
                    } else {
                        // First shared calendar, initialize counter
                        sharedCounterId = 1;
                        transaction.set(db.collection("counters").document("shared"),
                                java.util.Collections.singletonMap("nextId", 2));
                    }

                    ArrayList<CalendarEvent> toAdd = new ArrayList<>();

                    for (int j = 0; j < eventList.size(); j++) {
                        toAdd.add(events.get(eventList.get(j)));
                    }

                    String sharedId = "shared_" + sharedCounterId;
                    newShared[0] = new SharedCal(Objects.requireNonNull(nameInput.getText()).toString().trim(), sharedId, currentUserId, toAdd);
                    // Save the calendar with its ID
                    transaction.set(db.collection("Shared").document(sharedId), newShared[0]);

                    return sharedId;
                }).addOnSuccessListener(jobId -> {
                    sharedAdapter.notifyDataSetChanged();
                });
                Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
                intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, sharedCal);
                v.getContext().startActivity(intent);
            }

        });
    }
    public void editExisting(SharedCal cal) {
        listEvents();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Shared").whereEqualTo("sharedId", cal.getSharedId())
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    ArrayList<CalendarEvent> calEvents = cal.getEvents();
                                    for (int j = 0; j < eventList.size(); j++) {
                                        calEvents.add(events.get(eventList.get(j)));
                                    }
                                    DocumentReference ref = db.collection("Shared").document(cal.getSharedId());
                                    ref.update("events", calEvents);
                                    if (nameInput.getText() != null) {
                                        ref.update("name", nameInput.getText().toString().trim());
                                    }
                                    sharedAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.e("SharingMainActivity", "Error getting documents: ", task.getException());
                            }
                        });

                Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
                intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, sharedCal);
                v.getContext().startActivity(intent);
            }

        });
    }
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_jobs) {
                Intent intent = new Intent(NewSharedActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (itemId == R.id.nav_budget) {
                startActivity(new Intent(NewSharedActivity.this, BudgetMainActivity.class));
                return true;
            } else if (itemId == R.id.nav_sharing) {
                // already here
                return true;
            }
            return false;
        });
    }
}
