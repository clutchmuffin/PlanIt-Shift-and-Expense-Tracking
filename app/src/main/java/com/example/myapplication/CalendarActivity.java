package com.example.myapplication;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Get reference to the CalendarView from the layout
        CalendarView calendarView = findViewById(R.id.calendarView);

        // Set a listener for date changes (optional)
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Note: 'month' is zero-indexed (0 for January, 11 for December)
                String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                Toast.makeText(CalendarActivity.this, "Selected date: " + date, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

