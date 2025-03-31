package com.example.myapplication.controller;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.myapplication.view.adapter.SharedEventAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class NewSharedActivity extends AppCompatActivity {
    private static final String TAG = "NewSharedActivity";

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNav;
    private TextInputEditText nameInput;
    private Button button;
    private boolean[] selectedEvents;
    private ArrayList<CalendarEvent> events;
    private ArrayList<Integer> eventList;
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
        topAppBar = findViewById(R.id.topAppBar);
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
        sharedCal = getIntent().getSerializableExtra(EXTRA_SHARED, SharedCal.class);
        if (sharedCal != null) {
            editExisting(sharedCal);
        }
        else {
            createNew();
        }
    }
    private ArrayList<CalendarEvent> loadEventsFromFirestore() {
        db.collectionGroup("Events").whereEqualTo("userId", currentUserId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            CalendarEvent event = doc.toObject(CalendarEvent.class);
                            events.add(event);
                        }
                    }
                });
        return events;
    }
    public void listEvents() {
        adapter = new SharedEventAdapter(loadEventsFromFirestore());
        recycler.setAdapter(adapter);
    }
    public void createNew() {
        listEvents();
        adapter.notifyDataSetChanged();
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
                                Collections.singletonMap("nextId", 2));
                    }

                    ArrayList<CalendarEvent> toAdd = new ArrayList<>(adapter.getSelected());

                    String sharedId = "shared_" + sharedCounterId;
                    newShared[0] = new SharedCal(Objects.requireNonNull(nameInput.getText()).toString().trim(), sharedId, currentUserId, toAdd);
                    // Save the calendar with its ID
                    transaction.set(db.collection("Shared").document(sharedId), newShared[0]);

                    return sharedId;
                }).addOnSuccessListener(jobId -> {
                });
                Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
                intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, sharedCal);
                v.getContext().startActivity(intent);
            }

        });
    }
    public void editExisting(SharedCal cal) {
        listEvents();
        adapter.notifyDataSetChanged();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Shared").whereEqualTo("sharedId", cal.getSharedId())
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    ArrayList<CalendarEvent> calEvents = cal.getEvents();
                                    calEvents.addAll(adapter.getSelected());
                                    DocumentReference ref = db.collection("Shared").document(cal.getSharedId());
                                    ref.update("events", calEvents);
                                    if (nameInput.getText() != null) {
                                        ref.update("name", nameInput.getText().toString().trim());
                                    }
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
