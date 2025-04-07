package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SetBudget extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Map<String, EditText> budgetFields = new HashMap<>();
    private TextView addToBudget, resetBudget;
    private EditText foodBudget, travelingBudget, entertainmentBudget, shoppingBudget;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        foodBudget = findViewById(R.id.foodBudget);
        travelingBudget = findViewById(R.id.travelingBudget);
        entertainmentBudget = findViewById(R.id.entertainmentBudget);
        shoppingBudget = findViewById(R.id.shoppingBudget);

        budgetFields.put("food", foodBudget);
        budgetFields.put("traveling", travelingBudget);
        budgetFields.put("entertainment", entertainmentBudget);
        budgetFields.put("shopping", shoppingBudget);

        // Retrieve user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String category = getIntent().getStringExtra("BUDGET_CATEGORY");
        if (category != null) {
            showOnlySelectedCategory(category);
        }

        addToBudget = findViewById(R.id.addToBudget);
        resetBudget = findViewById(R.id.resetBudget);

        addToBudget.setOnClickListener(v -> modifyBudget(true));
        resetBudget.setOnClickListener(v -> modifyBudget(false));
    }

    private void modifyBudget(boolean isAddition) {
        for (Map.Entry<String, EditText> entry : budgetFields.entrySet()) {
            EditText field = entry.getValue();

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
            }
        }
    }

    private void addToExistingBudget(String category, double newAmount) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            double currentBudget = 0.0; // Default if missing


            if (documentSnapshot.exists() && documentSnapshot.contains(category)) {
                Map<String, Object> categoryData = (Map<String, Object>) documentSnapshot.get(category);
                if (categoryData != null && categoryData.containsKey("budgetAmount")) {
                    currentBudget = ((Number) categoryData.get("budgetAmount")).doubleValue();
                }
            }

            double updatedBudget = currentBudget + newAmount;

            Map<String, Object> newCategoryData = new HashMap<>();
            newCategoryData.put("budgetAmount", updatedBudget);

            docRef.set(new HashMap<String, Object>() {{
                        put(category, newCategoryData);
                    }}, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SetBudget.this, category + " budget updated!", Toast.LENGTH_SHORT).show();
                        goToMainMenu();
                    })
                    .addOnFailureListener(e -> Toast.makeText(SetBudget.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void resetBudget(String category, double newValue) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);

        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("budgetAmount", newValue);
        categoryData.put("totalExpenses", 0.0);

        docRef.set(new HashMap<String, Object>() {{
                    put(category, categoryData);
                }}, SetOptions.merge())
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
        foodBudget.setVisibility(View.GONE);
        travelingBudget.setVisibility(View.GONE);
        entertainmentBudget.setVisibility(View.GONE);
        shoppingBudget.setVisibility(View.GONE);

        switch (category) {
            case "food":
                foodBudget.setVisibility(View.VISIBLE);
                break;
            case "travel":
                travelingBudget.setVisibility(View.VISIBLE);
                break;
            case "entertainment":
                entertainmentBudget.setVisibility(View.VISIBLE);
                break;
            case "shopping":
                shoppingBudget.setVisibility(View.VISIBLE);
                break;
        }
    }
}
