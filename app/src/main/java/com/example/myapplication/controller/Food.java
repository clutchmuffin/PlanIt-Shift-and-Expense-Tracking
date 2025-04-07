package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.EXP;
import com.example.myapplication.view.adapter.ExpenseListAdapter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Food extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> foodExpenses;
    private TextView addBudget;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FoodActivity";

    private ProgressDialog progressDialog;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        recyclerView = findViewById(R.id.recyclerView);
        pieChart = findViewById(R.id.pieFoodChart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter((ArrayList<EXP>) foodExpenses, null);
        recyclerView.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading Food Data...");
        progressDialog.setCancelable(false);

        addBudget = findViewById(R.id.addBudget);
        addBudget.setOnClickListener(v -> openSetBudget("food"));

        loadFoodExpenses();
        showBudgetAndPieChart();
    }

    private void openSetBudget(String category) {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }
    private void loadFoodExpenses() {
        progressDialog.show();
        db.collection("Jobs").whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalFoodExpenseAmount = {0.0};
                        foodExpenses.clear();
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", "Food")
                                    .get();
                            expenseFetchTasks.add(expenseTask);
                        }

                        Tasks.whenAllComplete(expenseFetchTasks)
                                .addOnCompleteListener(allTask -> {
                                    for (Task<QuerySnapshot> expenseTask : expenseFetchTasks) {
                                        if (expenseTask.isSuccessful()) {
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                Log.d(TAG, "Found food expense: " + expenseDocument.getData()); // DEBUG
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    foodExpenses.add(expense);
                                                    totalFoodExpenseAmount[0] += expense.calculateExpenseDetails().get(1);
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "Failed to fetch food expenses", expenseTask.getException());
                                        }
                                    }

                                    // Check if foodExpenses is empty and show a Toast if it is
                                    if (foodExpenses.isEmpty()) {
                                        Toast.makeText(Food.this, "No food expenses found", Toast.LENGTH_SHORT).show();
                                    }

                                    Log.d(TAG, "Total food expenses amount: " + totalFoodExpenseAmount[0]); // DEBUG
                                    Log.d(TAG, "Total food expenses count: " + foodExpenses.size()); // DEBUG

                                    adapter.notifyDataSetChanged();
                                    updateBudgetTotal(totalFoodExpenseAmount[0]);
                                });
                    } else {
                        Log.e(TAG, "Failed to get Jobs documents", task.getException());
                    }
                });
    }



    private void updateBudgetTotal(double totalExp) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null && budgetData.containsKey("food")) {
                    Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("food");
                    Log.d(TAG, "Before update, foodCategory: " + foodCategory); // DEBUG

                    foodCategory.put("totalExpenses", totalExp * 1.0); // Ensure it's a Double

                    docRef.update("food", foodCategory)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Food expenses updated successfully: " + totalExp)) // DEBUG
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update food expenses", e));
                } else {
                    Log.e(TAG, "No 'food' key found in budget document");
                }
            }
        });
    }


    private void showBudgetAndPieChart() {
        db.collection("budget").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> budgetData = documentSnapshot.getData();
                        if (budgetData != null && budgetData.containsKey("food")) {
                            Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("food");

                            Log.d(TAG, "Food budget data from Firestore: " + foodCategory); // DEBUG

                            Number budgetRaw = (Number) foodCategory.get("budgetAmount");
                            Number totalExpRaw = (Number) foodCategory.get("totalExpenses");

                            if (budgetRaw != null && totalExpRaw != null) {
                                double budget = budgetRaw.doubleValue();
                                double totalExp = totalExpRaw.doubleValue();
                                double remaining = budget - totalExp;

                                Log.d(TAG, "Parsed budget: " + budget + ", totalExpenses: " + totalExp + ", remaining: " + remaining); // DEBUG

                                updatePieChart(totalExp, remaining);
                            } else {
                                Log.e(TAG, "Budget or totalExpenses for food is null");
                            }
                        } else {
                            Log.e(TAG, "No 'food' category found in budget data");
                        }
                    }
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching food budget data", e);
                    progressDialog.dismiss();
                });
    }


    private void updatePieChart(double spent, double remaining) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) spent, "Spent"));
        entries.add(new PieEntry((float) remaining, "Remaining"));
    
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.RED, Color.parseColor("#2E9797"));
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);
    
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        
        // Match Financial Summary styling
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        
        pieChart.setCenterText("Food\nBudget\nBreakdown");
        pieChart.setCenterTextSize(14f);
        pieChart.setDrawCenterText(true);
        
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        // Consistent sizing parameters
        pieChart.setExtraOffsets(10f, 10f, 10f, 10f);
        pieChart.setMinimumHeight(500);
        pieChart.setMinimumWidth(500);
    
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
    
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}
