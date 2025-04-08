package com.example.myapplication.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.SharedCal;
import com.example.myapplication.view.adapter.SharedEventAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;


public class NewSharedActivity extends AppCompatActivity {
    private static final String TAG = "NewSharedActivity";

    private BottomNavigationView bottomNav;
    private TextInputEditText nameInput;
    private Button button;
    private ArrayList<CalendarEvent> events;
    public static final String EXTRA_SHARED = "com.example.myapplication.SHARED";
    private String currentUserId;
    private SharedEventAdapter adapter;
    private RecyclerView recycler;
    private SharedCal sharedCal;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sharing);

        bottomNav = findViewById(R.id.bottomNav);
        nameInput = findViewById(R.id.nameInput);
        button = findViewById(R.id.button2);
        recycler = findViewById(R.id.sharedEventRecycle);

        events = new ArrayList<>();

        recycler.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);
        // If no user is logged in, redirect to login
        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupBottomNavigation();

        // Are we editing a previous calendar or making a brand new one?
        sharedCal = getIntent().getSerializableExtra(EXTRA_SHARED, SharedCal.class);
        if (sharedCal != null) {
            editExisting(sharedCal);
        }
        else {
            createNew();
        }
    }


    private ArrayList<CalendarEvent> loadEventsFromFirestore() {
        // look through events for ones with our user ID
        db.collectionGroup("Events").get()
                .addOnSuccessListener(new OnSuccessListener<>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        events.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            CalendarEvent event = doc.toObject(CalendarEvent.class);
                            assert event != null;
                            // if the user id matches, add to list of events
                            if (Objects.equals(event.getUserId(), currentUserId)) {
                                events.add(event);
                            }
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter = new SharedEventAdapter(events);
                            recycler.setAdapter(adapter);
                        }
                    }
                });
        return events;
    }
    public void listEvents() {
        adapter = new SharedEventAdapter(events);
        recycler.setAdapter(adapter);
        loadEventsFromFirestore();
    }
    public void createNew() {
        listEvents();
        adapter.notifyDataSetChanged();
        button.setText("Create");

        // user has entered info and calendar is ready to be created
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use a transaction to generate a counter-based shared ID
                Log.d(TAG, "Attempting to save new calendar.");
                // get selected events from recyclerview
                ArrayList<CalendarEvent> toAdd = new ArrayList<>(adapter.getSelected());
                db.runTransaction(transaction -> {
                    DocumentReference counterRef = db.collection("counters").document("shared");
                    DocumentSnapshot counterDoc = transaction.get(counterRef);
                    int sharedCounterId;

                    if (counterDoc.exists()) {
                        sharedCounterId = counterDoc.getLong("nextId").intValue();
                        transaction.update(counterRef, "nextId", sharedCounterId + 1);
                    } else {
                        sharedCounterId = 1;
                        transaction.set(counterRef, Collections.singletonMap("nextId", 2));
                    }

                    String sharedId = "shared_" + sharedCounterId;
                    String uuid = java.util.UUID.randomUUID().toString(); // generate real UUID here

                    SharedCal newShared = new SharedCal(
                            Objects.requireNonNull(nameInput.getText()).toString().trim(),
                            sharedId,
                            uuid,
                            currentUserId,
                            toAdd
                    );

                    DocumentReference sharedDocRef = db.collection("Shared").document(sharedId);
                    transaction.set(sharedDocRef, newShared);

                    return sharedId;
                }).addOnSuccessListener(sharedId -> {
                    Log.d(TAG, "Shared Calendar created successfully with ID: " + sharedId);

                    // Now add events outside the transaction
                    DocumentReference sharedDocRef = db.collection("Shared").document(sharedId);
                    CollectionReference eventsRef = sharedDocRef.collection("Events");

                    for (CalendarEvent event : toAdd) {
                        eventsRef.add(event)
                                .addOnSuccessListener(ref -> Log.d(TAG, "Event saved: " + event.getName()))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to save event: " + event.getName(), e));
                    }

                    // Continue to next activity
                    Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
                    intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, sharedCal);
                    v.getContext().startActivity(intent);

                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating shared calendar", e);
                });
            }
        });
    }
    public void editExisting(SharedCal cal) {
        listEvents();
        adapter.notifyDataSetChanged();
        button.setText("Confirm");

        // user has entered changes, ready to update database
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<CalendarEvent> updatedEvents = new ArrayList<>();
                updatedEvents.addAll(adapter.getSelected());

                DocumentReference sharedDocRef = db.collection("Shared").document(cal.getSharedId());

                // First, update the shared calendar document in a transaction
                db.runTransaction(transaction -> {
                    DocumentSnapshot sharedSnapshot = transaction.get(sharedDocRef);

                    String updatedName = Objects.requireNonNull(nameInput.getText()).toString().trim();
                    SharedCal updatedShared = new SharedCal(
                            updatedName,
                            cal.getSharedId(),
                            cal.getCode(),
                            currentUserId,
                            updatedEvents
                    );

                    transaction.set(sharedDocRef, updatedShared);
                    Log.d(TAG, "Updated shared calendar metadata");

                    return null;
                }).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully updated shared calendar: " + cal.getSharedId());

                    // Now write events to the subcollection (outside transaction)
                    CollectionReference eventsCollection = sharedDocRef.collection("Events");

                    for (CalendarEvent event : updatedEvents) {
                        if (!sharedCal.getEvents().contains(event)) {
                            eventsCollection.add(event)
                                    .addOnSuccessListener(ref -> Log.d(TAG, "Added event: " + event.getName()))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error adding event: " + event.getName(), e));
                        }
                    }

                    // Continue to calendar screen
                    Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
                    intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, sharedCal);
                    v.getContext().startActivity(intent);

                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating shared calendar", e);
                });
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
