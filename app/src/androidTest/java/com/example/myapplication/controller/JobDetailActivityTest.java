package com.example.myapplication.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.myapplication.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class JobDetailActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setup() throws UiObjectNotFoundException {
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
        scenario = ActivityScenario.launch(MainActivity.class);

        // Handle notification permission dialog
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject allowButton = device.findObject(new UiSelector()
                .text("Allow")  // or "Yes" or whatever the button text is
                .className("android.widget.Button"));

        if (allowButton.exists()) {
            allowButton.click();
        }
    }

    @Test
    public void testJobDetailActivityLaunch() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Check if JobDetailActivity is displayed
        onView(withId(R.id.detailJobTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void testJobDetailActivity_JobDetails() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Check if the job title is displayed
        onView(withId(R.id.detailJobTitle)).check(matches(isDisplayed()));

        // Check if the employer name is displayed
        onView(withId(R.id.detailJobEmployer)).check(matches(isDisplayed()));

        // Check if the job location is displayed
        onView(withId(R.id.detailJobLocation)).check(matches(isDisplayed()));

        // Check if the job pay rate is displayed
        onView(withId(R.id.detailJobPayRate)).check(matches(isDisplayed()));

        // Check if the job color is displayed
        onView(withId(R.id.detailJobColor)).check(matches(isDisplayed()));
    }

    @Test
    public void testJobDetailActivity_Events() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Check if the events section is displayed
        onView(withText("Events")).check(matches(isDisplayed()));

        // Check if the event recycler view is displayed
        onView(withId(R.id.eventRecyclerView)).check(matches(isDisplayed()));

        // Check if the add event button is displayed
        onView(withId(R.id.fabAdd)).check(matches(isDisplayed()));
    }

    @Test
    public void testJobDetailActivity_Expenses() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Check if the expenses section is displayed
        onView(withText("Expenses")).check(matches(isDisplayed()));

        // Check if the expense recycler view is displayed
        onView(withId(R.id.expenseRecyclerView)).check(matches(isDisplayed()));

        // Check if the add expense button is displayed
        onView(withId(R.id.fabAddExp)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddingEvent_NoData() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAdd)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));

        // Click adding without title
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editEventName)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testAddingEvent_OnlyStartDate() throws InterruptedException {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAdd)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));

        // Click on the start date
        onView(withId(R.id.btnSelectBeginDate)).perform(click());

        // Click the current date in the calendar grid
        int currentDay = java.time.LocalDate.now().getDayOfMonth();
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject dayButton = device.findObject(new UiSelector()
                .text(String.valueOf(currentDay))
                .className("android.widget.TextView"));
        try {
            dayButton.click();
        } catch (UiObjectNotFoundException e) {
            throw new AssertionError("Could not find day " + currentDay, e);
        }

        // Click on the OK button in the date picker
        onView(withText("OK")).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editEventName)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testAddingEvent_OnlyEndDate() throws InterruptedException {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAdd)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));

        // Click on the end date
        onView(withId(R.id.btnSelectEndDate)).perform(click());

        // Click the current date in the calendar grid
        int currentDay = java.time.LocalDate.now().getDayOfMonth();
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject dayButton = device.findObject(new UiSelector()
                .text(String.valueOf(currentDay))
                .className("android.widget.TextView"));
        try {
            dayButton.click();
        } catch (UiObjectNotFoundException e) {
            throw new AssertionError("Could not find day " + currentDay, e);
        }

        // Click on the OK button in the date picker
        onView(withText("OK")).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editEventName)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testAddingEvent_OnlyStartTime() throws InterruptedException {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAdd)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));

        // Click on the start date
        onView(withId(R.id.btnSelectStartTime)).perform(click());

        // Click on the OK button in the date picker
        onView(withText("OK")).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editEventName)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testAddingEvent_OnlyEndTime() throws InterruptedException {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAdd)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));

        // Click on the start date
        onView(withId(R.id.btnSelectEndTime)).perform(click());

        // Click on the OK button in the date picker
        onView(withText("OK")).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editEventName)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testAddingEvent_OnlyRepeatStatus() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAdd)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));

        // Click on the daily repeat radio button
        onView(withId(R.id.radioDaily)).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editEventName)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testAddingExpense_NoData() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add expense button
        onView(withId(R.id.fabAddExp)).perform(click());

        // Check if the add expense dialog is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(isDisplayed()));

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(hasErrorText("Description is required")));
    }

    @Test
    public void testAddingExpense_OnlyDescription() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add expense button
        onView(withId(R.id.fabAddExp)).perform(click());

        // Check if the add expense dialog is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(isDisplayed()));

        // Add only the expense description
        onView(withId(R.id.editExpenseDescription))
                .perform(typeText("Espresso Test Expense"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editExpenseAmount)).check(matches(hasErrorText("Amount is required")));
    }

    @Test
    public void testAddingExpense_OnlyBeginDate() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAddExp)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(isDisplayed()));

        // Click on the start date
        onView(withId(R.id.btnSelectBeginDate)).perform(click());

        // Click the current date in the calendar grid
        int currentDay = java.time.LocalDate.now().getDayOfMonth();
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject dayButton = device.findObject(new UiSelector()
                .text(String.valueOf(currentDay))
                .className("android.widget.TextView"));
        try {
            dayButton.click();
        } catch (UiObjectNotFoundException e) {
            throw new AssertionError("Could not find day " + currentDay, e);
        }

        // Click on the OK button in the date picker
        onView(withText("OK")).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(hasErrorText("Description is required")));
    }

    @Test
    public void testAddingExpense_OnlyEndDate() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAddExp)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(isDisplayed()));

        // Click on the start date
        onView(withId(R.id.btnSelectEndDate)).perform(click());

        // Click the current date in the calendar grid
        int currentDay = java.time.LocalDate.now().getDayOfMonth();
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject dayButton = device.findObject(new UiSelector()
                .text(String.valueOf(currentDay))
                .className("android.widget.TextView"));
        try {
            dayButton.click();
        } catch (UiObjectNotFoundException e) {
            throw new AssertionError("Could not find day " + currentDay, e);
        }

        // Click on the OK button in the date picker
        onView(withText("OK")).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(hasErrorText("Description is required")));
    }

    @Test
    public void testAddingExpense_OnlyRepeatType() {
        // Click on the first job in the list
        onView(withId(R.id.jobRecyclerView)).perform(actionOnItemAtPosition(0, click()));

        // Click on the add event button
        onView(withId(R.id.fabAddExp)).perform(click());

        // Check if the add event dialog is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(isDisplayed()));

        // Click on the daily repeat radio button
        onView(withId(R.id.radioDaily)).perform(click());

        // Click on the add button
        onView(withText("Add")).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editExpenseDescription)).check(matches(hasErrorText("Description is required")));
    }

}
