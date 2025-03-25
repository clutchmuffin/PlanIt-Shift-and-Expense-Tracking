package com.example.myapplication.model;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
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

public class Shopping extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> shoppingExpenses;
    private TextView addBudget, mainBalanceText;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ShoppingActivity";

    private ProgressDialog progressDialog;

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

// Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading Shopping Data...");
        progressDialog.setCancelable(false);

        addBudget = findViewById(R.id.addBudget);

        addBudget.setOnClickListener(v -> openSetBudget("Shopping"));



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
        db.collection("Jobs")
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
                                    // totalFoodExpense.setText("BDT: " + totalFoodExpenseAmount[0]);
                                    updateBudgetTotal(totalShoppingExpenseAmount[0]);
                                    progressDialog.dismiss();
                                });
                    }
                });
    }

    private void updateBudgetTotal(double totalExp) {
        db.collection("Budgy").document("Shopping")
                .update("totalExpenses", totalExp)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated total expense."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating total expense.", e));
    }

    private void showBudgetAndPieChart() {
        db.collection("Budgy").document("Shopping")
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching budget data", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        double budget = documentSnapshot.getDouble("budget");
                        double totalExp = documentSnapshot.getDouble("totalExpenses");
                        double remaining = budget - totalExp;

                        //mainBalanceText.setText("BDT: " + remaining);
                        updatePieChart(totalExp, remaining);
                    }
                });
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
