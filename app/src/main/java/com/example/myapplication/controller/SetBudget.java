package com.example.myapplication.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetBudget extends AppCompatActivity {

    EditText shoppingBudget, foodBudget, entertainmentBudget, travelingBudget;
    TextView addBudget;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Correct the IDs
        shoppingBudget = findViewById(R.id.shoppingBudget);
        foodBudget = findViewById(R.id.foodBudget);
        entertainmentBudget = findViewById(R.id.entertainmentBudget);
        travelingBudget = findViewById(R.id.travelingBudget);
        addBudget = findViewById(R.id.addBudget);

        FirebaseApp.initializeApp(this);

        addBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBudget();
            }
        });
    }

    private void updateBudget() {
        String food = foodBudget.getText().toString().trim();
        String travel = travelingBudget.getText().toString().trim();
        String entertainment = entertainmentBudget.getText().toString().trim();
        String shopping = shoppingBudget.getText().toString().trim();

        // Ensure values are not empty
        if (food.isEmpty() || travel.isEmpty() || entertainment.isEmpty() || shopping.isEmpty()) {
            Toast.makeText(this, "Please enter all budget values!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Convert to numeric values
            double foodValue = Double.parseDouble(food);
            double travelValue = Double.parseDouble(travel);
            double entertainmentValue = Double.parseDouble(entertainment);
            double shoppingValue = Double.parseDouble(shopping);

            // Create data maps for each category
            Map<String, Object> foodData = new HashMap<>();
            foodData.put("budget", foodValue);
            foodData.put("totalExpenses", 0.0);

            Map<String, Object> travelData = new HashMap<>();
            travelData.put("budget", travelValue);
            travelData.put("totalExpenses", 0.0);

            Map<String, Object> entertainmentData = new HashMap<>();
            entertainmentData.put("budget", entertainmentValue);
            entertainmentData.put("totalExpenses", 0.0);

            Map<String, Object> shoppingData = new HashMap<>();
            shoppingData.put("budget", shoppingValue);
            shoppingData.put("totalExpenses", 0.0);

            // Perform Firestore writes and handle success/failure using a helper method
            updateFirestore("Food", foodData);
            updateFirestore("Traveling", travelData);
            updateFirestore("Entertainment", entertainmentData);
            updateFirestore("Shopping", shoppingData);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input. Please enter valid numbers.", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to update Firestore documents
    private void updateFirestore(String category, Map<String, Object> categoryData) {
        db.collection("Budgy").document(category)
                .set(categoryData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SetBudget.this, category + " budget updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SetBudget.this, "Failed to update " + category + " budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
