package com.example.myapplication.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.example.myapplication.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SharingActivityTest {

    private ActivityScenario<SharingMainActivity> scenario;

    private void waitForProgressDialogToDisappear(String dialogText) {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.wait(Until.gone(By.text(dialogText)), 5000); // 5 second timeout
    }

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
        scenario = ActivityScenario.launch(SharingMainActivity.class);
    }

    @Test
    public void testSharingActivityLaunch() {
        // Check if the sharing activity is displayed
        onView(withId(R.id.sharingTitleText)).check(matches(isDisplayed()));
    }

    @Test
    public void testSharingActivity_UI() {
        // Check if the sharing activity is displayed
        onView(withId(R.id.sharingTitleText)).check(matches(isDisplayed()));

        // Check if the buttons exist
        onView(withId(R.id.joinbutton)).check(matches(isDisplayed()));
        onView(withId(R.id.createbutton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSharingActivity_RecyclerView() {
        // Check if the sharing activity is displayed
        onView(withId(R.id.sharingTitleText)).check(matches(isDisplayed()));

        // Check if the RecyclerView is displayed
        onView(withId(R.id.sharedRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void testSharingActivity_JoinButton() {
        // Check if the sharing activity is displayed
        onView(withId(R.id.sharingTitleText)).check(matches(isDisplayed()));

        // Click the join button
        onView(withId(R.id.joinbutton)).perform(click());

        // Check if the join activity elements are displayed
        onView(withId(R.id.textView3)).check(matches(isDisplayed()));
        onView(withId(R.id.codeInput)).check(matches(isDisplayed()));
        onView(withId(R.id.join)).check(matches(isDisplayed()));
    }

    @Test
    public void testSharingActivity_CreateButton() {
        // Check if the sharing activity is displayed
        onView(withId(R.id.sharingTitleText)).check(matches(isDisplayed()));

        // Click on the create button
        onView(withId(R.id.createbutton)).perform(click());

        // Check if the create activity elements are displayed
        onView(withId(R.id.textView)).check(matches(isDisplayed()));
        onView(withId(R.id.nameInput)).check(matches(isDisplayed()));
        onView(withId(R.id.textView2)).check(matches(isDisplayed()));
        onView(withId(R.id.button2)).check(matches(isDisplayed()));
    }

    @Test
    public void testSharingActivity_CreateButton_RecyclerView() {
        // Check if the sharing activity is displayed
        onView(withId(R.id.sharingTitleText)).check(matches(isDisplayed()));

        // Click on the create button
        onView(withId(R.id.createbutton)).perform(click());

        // Check if the create activity elements are displayed
        onView(withId(R.id.textView)).check(matches(isDisplayed()));
        onView(withId(R.id.nameInput)).check(matches(isDisplayed()));
        onView(withId(R.id.textView2)).check(matches(isDisplayed()));
        onView(withId(R.id.button2)).check(matches(isDisplayed()));


        // Check if the RecyclerView is displayed
        onView(withId(R.id.sharedEventRecycle)).check(matches(isDisplayed()));
    }

}
