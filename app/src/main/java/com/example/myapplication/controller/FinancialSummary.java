package com.example.myapplication.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.EXP;
import com.example.myapplication.view.adapter.ExpenseListAdapter;
import com.github.mikephil.charting.charts.PieChart;
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

public class FinancialSummary extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PieChart pieChart;
    private TextView totalExpensesTextView;
    private TextView totalNetPayTextView;
    private List<EXP> allExpenses;
    private ExpenseListAdapter adapter;
    private RecyclerView recyclerView;
    private static final String TAG = "FinancialSummary";

    private String currentUserId;
    private ProgressDialog progressDialog;  // Declare the ProgressDialog

    private double totalExpenseAmount = 0.0;
    private double totalNetpayAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_financial_summary);

        pieChart = findViewById(R.id.pieChart);
        recyclerView = findViewById(R.id.expensesRecyclerView);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);
        totalNetPayTextView = findViewById(R.id.totalNetPayTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter((ArrayList<EXP>) allExpenses, null);
        recyclerView.setAdapter(adapter);

        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading Food Data...");
        progressDialog.setCancelable(false);


        // Retrieve user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fetch and display data
        fetchData();
        loadFoodExpenses();
    }

    private void fetchData() {

        // Fetch total expenses
        db.collection("Jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();
                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .get()
                                    .addOnCompleteListener(expenseTaskResult -> {
                                        if (expenseTaskResult.isSuccessful()) {
                                            for (DocumentSnapshot expenseDoc : expenseTaskResult.getResult()) {
                                                Double amount = expenseDoc.getDouble("amount");
                                                if (amount != null) {
                                                    totalExpenseAmount += amount;
                                                }
                                            }
                                        }

                                    });
                            expenseFetchTasks.add(expenseTask);
                        }

                        // Wait for all expenses to be fetched and then fetch net pay
                        Tasks.whenAllComplete(expenseFetchTasks).addOnCompleteListener(allTasks -> {
                            totalExpensesTextView.setText("Total Expenses: $" + totalExpenseAmount);

                            // Fetch total net pay
                            fetchNetPay();
                            loadFoodExpenses();
                        });
                    }
                });
    }

    private void fetchNetPay() {
        db.collection("Jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();
                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("Events")
                                    .get()
                                    .addOnCompleteListener(expenseTaskResult -> {
                                        if (expenseTaskResult.isSuccessful()) {
                                            for (DocumentSnapshot expenseDoc : expenseTaskResult.getResult()) {
                                                Double amount = expenseDoc.getDouble("netPay");
                                                if (amount != null) {
                                                    totalNetpayAmount += amount;
                                                }
                                            }
                                        }
                                    });
                            expenseFetchTasks.add(expenseTask);
                        }

                        // Wait for all net pay data to be fetched
                        Tasks.whenAllComplete(expenseFetchTasks).addOnCompleteListener(allTasks -> {
                            totalNetPayTextView.setText("Total Net Pay: $" + totalNetpayAmount);

                            // Now update the PieChart with both values
                            updatePieChart(totalExpenseAmount, totalNetpayAmount);
                        });
                    }
                });
    }

    private void updatePieChart(double totalExpenses, double netPay) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalExpenses, "Total Expenses"));
        entries.add(new PieEntry((float) netPay, "Net Pay"));

        PieDataSet dataSet = new PieDataSet(entries, "Financial Summary");
        dataSet.setColors(new int[] {getResources().getColor(R.color.teal_700), getResources().getColor(R.color.red)});
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(getResources().getColor(R.color.white));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();  // Refresh the PieChart
    }


    private void loadFoodExpenses() {

        progressDialog.show();

        db.collection("Jobs").whereEqualTo("userId", currentUserId) // Filter jobs by current user
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalFoodExpenseAmount = {0.0};
                        allExpenses.clear();
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId();
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
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
                                                    allExpenses.add(expense);
                                                    totalFoodExpenseAmount[0] += expense.getAmount();

                                                }
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    progressDialog.dismiss();


                                });
                    } else {
                        Log.e(TAG, "Error fetching jobs for current user", task.getException());
                        progressDialog.dismiss();
                    }
                });
    }

}
