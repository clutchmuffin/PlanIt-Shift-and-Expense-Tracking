package com.example.myapplication.controller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class Entertainment extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> entertainmentExpenses;
    private TextView totalEntertainmentExpense, addIncome;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EntertainmentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entertainment);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        //totalEntertainmentExpense = findViewById(R.id.totalEntertainmentExpense);
        pieChart = findViewById(R.id.pieEntertainmentChart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize expense list and adapter
        entertainmentExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter((ArrayList<EXP>) entertainmentExpenses, null);
        recyclerView.setAdapter(adapter);


        addIncome = findViewById(R.id.addIncome);

        addIncome.setOnClickListener(v -> openSetBudget("Entertainment"));

        // Load data
        loadEntertainmentExpenses();
        showMainBalance();


    }

    private void openSetBudget(String category) {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }
    private void loadEntertainmentExpenses() {
        db.collection("Jobs")
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
                                            double jobEntertainmentExpense = 0.0;
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    entertainmentExpenses.add(expense);
                                                    jobEntertainmentExpense += expense.getAmount();
                                                }
                                            }
                                            totalEntertainmentExpenseAmount[0] += jobEntertainmentExpense;
                                        } else {
                                            Log.e(TAG, "Error fetching expenses", expenseTask.getException());
                                        }
                                    }

                                    adapter.notifyDataSetChanged();
                                    //totalEntertainmentExpense.setText("BDT: " + totalEntertainmentExpenseAmount[0]);
                                    updateBudgetTotal(totalEntertainmentExpenseAmount[0]);
                                });
                    } else {
                        Log.e(TAG, "Error fetching jobs", task.getException());
                    }
                });
    }

    private void updateBudgetTotal(double totalExp) {
        db.collection("Budgy").document("Entertainment")
                .update("totalExpenses", totalExp)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully updated total expense in Budgy."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating total expense in Budgy.", e));
    }

    private void showMainBalance() {
        db.collection("Budgy").document("Entertainment")
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching budget data", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Double budget = documentSnapshot.getDouble("budget");
                        Double totalExp = documentSnapshot.getDouble("totalExpenses");
                        double remaining = 0;
                        if (budget != null && totalExp != null) {
                            remaining = budget - totalExp;
                        } else {
                            Log.e(TAG, "Budget or Total Expenses is null");
                        }

                        //mainBalanceText.setText("BDT: " + remaining);
                        updatePieChart(totalExp, remaining);
                    }
                });
    }

    private void updatePieChart(double spent, double remaining) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) spent, "Spent"));
        entries.add(new PieEntry((float) remaining, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
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
