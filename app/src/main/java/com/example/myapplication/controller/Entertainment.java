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

public class Entertainment extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> entertainmentExpenses;
    private TextView addBudget;
    private PieChart pieChart;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EntertainmentActivity";

    private ProgressDialog progressDialog;
    private String currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entertainment);

        recyclerView = findViewById(R.id.recyclerView);
        pieChart = findViewById(R.id.pieEntertainmentChart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        entertainmentExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter(entertainmentExpenses, null);
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
        progressDialog.setMessage("Loading Entertainment Data...");
        progressDialog.setCancelable(false);

        addBudget = findViewById(R.id.addIncome);
        addBudget.setOnClickListener(v -> openSetBudget("entertainment"));
    }

    // Refresh data when user returns to screen
    @Override
    protected void onResume() {
        super.onResume();
        progressDialog.show();
        loadEntertainmentExpenses();
    }

    private void openSetBudget(String category) {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }

    private void loadEntertainmentExpenses() {
        db.collection("Jobs").whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalEntertainmentExpenseAmount = {0.0};
                        entertainmentExpenses.clear();
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", "Entertainment")
                                    .get();
                            expenseFetchTasks.add(expenseTask);
                        }

                        Tasks.whenAllComplete(expenseFetchTasks)
                                .addOnCompleteListener(allTask -> {
                                    for (Task<QuerySnapshot> expenseTask : expenseFetchTasks) {
                                        if (expenseTask.isSuccessful()) {
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    entertainmentExpenses.add(expense);
                                                    totalEntertainmentExpenseAmount[0] += expense.calculateExpenseDetails().get(1);
                                                }
                                            }
                                        }
                                    }

                                    adapter.notifyDataSetChanged();
                                    updateBudgetTotal(totalEntertainmentExpenseAmount[0]);
                                });
                    } else {
                        Log.e(TAG, "Failed to get Jobs documents", task.getException());
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateBudgetTotal(double totalExp) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null && budgetData.containsKey("entertainment")) {
                    Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("entertainment");
                    foodCategory.put("totalExpenses", totalExp);

                    docRef.update("entertainment", foodCategory)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Food expenses updated successfully");
                                showBudgetAndPieChart(); // Refresh chart after update
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update entertainment expenses", e);
                                progressDialog.dismiss();
                            });
                }
            } else {
                progressDialog.dismiss();
            }
        });
    }

    private void showBudgetAndPieChart() {
        db.collection("budget").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> budgetData = documentSnapshot.getData();
                        if (budgetData != null && budgetData.containsKey("entertainment")) {
                            Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("entertainment");

                            Number budgetRaw = (Number) foodCategory.get("budgetAmount");
                            Number totalExpRaw = (Number) foodCategory.get("totalExpenses");

                            if (budgetRaw != null && totalExpRaw != null) {
                                double budget = budgetRaw.doubleValue();
                                double totalExp = totalExpRaw.doubleValue();
                                double remaining = budget - totalExp;
                                updatePieChart(totalExp, remaining);
                            } else {
                                Log.e(TAG, "Budget or totalExpenses for entertainment is null");
                            }
                        }
                    }
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching travel budget data", e);
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

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("Entertainment\nBudget\nBreakdown");
        pieChart.setCenterTextSize(14f);
        pieChart.setDrawCenterText(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
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
