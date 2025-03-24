package com.example.myapplication.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.controller.BudgetMainActivity;
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

public class Food extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> foodExpenses;
    private TextView totalFoodExpense, addIncome;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FoodActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        recyclerView = findViewById(R.id.recyclerView);
        // totalFoodExpense = findViewById(R.id.totalFoodExpense);
        //mainBalanceText = findViewById(R.id.mainBalance);
        pieChart = findViewById(R.id.pieFoodChart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter((ArrayList<EXP>) foodExpenses, null);
        recyclerView.setAdapter(adapter);


        addIncome = findViewById(R.id.addIncome);
        addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Food.this, SetBudget.class);
                intent.putExtra("BUDGET_CATEGORY", "Food");
                startActivity(intent);


            }
        });




        loadFoodExpenses();
        showBudgetAndPieChart();
    }

    private void openSetBudget(String category) {
        Intent intent = new Intent(Food.this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }

    private void loadFoodExpenses() {
        db.collection("Jobs")
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
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    foodExpenses.add(expense);
                                                    totalFoodExpenseAmount[0] += expense.getAmount();
                                                }
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    // totalFoodExpense.setText("BDT: " + totalFoodExpenseAmount[0]);
                                    updateBudgetTotal(totalFoodExpenseAmount[0]);
                                });
                    }
                });
    }

    private void updateBudgetTotal(double totalExp) {
        db.collection("Budgy").document("Food")
                .update("totalExpenses", totalExp)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated total expense."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating total expense.", e));
    }

    private void showBudgetAndPieChart() {
        db.collection("Budgy").document("Food")
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

        PieDataSet dataSet = new PieDataSet(entries, "Food Budget Breakdown");

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
