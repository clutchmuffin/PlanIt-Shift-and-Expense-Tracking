package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

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

public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "BUDGET_CATEGORY";

    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> expenses = new ArrayList<>();
    private TextView addBudget;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    private String currentUserId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food); // Same layout for all categories

        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (categoryName == null) {
            finish(); // prevent crash if no category passed
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        pieChart = findViewById(R.id.pieChart);
        addBudget = findViewById(R.id.addBudget);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseListAdapter(expenses, null);
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
        progressDialog.setMessage("Loading " + categoryName + " data...");
        progressDialog.setCancelable(false);

        addBudget.setOnClickListener(v -> openSetBudget());

    }

    @Override
    protected void onResume() {
        super.onResume();
        progressDialog.show();
        loadExpenses();
    }

    private void openSetBudget() {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra(EXTRA_CATEGORY, categoryName);
        startActivity(intent);
    }

    private void loadExpenses() {
        db.collection("Jobs").whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        expenses.clear();
                        final double[] totalExpenseAmount = {0.0};
                        List<Task<QuerySnapshot>> expenseTasks = new ArrayList<>();

                        for (DocumentSnapshot job : task.getResult()) {
                            String jobId = job.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", capitalize(categoryName))
                                    .get();
                            expenseTasks.add(expenseTask);
                        }

                        Tasks.whenAllComplete(expenseTasks)
                                .addOnCompleteListener(done -> {
                                    for (Task<QuerySnapshot> expenseTask : expenseTasks) {
                                        if (expenseTask.isSuccessful()) {
                                            for (DocumentSnapshot expenseDoc : expenseTask.getResult()) {
                                                EXP expense = expenseDoc.toObject(EXP.class);
                                                if (expense != null) {
                                                    expenses.add(expense);
                                                    totalExpenseAmount[0] += expense.calculateExpenseDetails().get(1);
                                                }
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    updateBudget(totalExpenseAmount[0]);
                                });
                    } else {
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateBudget(double totalExp) {
        DocumentReference docRef = db.collection("budget").document(currentUserId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null && budgetData.containsKey(categoryName)) {
                    Map<String, Object> category = (Map<String, Object>) budgetData.get(categoryName);
                    category.put("totalExpenses", totalExp);

                    docRef.update(categoryName, category)
                            .addOnSuccessListener(aVoid -> showPieChart(category))
                            .addOnFailureListener(e -> progressDialog.dismiss());
                } else {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void showPieChart(Map<String, Object> category) {
        Number budgetRaw = (Number) category.get("budgetAmount");
        Number totalExpRaw = (Number) category.get("totalExpenses");

        if (budgetRaw != null && totalExpRaw != null) {
            double budget = budgetRaw.doubleValue();
            double totalExp = totalExpRaw.doubleValue();
            double remaining = budget - totalExp;

            updatePieChart(totalExp, remaining);
        }

        progressDialog.dismiss();
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
        pieChart.setCenterText(capitalize(categoryName) + "\nBudget Breakdown");
        pieChart.setDrawCenterText(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
