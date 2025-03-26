package com.example.myapplication.model;

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
import com.example.myapplication.controller.LoginActivity;
import com.example.myapplication.controller.SetBudget;
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

public class Shopping extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> shoppingExpenses;
    private TextView addBudget, mainBalanceText;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ShoppingActivity";

    private ProgressDialog progressDialog;
private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        recyclerView = findViewById(R.id.recyclerView);
        // totalFoodExpense = findViewById(R.id.totalFoodExpense);
        //mainBalanceText = findViewById(R.id.mainBalance);
        pieChart = findViewById(R.id.pieShoppingChart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        shoppingExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter((ArrayList<EXP>) shoppingExpenses, null);
        recyclerView.setAdapter(adapter);

        // Retrieve user ID from SharedPreferences
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
        progressDialog.setMessage("Loading Shopping Data...");
        progressDialog.setCancelable(false);

        addBudget = findViewById(R.id.addBudget);

        addBudget.setOnClickListener(v -> openSetBudget("shopping"));



        loadShoppingExpenses();
        showBudgetAndPieChart();
    }


    private void openSetBudget(String category) {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }
    private void loadShoppingExpenses() {
        progressDialog.show();
        db.collection("Jobs").whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalShoppingExpenseAmount = {0.0};
                        shoppingExpenses.clear();
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", "Shopping")
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
                                                    shoppingExpenses.add(expense);
                                                    totalShoppingExpenseAmount[0] += expense.getAmount();
                                                }
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    updateBudgetTotal(totalShoppingExpenseAmount[0]);
                                    progressDialog.dismiss();
                                });
                    }
                });
    }


    private void updateBudgetTotal(double totalExp) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null && budgetData.containsKey("shopping")) {
                    Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("shopping");
                    foodCategory.put("totalExpenses", totalExp);

                    docRef.update("shopping", foodCategory)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Shopping expenses updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update shopping expenses", e));
                }
            }
        });
    }

    private void showBudgetAndPieChart() {
        db.collection("budget").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> budgetData = documentSnapshot.getData();
                        if (budgetData != null && budgetData.containsKey("shopping")) {
                            Map<String, Object> foodCategory = (Map<String, Object>) budgetData.get("shopping");
                            Double budget = (Double) foodCategory.get("budgetAmount");
                            Double totalExp = (Double) foodCategory.get("totalExpenses");

                            if (budget != null && totalExp != null) {
                                double remaining = budget - totalExp;
                                updatePieChart(totalExp, remaining);
                            } else {
                                Log.e(TAG, "Budget or totalExpenses for shopping is null");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching shopping budget data", e));
    }


    private void updatePieChart(double spent, double remaining) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) spent, "Spent"));
        entries.add(new PieEntry((float) remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "\t \t \t Shopping Budget Breakdown");
        dataSet.setColors(Color.RED,Color.parseColor("#2E9797"));
        dataSet.setValueTextSize(18f);

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
