package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

public class Traveling extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> travelingExpenses;
    private TextView addIncome, mainBalanceText;
    private PieChart pieChart;
    private String currentUserId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "TravelingActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveling);

        recyclerView = findViewById(R.id.recyclerView);
        // totalFoodExpense = findViewById(R.id.totalFoodExpense);
        //mainBalanceText = findViewById(R.id.mainBalance);
        pieChart = findViewById(R.id.pieTravelingChart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        travelingExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter((ArrayList<EXP>) travelingExpenses, null);
        recyclerView.setAdapter(adapter);

SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


// Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading Traveling Data...");
        progressDialog.setCancelable(false);


        addIncome = findViewById(R.id.addIncome);

        addIncome.setOnClickListener(v -> openSetBudget("traveling"));



        loadTravelingExpenses();
        showBudgetAndPieChart();
    }

    private void openSetBudget(String category) {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }


    private void loadTravelingExpenses() {
        progressDialog.show();
        db.collection("Jobs").whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalTravelingExpenseAmount = {0.0};
                        travelingExpenses.clear();
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", "Traveling")
                                    .get();
                            expenseFetchTasks.add(expenseTask);
                        }

                        Tasks.whenAllComplete(expenseFetchTasks)
                                .addOnCompleteListener(allTask -> {
                                    boolean hasExpenses = false;
                                    for (Task<QuerySnapshot> expenseTask : expenseFetchTasks) {
                                        if (expenseTask.isSuccessful()) {
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    travelingExpenses.add(expense);
                                                    totalTravelingExpenseAmount[0] += expense.getAmount();
                                                    hasExpenses = true;
                                                }
                                            }
                                        }
                                    }

                                    // If no expenses were found, ensure totalExpenses is set to 0
                                    if (!hasExpenses) {
                                        totalTravelingExpenseAmount[0] = 0.0;
                                    }

                                    adapter.notifyDataSetChanged();
                                    updateBudgetTotal(totalTravelingExpenseAmount[0]);

                                });
                    }
                });
    }


    private void updateBudgetTotal(double totalExp) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null && budgetData.containsKey("traveling")) {
                    Map<String, Object> travelingCategory = (Map<String, Object>) budgetData.get("traveling");
                    travelingCategory.put("totalExpenses", totalExp);

                    docRef.update("traveling", travelingCategory)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Traveling expenses updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update traveling expenses", e));
                }
            }
        });
    }

    private void showBudgetAndPieChart() {
        db.collection("budget").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> budgetData = documentSnapshot.getData();
                        if (budgetData != null && budgetData.containsKey("traveling")) {
                            Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("traveling");
                            Double budget = (Double) foodCategory.get("budgetAmount");
                            Double totalExp = (Double) foodCategory.get("totalExpenses");

                            if (budget != null && totalExp != null) {
                                double remaining = budget - totalExp;
                                updatePieChart(totalExp, remaining);
                            } else {
                                Log.e(TAG, "Budget or totalExpenses for traveling is null");
                            }
                        }
                    } progressDialog.dismiss(); // Dismiss after data is fetched
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching traveling budget data", e));
        progressDialog.dismiss(); // Dismiss after data is fetched
    }

    private void updatePieChart(double spent, double remaining) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) spent, "Spent"));
        entries.add(new PieEntry((float) remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "Traveling Budget Breakdown");

        dataSet.setColors(Color.RED, Color.parseColor("#2E9797"));
        dataSet.setValueTextSize(17f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setUsePercentValues(false);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        pieChart.invalidate(); // Refresh the chart
    }
}
