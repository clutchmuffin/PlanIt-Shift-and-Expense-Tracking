package com.example.myapplication.controller;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private static List<CalendarEvent> events = new ArrayList<>();
    private static RecyclerView dailyEventRecyclerView;
    private static dailyEventListAdapter dailyEventListAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Find the CalendarView defined in activity_calendar.xml
        calendarView = findViewById(R.id.calendarView);

        // Define the calendar's date range.
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(12);
        YearMonth endMonth = currentMonth.plusMonths(12);

        // Define the first day of the week.
        // For simplicity, we use Monday; alternatively, you could determine this from Locale.
        DayOfWeek firstDay = DayOfWeek.MONDAY;

        // Setup the CalendarView with the start and end dates.
        calendarView.setup(startMonth, endMonth, firstDay);
        calendarView.scrollToMonth(currentMonth);

        List<DayOfWeek> daysOfWeek = java.util.Arrays.asList(DayOfWeek.values());
        ViewGroup titlesContainer = findViewById(R.id.titlesContainer);
        int childCount = titlesContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = titlesContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                DayOfWeek dayOfWeek = daysOfWeek.get(i);
                String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                textView.setText(title);
            }
        }

        // Set the day binder to create and bind each day cell.
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {

            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay data) {
                // Store the day data in the container.
                container.day = data;
                // Set the day number in the TextView.
                container.textView.setText(String.valueOf(data.getDate().getDayOfMonth()));
                // Only show days that belong to the current month.
                if (data.getPosition() == DayPosition.MonthDate) {
                    container.textView.setVisibility(View.VISIBLE);
                    container.textView.setTextColor(Color.BLACK);
                } else {
                    container.textView.setVisibility(View.INVISIBLE);
                }
            }
        });

        dailyEventRecyclerView = findViewById(R.id.dailyEventsRecyclerView);
        dailyEventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        db.collectionGroup("Events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    events.add(document.toObject(CalendarEvent.class));
                }
            }
        });
    }

    // A simple ViewContainer subclass to hold our day cell view.
    public static class DayViewContainer extends ViewContainer {
        public CalendarDay day;
        public TextView textView;

        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);

            // Set the click listener on the entire container view.
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (day != null) {
                        // Format the day as a full date string
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String selectedDate = day.getDate().format(formatter);

                        List<CalendarEvent> dailyEvents = new ArrayList<>();
                        for (CalendarEvent event : events) {
                            if (event.getBegin_date().equals(selectedDate)) {
                                dailyEvents.add(event);
                            }
                        }

                        // Then update the adapter for the daily events RecyclerView with dailyEvents
                        dailyEventListAdapter = new dailyEventListAdapter(dailyEvents);
                        dailyEventRecyclerView.setAdapter(dailyEventListAdapter);
                    }
                }
            });
        }
    }
}
