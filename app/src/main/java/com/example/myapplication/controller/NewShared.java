package com.example.myapplication.controller;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;


public class NewShared extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNav;
    private TextInputEditText nameInput;
    private ListView jobList;
    private TextView dropDownText;
    private boolean[] selectedEvents;
    private ArrayList<CalendarEvent> events;
    private String[] eventNames;
    private ArrayList<Integer> eventList;
    private String currentUserId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sharing);

        bottomNav = findViewById(R.id.bottomNav);
        topAppBar = findViewById(R.id.topAppBar);
        nameInput = findViewById(R.id.nameInput);
        dropDownText = findViewById(R.id.dropDown);

        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        db.collection("Jobs")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(jobSnapshots -> {
                    for (QueryDocumentSnapshot jobDoc : jobSnapshots) {
                        jobDoc.getReference().collection("Events")
                                .get()
                                .addOnSuccessListener(eventSnapshots -> {
                                    for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                                        CalendarEvent event = eventDoc.toObject(CalendarEvent.class);
                                        events.add(event);
                                    }
                                });
                    }
                });
        selectedEvents = new boolean[events.size()];
        eventList = new ArrayList<>();
        int i = 0;
        for (CalendarEvent e : events) {
            eventNames[i] = e.getName();
            i++;
        }

        dropDownText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewShared.this);
                builder.setTitle("Select Events");
                builder.setMultiChoiceItems(eventNames, selectedEvents, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            eventList.add(which);
                            Collections.sort(eventList);
                        }
                        else {
                            eventList.remove(Integer.valueOf(which));
                        }
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
}
