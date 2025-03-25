package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import android.graphics.Color;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;


public class BudgetMainActivity extends AppCompatActivity {

    TextView mainBalance;

    Button traveling, food, shopping, entertainment, updateBudget, financialSummary;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    PieChart pieChart;

    long totalBudget = 0, totalExpenses = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budgetmain);


        pieChart = findViewById(R.id.pieMainChart);
        getBudgetData();


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


    }


    @Override
    protected void onResume() {
        //showData();
        super.onResume();
    }

    //setting up the pie chart
    private void getBudgetData() {
        db.collection("Budgy").get().addOnSuccessListener(queryDocumentSnapshots -> {
            totalBudget = 0;
            totalExpenses = 0;

            List<PieEntry> pieEntries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            // Category color mapping
            Map<String, Integer> categoryColors = new HashMap<>();
            categoryColors.put("Shopping", getResources().getColor(R.color.purple_700));
            categoryColors.put("Food", getResources().getColor(R.color.hot_pink));
            categoryColors.put("Traveling", getResources().getColor(R.color.orange));
            categoryColors.put("Entertainment", getResources().getColor(R.color.purple_200));

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                if (doc.contains("budget") && doc.contains("totalExpenses")) {
                    long budget = doc.getLong("budget");
                    long expenses = doc.getLong("totalExpenses");
                    totalBudget += budget;
                    totalExpenses += expenses;

                    String category = doc.getId(); // Get category name from Firestore doc ID

                    if (expenses > 0 && categoryColors.containsKey(category)) {
                        pieEntries.add(new PieEntry(expenses, category)); // Add category label
                        colors.add(categoryColors.get(category)); // Assign color
                    }
                }
            }

            // Add remaining budget as a separate category
            if (totalBudget - totalExpenses > 0) {
                pieEntries.add(new PieEntry(totalBudget - totalExpenses, "Remaining"));
                colors.add(getResources().getColor(R.color.teal_700));
            }

            setUpGraph(pieEntries, colors);
        });
    }

    private void setUpGraph(List<PieEntry> pieEntries, List<Integer> colors) {
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Budget Overview");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(14f);

        // Improve visibility of small slices
        pieDataSet.setSliceSpace(3f); // Add spacing between slices
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); // Move small labels outside
        pieDataSet.setValueLinePart1Length(0.8f);
        pieDataSet.setValueLinePart2Length(0.8f);
        pieDataSet.setValueLineColor(Color.BLACK); // Ensure label lines are visible

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart)); // Ensure small values are formatted properly

        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false); // Disable direct labels if they overlap
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.getDescription().setEnabled(false); // Hide extra description text
        pieChart.setUsePercentValues(true);
        pieChart.setExtraOffsets(15, 15, 10, 15); // Adjust offsets for better spacing
        pieChart.getLegend().setEnabled(true); // Enable legend for better readability
        pieChart.invalidate(); // Refresh chart
    }



}

