package com.example.myapplication.controller;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.example.myapplication.R;

import org.junit.Before;
import org.junit.Test;

public class BudgetActivityTest {

    private ActivityScenario<BudgetMainActivity> scenario;

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
        scenario = ActivityScenario.launch(BudgetMainActivity.class);
    }

    @Test
    public void testBudgetActivityLaunch() {
        // Check if the budget activity is displayed
        onView(withId(R.id.pieCardView)).check(matches(isDisplayed()));
    }

    @Test
    public void testBudgetActivity_UIElements() {
        // Check if the budget activity is displayed
        onView(withId(R.id.pieCardView)).check(matches(isDisplayed()));

        // Check if the financial summary button is displayed
        onView(withId(R.id.financialSummary)).check(matches(isDisplayed()));

        // Check if the Update Budget button is displayed
        onView(withId(R.id.updateBudget)).check(matches(isDisplayed()));

        // Check if all the category buttons are displayed
        onView(withId(R.id.food)).check(matches(isDisplayed()));
        onView(withId(R.id.traveling)).check(matches(isDisplayed()));
        onView(withId(R.id.shopping)).check(matches(isDisplayed()));
        onView(withId(R.id.entertainment)).check(matches(isDisplayed()));
    }

    @Test
    public void testResetBudget() {

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Updating budget...");

        // Click the Update Budget button
        onView(withId(R.id.updateBudget)).perform(click());

        // Check if the new activity is loaded
        onView(withId(R.id.buyDisplay)).check(matches(isDisplayed()));

        // Check if all the text fields are displayed
        onView(withId(R.id.foodBudget)).check(matches(isDisplayed()));
        onView(withId(R.id.travelingBudget)).check(matches(isDisplayed()));
        onView(withId(R.id.entertainmentBudget)).check(matches(isDisplayed()));
        onView(withId(R.id.shoppingBudget)).check(matches(isDisplayed()));

        // Check if the buttons are displayed
        onView(withId(R.id.addToBudget)).check(matches(isDisplayed()));
        onView(withId(R.id.resetBudget)).check(matches(isDisplayed()));

        // Enter 0 for all categories
        onView(withId(R.id.foodBudget))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.travelingBudget))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.entertainmentBudget))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.shoppingBudget))
                .perform(typeText("0"), closeSoftKeyboard());

        // Click the reset budget button
        onView(withId(R.id.resetBudget)).perform(click());

        // Check if the budget activity is loaded
        onView(withId(R.id.buyDisplay)).check(matches(isDisplayed()));
    }

    @Test
    public void testUpdateBudget() {

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Updating budget...");

        // Reset the budget
        // Click the Update Budget button
        onView(withId(R.id.updateBudget)).perform(click());

        onView(withId(R.id.foodBudget))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.travelingBudget))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.entertainmentBudget))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.shoppingBudget))
                .perform(typeText("0"), closeSoftKeyboard());

        // Click the reset budget button
        onView(withId(R.id.resetBudget)).perform(click());

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Updating budget...");

        // Click the Update Budget button
        onView(withId(R.id.updateBudget)).perform(click());

        // Check if the new activity is loaded
        onView(withId(R.id.buyDisplay)).check(matches(isDisplayed()));

        // Enter 500 for all categories
        onView(withId(R.id.foodBudget))
                .perform(typeText("500"), closeSoftKeyboard());
        onView(withId(R.id.travelingBudget))
                .perform(typeText("500"), closeSoftKeyboard());
        onView(withId(R.id.entertainmentBudget))
                .perform(typeText("500"), closeSoftKeyboard());
        onView(withId(R.id.shoppingBudget))
                .perform(typeText("500"), closeSoftKeyboard());

        onView(withId(R.id.addToBudget)).perform(click());

        // Check if the budget activity is loaded
        onView(withId(R.id.pieCardView)).check(matches(isDisplayed()));
    }

    @Test
    public void testFinancialSummary() {
        // Click the financial summary button
        onView(withId(R.id.financialSummary)).perform(click());

        // Check if the new activity is loaded
        onView(withId(R.id.monthSelectorLayout)).check(matches(isDisplayed()));
    }

    @Test
    public void testFoodButton() {
        // Click the food button
        onView(withId(R.id.food)).perform(click());

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Loading Food Data...");

        // Check if the new activity is loaded
        onView(withId(R.id.pieFoodChart)).check(matches(isDisplayed()));
    }

    @Test
    public void testShoppingButton() {
        // Click the shopping button
        onView(withId(R.id.shopping)).perform(click());

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Loading Shopping Data...");

        // Check if the new activity is loaded
        onView(withId(R.id.pieShoppingChart)).check(matches(isDisplayed()));
    }

    @Test
    public void testEntertainmentButton() {
        // Click the entertainment button
        onView(withId(R.id.entertainment)).perform(click());

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Loading Entertainment Data...");

        // Check if the new activity is loaded
        onView(withId(R.id.pieEntertainmentChart)).check(matches(isDisplayed()));
    }

    @Test
    public void testTravelingButton() {
        // Click the traveling button
        onView(withId(R.id.traveling)).perform(click());

        // Wait for the progress dialog to disappear
        waitForProgressDialogToDisappear("Loading Traveling Data...");

        // Check if the new activity is loaded
        onView(withId(R.id.pieTravelingChart)).check(matches(isDisplayed()));
    }


    
}
