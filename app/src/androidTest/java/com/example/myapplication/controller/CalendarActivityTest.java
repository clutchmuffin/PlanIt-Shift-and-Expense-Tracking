package com.example.myapplication.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.myapplication.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CalendarActivityTest {

    private ActivityScenario<CalendarActivity> scenario;

    @Before
    public void setup() {
        // Set up logged-in state before activity launches
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Set the login state in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("PlanITPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Add necessary login data
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", "1");
        editor.putString("userName", "FirstUser");
        editor.putString("userUsername", "FirstUserName");
        editor.putString("email", "user@domain.com");

        // Apply changes
        editor.apply();
        scenario = ActivityScenario.launch(CalendarActivity.class);
    }

    @Rule
    public ActivityScenarioRule<CalendarActivity> activityRule =
            new ActivityScenarioRule<>(CalendarActivity.class);

    @Test
    public void testCalendarActivityLaunch() {
        // Check if the calendar activity is displayed
        onView(withId(R.id.MonthYearText)).check(matches(isDisplayed()));
    }

    @Test
    public void testCalendarNavigation_Right() {
        // Click the right arrow button
        onView(withId(R.id.NextMonthImage)).perform(click());

        // Check if the next month is displayed
        onView(withId(R.id.MonthYearText)).check(matches(withText("May 2025")));
    }

    @Test
    public void testCalendarNavigation_Left() {
        // Click the right arrow button
        onView(withId(R.id.PreviousMonthImage)).perform(click());

        // Check if the next month is displayed
        onView(withId(R.id.MonthYearText)).check(matches(withText("March 2025")));
    }

    @After
    public void tearDown() {
        // Close the activity after each test
        activityRule.getScenario().close();
    }

}
