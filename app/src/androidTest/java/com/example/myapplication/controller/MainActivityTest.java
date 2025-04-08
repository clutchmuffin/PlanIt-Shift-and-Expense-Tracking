package com.example.myapplication.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.myapplication.R;

import org.junit.Before;
import org.junit.Test;


public class MainActivityTest {

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
    public void testMainActivityLaunch() {
        // Check if the main activity is displayed
        onView(withId(R.id.userAvatar)).check(matches(isDisplayed()));
    }

    @Test
    public void checkRecyclerViewExists() {
        // Check if the RecyclerView is displayed
        onView(withId(R.id.jobRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddingJob_NoData() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Click on the add button without entering any data
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editJobTitle)).check(matches(hasErrorText("Title is required")));
    }

    @Test
    public void testAddingJob_OnlyTitle() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Enter only the job title
        onView(withId(R.id.editJobTitle))
                .perform(typeText("Espresso Test"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the recycler view has the new job
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject jobItem = device.findObject(new UiSelector().text("Espresso Test"));
        if (!jobItem.exists()) {
            throw new AssertionError("Job item not found in RecyclerView");
        }
    }

    @Test
    public void testAddingJob_OnlyJobSubtitle() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Add only the job subtitle
        onView(withId(R.id.editJobSubtitle))
                .perform(typeText("Espresso Test Subtitle"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editJobTitle)).check(matches(hasErrorText("Title is required")));
    }

    @Test
    public void testAddingJob_OnlyEmployer() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Add only the job employer
        onView(withId(R.id.editJobEmployer))
                .perform(typeText("Espresso Test Employer"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editJobTitle)).check(matches(hasErrorText("Title is required")));
    }

    @Test
    public void testAddingJob_OnlyLocation() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Add only the job location
        onView(withId(R.id.editJobLocation))
                .perform(typeText("Espresso Test Location"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editJobTitle)).check(matches(hasErrorText("Title is required")));
    }

    @Test
    public void testAddingJob_OnlyPayRate() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Add only the job pay rate
        onView(withId(R.id.editPayRate))
                .perform(typeText("20"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editJobTitle)).check(matches(hasErrorText("Title is required")));
    }

    @Test
    public void testAddingJob_OnlyColor() {
        // Click on the add job button
        onView(withId(R.id.fabAddJob)).perform(click());

        // Check if the add job dialog is displayed
        onView(withId(R.id.editJobTitle)).check(matches(isDisplayed()));

        // Add only the job color
        onView(withId(R.id.editJobColor))
                .perform(typeText("#6200EE"), closeSoftKeyboard());

        // Click on the add button
        onView(withText("Add")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        // Check if the error message is displayed
        onView(withId(R.id.editJobTitle)).check(matches(hasErrorText("Title is required")));
    }

}
