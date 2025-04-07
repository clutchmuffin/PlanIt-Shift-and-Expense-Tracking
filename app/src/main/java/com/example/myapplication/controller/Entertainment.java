package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Entertainment extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> entertainmentExpenses;
    private TextView totalEntertainmentExpense, addIncome;
    private PieChart pieChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EntertainmentActivity";
    private ProgressDialog progressDialog;
    private String currentUserId;

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
        progressDialog.setMessage("Loading Entertainment Data...");
        progressDialog.setCancelable(false);


        addIncome = findViewById(R.id.addIncome);

        addIncome.setOnClickListener(v -> openSetBudget("entertainment"));

        // Load data
        loadEntertainmentExpenses();
        showBudgetAndPieChart();


    }

    private void openSetBudget(String category) {
        Intent intent = new Intent(this, SetBudget.class);
        intent.putExtra("BUDGET_CATEGORY", category);
        startActivity(intent);
    }
    private void loadEntertainmentExpenses() {
        progressDialog.show();
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
//                                            double jobEntertainmentExpense = 0.0;
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    entertainmentExpenses.add(expense);
                                                    totalEntertainmentExpenseAmount[0] += expense.calculateExpenseDetails().get(1);
                                                }
                                            }
//                                            totalEntertainmentExpenseAmount[0] += jobEntertainmentExpense;
                                        } else {
                                            Log.e(TAG, "Error fetching expenses", expenseTask.getException());
                                        }
                                    }

                                    adapter.notifyDataSetChanged();
                                    //totalEntertainmentExpense.setText("BDT: " + totalEntertainmentExpenseAmount[0]);
                                    updateBudgetTotal(totalEntertainmentExpenseAmount[0]);

                                });
                    } else {
                        progressDialog.dismiss(); // Dismiss after data is fetched
                        Log.e(TAG, "Error fetching jobs", task.getException());
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
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Entertainment expenses updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update entertainment expenses", e));
                }
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
                            Double budget = (Double) foodCategory.get("budgetAmount");
                            Double totalExp = (Double) foodCategory.get("totalExpenses");

                            if (budget != null && totalExp != null) {
                                double remaining = budget - totalExp;
                                updatePieChart(totalExp, remaining);
                            } else {
                                Log.e(TAG, "Budget or totalExpenses for entertainment is null");
                            }
                        }
                    } progressDialog.dismiss(); // Dismiss after data is fetched
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching entertainment budget data", e));
        progressDialog.dismiss(); // Dismiss after data is fetched
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
