package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.SharedCal;
import com.example.myapplication.view.adapter.dailyEventListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

public class SharedCalendarActivity extends AppCompatActivity {
    private static final String TAG = "CalendarActivity";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private String currentUserId;
    private String currentSharedId;
    private YearMonth currentVisibleMonth;
    private CalendarView calendarView;
    private RecyclerView dailyEventRecyclerView;
    private com.example.myapplication.view.adapter.dailyEventListAdapter dailyEventListAdapter;
    private List<CalendarEvent> allEvents = new ArrayList<>();
    private SharedCal cal;
    public static final String EXTRA_SHARED = "com.example.myapplication.SHARED";
    private Button editButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_calendar);

        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        cal = getIntent().getSerializableExtra(EXTRA_SHARED, SharedCal.class);

        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupCalendarDateRange();
        setupMonthNavigation();
        setupDayOfWeekHeaders();
        setupCalendarDayBinder();
        setupDailyEventsRecyclerView();
        loadEventsFromFirestore();

        editButton = findViewById(R.id.button3);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NewSharedActivity.class);
                intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, cal);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        dailyEventRecyclerView = findViewById(R.id.dailyEventsRecyclerView);
    }
    private void setupMonthNavigation() {
        TextView monthTextView = findViewById(R.id.MonthYearText);
        View previousMonthImage = findViewById(R.id.PreviousMonthImage);
        View nextMonthImage = findViewById(R.id.NextMonthImage);

        // Set up arrow click listeners
        previousMonthImage.setOnClickListener(v -> {
            YearMonth previousMonth = currentVisibleMonth.minusMonths(1);
            calendarView.scrollToMonth(previousMonth);
        });

        nextMonthImage.setOnClickListener(v -> {
            YearMonth nextMonth = currentVisibleMonth.plusMonths(1);
            calendarView.scrollToMonth(nextMonth);
        });

        // Add month change listener
        calendarView.setMonthScrollListener(month -> {
            currentVisibleMonth = month.getYearMonth();
            monthTextView.setText(currentVisibleMonth.format(monthTitleFormatter));
            return null;
        });
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
        calendarView.setDayBinder(new MonthDayBinder<SharedCalendarActivity.DayViewContainer>() {
            @NonNull
            @Override
            public SharedCalendarActivity.DayViewContainer create(@NonNull View view) {
                return new SharedCalendarActivity.DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull SharedCalendarActivity.DayViewContainer container, CalendarDay day) {
                container.day = day;
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getPosition() == DayPosition.MonthDate) {
                    container.textView.setVisibility(View.VISIBLE);
                    container.textView.setTextColor(Color.BLACK);

                    // Check if there are events for this day
                    String dateString = day.getDate().format(DATE_FORMATTER);
                    Log.d(TAG, "Checking events for date: " + dateString);
                    boolean hasEvents = false;

                    for (CalendarEvent event : allEvents) {
                        Log.d(TAG, "Event date: " + event.getBegin_date() + ", Calendar day: " + dateString);
                        if (event.getBegin_date().equals(dateString)) {
                            hasEvents = true;
                            break;
                        }
                    }

                    // Show indicator only if events exist
                    container.eventIndicator.setVisibility(hasEvents ? View.VISIBLE : View.INVISIBLE);
                } else {
                    container.textView.setVisibility(View.INVISIBLE);
                    container.eventIndicator.setVisibility(View.INVISIBLE);
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

        db.collection("Shared")
                .whereEqualTo("sharedId", currentSharedId)
                .get()
                .addOnSuccessListener(jobsSnapshot -> {
                    for (DocumentSnapshot jobDoc : jobsSnapshot.getDocuments()) {
                        // Fetch events for each job one after another
                        db.collection("Events")
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
        public View eventIndicator;

        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
            eventIndicator = view.findViewById(R.id.eventIndicator);

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
