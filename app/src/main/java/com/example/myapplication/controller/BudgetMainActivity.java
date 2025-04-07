package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetMainActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PieChart pieChart;
    private ProgressDialog progressDialog;
    private String currentUserId;

    private Button food, shopping, entertainment, traveling, updateBudget, financialSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budgetmain);

        // Initialize UI
        pieChart = findViewById(R.id.pieMainChart);
        food = findViewById(R.id.food);
        shopping = findViewById(R.id.shopping);
        entertainment = findViewById(R.id.entertainment);
        traveling = findViewById(R.id.traveling);
        updateBudget = findViewById(R.id.updateBudget);
        financialSummary = findViewById(R.id.financialSummary);

        progressDialog = new ProgressDialog(this);

        // Retrieve user ID
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            Log.e("BudgetMainActivity", "Firestore access failed: userId is null");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set click listeners
        food.setOnClickListener(v -> startActivity(new Intent(this, Food.class)));
        shopping.setOnClickListener(v -> startActivity(new Intent(this, Shopping.class)));
        entertainment.setOnClickListener(v -> startActivity(new Intent(this, Entertainment.class)));
        traveling.setOnClickListener(v -> startActivity(new Intent(this, Traveling.class)));
        updateBudget.setOnClickListener(v -> startActivity(new Intent(this, SetBudget.class)));
        financialSummary.setOnClickListener(v -> startActivity(new Intent(this, JobSummaryActivity.class)));

        // Load pie chart
        getBudgetData(currentUserId);
    }

    private void getBudgetData(String userId) {
        progressDialog.setMessage("Updating budget...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DocumentReference userBudgetRef = db.collection("budget").document(userId);

        userBudgetRef.get().addOnSuccessListener(documentSnapshot -> {
            progressDialog.dismiss();

            if (!documentSnapshot.exists()) {
                Toast.makeText(this, "No budget data found", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> budgetData = documentSnapshot.getData();
            if (budgetData == null) return;

            int totalBudget = 0;
            int totalExpenses = 0;
            List<PieEntry> pieEntries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            for (Map.Entry<String, Object> entry : budgetData.entrySet()) {
                if ("financialSummary".equals(entry.getKey())) continue;

                if (entry.getValue() instanceof Map) {
                    Map<String, Object> categoryMap = (Map<String, Object>) entry.getValue();

                    int budgetAmount = getIntFromMap(categoryMap, "budgetAmount");
                    int expenses = getIntFromMap(categoryMap, "totalExpenses");

                    totalBudget += budgetAmount;
                    totalExpenses += expenses;

                    if (expenses > 0) {
                        pieEntries.add(new PieEntry(expenses, entry.getKey()));
                        colors.add(getCategoryColor(entry.getKey()));
                    }
                }
            }

            int remainingBudget = totalBudget - totalExpenses;
            if (remainingBudget > 0) {
                pieEntries.add(new PieEntry(remainingBudget, "Remaining Budget"));
                colors.add(Color.GRAY);
            }

            updatePieChart(pieEntries, colors, totalExpenses, remainingBudget);

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updatePieChart(List<PieEntry> entries, List<Integer> colors, int totalExpenses, int remainingBudget) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);
    
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
    
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Budget Overview\n Used: $" + totalExpenses + "\nLeft: $" + remainingBudget);
        pieChart.setCenterTextSize(14f);
        
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        // Consistent sizing parameters
        pieChart.setExtraOffsets(10f, 10f, 10f, 10f);
        pieChart.setMinimumHeight(500);
        pieChart.setMinimumWidth(500);
        
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelTextSize(12f);
        
        // Set legend properties
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private int getIntFromMap(Map<String, Object> map, String key) {
        return map.containsKey(key) ? ((Number) map.get(key)).intValue() : 0;
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
                return Color.LTGRAY;
        }
    }
}
