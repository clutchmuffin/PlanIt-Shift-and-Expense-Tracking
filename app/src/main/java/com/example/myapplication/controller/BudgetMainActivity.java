package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import android.graphics.Color;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Food;
import com.example.myapplication.R;
import com.example.myapplication.model.Shopping;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.SetOptions;


public class BudgetMainActivity extends AppCompatActivity {

    TextView mainBalance;

    Button traveling, food, shopping, entertainment, updateBudget, financialSummary;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    PieChart pieChart;

    long totalBudget = 0, totalExpenses = 0;
    private String currentUserId;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budgetmain);


        // Retrieve user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            Log.e("BudgetMainActivity", "Firestore access failed: userId is null");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        pieChart = findViewById(R.id.pieMainChart);



        // Button Click Listeners
        food = findViewById(R.id.food);
        shopping = findViewById(R.id.shopping);
        entertainment = findViewById(R.id.entertainment);
        traveling = findViewById(R.id.traveling);

        food.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Food.class)));
        shopping.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Shopping.class)));
        entertainment.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Entertainment.class)));
        traveling.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Traveling.class)));
        updateBudget = findViewById(R.id.updateBudget);
        financialSummary = findViewById(R.id.financialSummary);

        progressDialog = new ProgressDialog(this);

        updateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BudgetMainActivity.this,SetBudget.class));
            }
        });

        financialSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BudgetMainActivity.this,FinancialSummary.class));
            }
        });


        food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Food.class));


            }
        });

        shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Shopping.class));


            }
        });

        entertainment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Entertainment.class));


            }
        });

        traveling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Traveling.class));


            }
        });
        getBudgetData(currentUserId,pieChart);

    }


    @Override
    protected void onResume() {
        //showData();
        super.onResume();
    }

    //setting up the pie chart
    public void getBudgetData(String userId, PieChart pieChart) {

        progressDialog.setMessage("Updating budget...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show the loading dialog


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userBudgetRef = db.collection("budget").document(userId);

        userBudgetRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null) {
                    int totalBudget = 0;
                    int totalExpenses = 0;
                    List<PieEntry> pieEntries = new ArrayList<>();
                    List<Integer> colors = new ArrayList<>();

                    for (Map.Entry<String, Object> entry : budgetData.entrySet()) {
                        if ("financialSummary".equals(entry.getKey())) {
                            continue; // Skip financialSummary field
                        }
                        if (entry.getValue() instanceof Map) {
                            Map<String, Object> categoryMap = (Map<String, Object>) entry.getValue();

                            // Get budgetAmount
                            int budgetAmount = categoryMap.containsKey("budgetAmount") ?
                                    ((Number) categoryMap.get("budgetAmount")).intValue() : 0;
                            totalBudget += budgetAmount;

                            // Ensure totalExpenses exists, else set it to 0
                            if (!categoryMap.containsKey("totalExpenses")) {
                                categoryMap.put("totalExpenses", 0);
                            }

                            // Get totalExpenses
                            int expenses = categoryMap.containsKey("totalExpenses") ?
                                    ((Number) categoryMap.get("totalExpenses")).intValue() : 0;
                            totalExpenses += expenses;

                            // Add category as an individual slice
                            if (expenses > 0) { // Only add categories that have expenses
                                pieEntries.add(new PieEntry(expenses, entry.getKey())); // Category name
                                colors.add(getCategoryColor(entry.getKey())); // Assign a unique color
                            }
                        }
                    }

                    // Calculate remaining budget
                    int remainingBudget = totalBudget - totalExpenses;
                    if (remainingBudget > 0) {
                        pieEntries.add(new PieEntry(remainingBudget, "Remaining Budget"));
                        colors.add(Color.GRAY); // Use gray for the remaining balance
                    }

                    // Update Pie Chart
                    updatePieChart(pieChart, pieEntries, colors);
                }
            } else {
                Toast.makeText(pieChart.getContext(), "No budget data found", Toast.LENGTH_SHORT).show();
            } progressDialog.dismiss(); // Hide the loading di
        }).addOnFailureListener(e -> {
            progressDialog.dismiss(); // Hide the loading dialog on failure
            Toast.makeText(pieChart.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private int getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "shopping":
                return Color.BLUE;
            case "food":
                return Color.RED;
            case "traveling":
                return Color.MAGENTA;
            case "entertainment":
                return Color.GREEN;
            default:
                return Color.LTGRAY; // Default color for unknown categories
        }
    }

    private void updatePieChart(PieChart pieChart, List<PieEntry> entries, List<Integer> colors) {


        PieDataSet dataSet = new PieDataSet(entries, "Budget Overview");
        dataSet.setColors(colors);  // Use dynamic category colors
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Customize chart appearance
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Budget Breakdown");
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        // Refresh the chart
        pieChart.invalidate();
    }


}

