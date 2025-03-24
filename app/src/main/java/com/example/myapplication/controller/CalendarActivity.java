package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.view.adapter.dailyEventListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String currentUserId;
    private CalendarView calendarView;
    private RecyclerView dailyEventRecyclerView;
    private dailyEventListAdapter dailyEventListAdapter;
    private List<CalendarEvent> allEvents = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupCalendarDateRange();
        setupDayOfWeekHeaders();
        setupCalendarDayBinder();
        setupDailyEventsRecyclerView();
        loadEventsFromFirestore();
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        dailyEventRecyclerView = findViewById(R.id.dailyEventsRecyclerView);
    }

    private void setupCalendarDateRange() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);
        DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);
    }

    private void setupDayOfWeekHeaders() {
        List<DayOfWeek> daysOfWeek = Arrays.asList(DayOfWeek.values());
        ViewGroup titlesContainer = findViewById(R.id.titlesContainer);

        int childCount = titlesContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = titlesContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                String title = daysOfWeek.get(i).getDisplayName(TextStyle.SHORT, Locale.getDefault());
                textView.setText(title);
            }
        }
    }

    private void setupCalendarDayBinder() {
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                container.day = day;
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getPosition() == DayPosition.MonthDate) {
                    container.textView.setVisibility(View.VISIBLE);
                    container.textView.setTextColor(Color.BLACK);
                } else {
                    container.textView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void setupDailyEventsRecyclerView() {
        dailyEventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dailyEventListAdapter = new dailyEventListAdapter(new ArrayList<>());
        dailyEventRecyclerView.setAdapter(dailyEventListAdapter);
    }

    private void loadEventsFromFirestore() {
        allEvents.clear();
        
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
                                allEvents.add(event);
                            }
                            // Update UI after each job's events are loaded
                            dailyEventListAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error loading events", e));
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error loading jobs", e));
    }


    // ViewContainer for calendar day cells
    public class DayViewContainer extends ViewContainer {
        public CalendarDay day;
        public TextView textView;

        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);

            view.setOnClickListener(v -> {
                if (day != null) {
                    String selectedDate = day.getDate().format(DATE_FORMATTER);

                    List<CalendarEvent> dailyEvents = new ArrayList<>();
                    for (CalendarEvent event : allEvents) {
                        if (event.getBegin_date().equals(selectedDate)) {
                            dailyEvents.add(event);
                        }
                    }

                    dailyEventListAdapter = new dailyEventListAdapter(dailyEvents.reversed());
                    dailyEventRecyclerView.setAdapter(dailyEventListAdapter);
                }
            });
        }
    }
}