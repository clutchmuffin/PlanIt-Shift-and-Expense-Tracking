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
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.myapplication.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LoginActivityTest {

    private ActivityScenario<LoginActivity> scenario;

    @Before
    public void setup() {
        // Clear shared preferences
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("PlanITPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Close any active activities and launch a fresh LoginActivity for each test
        if (scenario != null) {
            scenario.close();
        }
        scenario = ActivityScenario.launch(LoginActivity.class);
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testLogin_onlyFullName() {
        onView(withId(R.id.nameEditText))
                .perform(typeText("FirstUser"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Check if the email field and username field show error
        onView(withId(R.id.usernameEditText))
                .check(matches(hasErrorText("Username is required")));
    }

    @Test
    public void testLogin_onlyUserName() {
        onView(withId(R.id.usernameEditText))
                .perform(typeText("FirstUserName"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Check if the full name field shows error
        onView(withId(R.id.nameEditText))
                .check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testLogin_onlyEmail() {
        onView(withId(R.id.emailEditText))
                .perform(typeText("user@domain.com"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Check if the full name field shows error
        onView(withId(R.id.nameEditText))
                .check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void testProperLogin() throws UiObjectNotFoundException, InterruptedException {
        onView(withId(R.id.nameEditText))
                .perform(typeText("FirstUser"), closeSoftKeyboard());
        onView(withId(R.id.usernameEditText))
                .perform(typeText("FirstUserName"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText))
                .perform(typeText("user@domain.com"), closeSoftKeyboard());

        // Perform the login action
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);

        // Handle notification permission dialog with UiAutomator
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject allowButton = device.findObject(new UiSelector()
                .text("Allow")  // or "Yes" or whatever the button text is
                .className("android.widget.Button"));

        if (allowButton.exists()) {
            allowButton.click();
        }

        // Check if the MainActivity is displayed by checking for any view in it
        onView(withId(R.id.jobRecyclerView)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        // Close the activity after each test
        activityRule.getScenario().close();
    }
}
