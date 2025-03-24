package com.example.myapplication.controller;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetBudget extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Map<String, EditText> budgetFields = new HashMap<>();
    private TextView addToBudget, resetBudget;

    private EditText foodBudget, travelingBudget, entertainmentBudget, shoppingBudget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Initialize fields before using them
        foodBudget = findViewById(R.id.foodBudget);
        travelingBudget = findViewById(R.id.travelingBudget);
        entertainmentBudget = findViewById(R.id.entertainmentBudget);
        shoppingBudget = findViewById(R.id.shoppingBudget);

        budgetFields.put("Food", findViewById(R.id.foodBudget));
        budgetFields.put("Traveling", findViewById(R.id.travelingBudget));
        budgetFields.put("Entertainment", findViewById(R.id.entertainmentBudget));
        budgetFields.put("Shopping", findViewById(R.id.shoppingBudget));

        // Get the selected category from intent
        String category = getIntent().getStringExtra("BUDGET_CATEGORY");

        if (category != null) {
            showOnlySelectedCategory(category);
        }


        addToBudget = findViewById(R.id.addToBudget);
        resetBudget = findViewById(R.id.resetBudget);

        addToBudget.setOnClickListener(v -> modifyBudget(true));  // Add to current budget
        resetBudget.setOnClickListener(v -> modifyBudget(false)); // Reset budget


    }

    private void modifyBudget(boolean isAddition) {
        for (Map.Entry<String, EditText> entry : budgetFields.entrySet()) {
            EditText field = entry.getValue();

            // Skip hidden fields
            if (field.getVisibility() != View.VISIBLE) {
                continue;
            }

            String category = entry.getKey();
            String input = field.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a budget value for " + category, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double budgetValue = Double.parseDouble(input);
                if (isAddition) {
                    addToExistingBudget(category, budgetValue);
                } else {
                    resetBudget(category, budgetValue);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input for " + category + ". Enter a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    // Adds the new budget value to the existing budget
    private void addToExistingBudget(String category, double newAmount) {
        db.collection("Budgy").document(category)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double currentBudget = documentSnapshot.getDouble("budget");
                        double updatedBudget = currentBudget + newAmount;

                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("budget", updatedBudget);

                        db.collection("Budgy").document(category)
                                .update(updatedData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SetBudget.this, category + " budget updated!", Toast.LENGTH_SHORT).show();
                                    goToMainMenu();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SetBudget.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(SetBudget.this, "Failed to fetch current budget: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Resets the budget to the new value
    private void resetBudget(String category, double newValue) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("budget", newValue);
        categoryData.put("totalExpenses", 0.0);

        db.collection("Budgy").document(category)
                .set(categoryData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SetBudget.this, category + " budget reset!", Toast.LENGTH_SHORT).show();
                    goToMainMenu();
                })
                .addOnFailureListener(e -> Toast.makeText(SetBudget.this, "Failed to reset " + category + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void goToMainMenu() {
        Intent intent = new Intent(SetBudget.this, BudgetMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showOnlySelectedCategory(String category) {
        // Hide all fields first

        foodBudget.setVisibility(View.GONE);
        travelingBudget.setVisibility(View.GONE);
        entertainmentBudget.setVisibility(View.GONE);
        shoppingBudget.setVisibility(View.GONE);

        // Show only the selected category
        switch (category) {
            case "Food":
                foodBudget.setVisibility(View.VISIBLE);
                break;
            case "Traveling":
                travelingBudget.setVisibility(View.VISIBLE);
                break;
            case "Entertainment":
                entertainmentBudget.setVisibility(View.VISIBLE);
                break;
            case "Shopping":
                shoppingBudget.setVisibility(View.VISIBLE);
                break;
        }
    }
}
