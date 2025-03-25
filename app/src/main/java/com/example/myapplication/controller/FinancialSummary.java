package com.example.myapplication.controller;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
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

    private double totalExpenseAmount = 0.0;
    private double totalNetpayAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_financial_summary);

        pieChart = findViewById(R.id.pieChart);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);
        totalNetPayTextView = findViewById(R.id.totalNetPayTextView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fetch and display data
        fetchData();
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
}
