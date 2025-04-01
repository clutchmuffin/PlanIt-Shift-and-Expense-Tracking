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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final int selectedYear = 2025; // Change as needed
    private final int selectedMonth = 3;   // Change as needed (1 = January, 12 = December)

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
        //fetchData();
        fetchExpensesForMonth(selectedYear,selectedMonth);
        loadFoodExpenses();
    }


    private void updateFinancialSummary() {
        // Assume `totalNetpayAmount` is already calculated in a previous method, such as `fetchNetPayForMonth`
        double totalIncome = totalNetpayAmount;

        DocumentReference docRef = db.collection("budget").document(currentUserId);

        // Fetch existing financial summary data
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> budgetData = documentSnapshot.getData();
                if (budgetData != null) {
                    if (budgetData.containsKey("financialSummary")) {
                        // If financialSummary exists, update it with the new totals
                        Map<String, Object> finSum = (Map<String, Object>) budgetData.get("financialSummary");
                        finSum.put("totalExpenses", totalExpenseAmount);
                        finSum.put("totalIncome", totalIncome);

                        // Update Firestore with the new values
                        docRef.update("financialSummary", finSum)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Financial Summary updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update financial summary", e));
                    } else {
                        // If financialSummary doesn't exist, create it
                        Map<String, Object> newFinSum = new HashMap<>();
                        newFinSum.put("totalExpenses", totalExpenseAmount);
                        newFinSum.put("totalIncome", totalIncome);

                        // Create the new financial summary document
                        docRef.update("financialSummary", newFinSum)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Financial Summary created and updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to create financial summary", e));
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch financial summary", e));
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
        float balance = (float) (netPay - totalExpenses);

        // Add pie chart slices (all values must be positive for MPAndroidChart)
        entries.add(new PieEntry((float) totalExpenses, "Total Expenses"));
        entries.add(new PieEntry(Math.abs(balance), balance < 0 ? "Deficit" : "Net Pay"));

        PieDataSet dataSet = new PieDataSet(entries, "Financial Summary");
        dataSet.setColors(new int[] {
                getResources().getColor(R.color.teal_700), // Total Expenses
                balance < 0 ? getResources().getColor(R.color.red) : getResources().getColor(R.color.purple_200) // Deficit or Net Pay
        });
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(getResources().getColor(R.color.white));

        // Set a custom ValueFormatter to display the negative sign
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == Math.abs(balance) && balance < 0) {
                    return String.format("-%.2f", value);  // Show negative sign
                }
                return String.format("%.2f", value);
            }
        });

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


    //new stuff
    private double fetchNetPayForMonth(int year, int month) {
        db.collection("Jobs").whereEqualTo("userId", currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task<QuerySnapshot>> incomeFetchTasks = new ArrayList<>();
                totalNetpayAmount = 0.0;

                for (DocumentSnapshot jobDocument : task.getResult()) {
                    String jobId = jobDocument.getId();
                    Task<QuerySnapshot> incomeTask = db.collection("Jobs")
                            .document(jobId)
                            .collection("Events")
                            .get()
                            .addOnCompleteListener(incomeTaskResult -> {
                                if (incomeTaskResult.isSuccessful()) {
                                    for (DocumentSnapshot incomeDoc : incomeTaskResult.getResult()) {
                                        Double amount = incomeDoc.getDouble("netPay");
                                        String startDate = incomeDoc.getString("begin_date");
                                        String endDate = incomeDoc.getString("end_date");
                                        String repeatUntilDate = incomeDoc.getString("repeat_until_date");
                                        String repeatType = incomeDoc.getString("repeated");

                                        //if the event range is within the month then just add the netPay to the total
                                        if (isEventInMonth(startDate,endDate,repeatUntilDate,year,month,repeatType)) {
                                            totalNetpayAmount += amount;
                                        } else{
                                            //if it is not within the month like if it spills into the next days then calculate how much you are earning for the current month only
                                            if (amount != null && startDate != null && repeatType != null &&
                                                    isEventInMonth(startDate, endDate, repeatUntilDate, year, month, repeatType)) {
                                                totalNetpayAmount += calculateEventPay(amount, startDate, endDate, repeatUntilDate, year, month, repeatType);
                                            }}
                                    }
                                }
                            });
                    incomeFetchTasks.add(incomeTask);
                }

                Tasks.whenAllComplete(incomeFetchTasks).addOnCompleteListener(allTasks -> {
                    totalNetPayTextView.setText("Total Monthly Net Pay: $" + totalNetpayAmount);

                    updateFinancialSummary();
                    updatePieChart(totalExpenseAmount,totalNetpayAmount);
                });
            }
        });
        return totalNetpayAmount;
    }

    private double fetchExpensesForMonth(int year, int month) {
        db.collection("Jobs").whereEqualTo("userId", currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();
                totalExpenseAmount = 0.0;

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
                                        String startDate = expenseDoc.getString("startDate");
                                        String endDate = expenseDoc.getString("endDate");
                                        String repeatType = expenseDoc.getString("repeatType");


                                        //if the expense range is within the month then just add the netPay to the total
                                        if (isExpenseInMonth(startDate,endDate,year,month,repeatType)) {
                                            totalExpenseAmount += amount;
                                        } else{
                                            //if it is not within the month like if it spills into the next days then calculate how much you are earning for the current month only
                                            if (amount != null && startDate != null && repeatType != null &&
                                                    isExpenseInMonth(startDate, endDate, year, month, repeatType)) {
                                                totalExpenseAmount += calculateExpense(amount, startDate, endDate, year, month, repeatType);
                                            }}
                                    }
                                }
                            });
                    expenseFetchTasks.add(expenseTask);
                }

                Tasks.whenAllComplete(expenseFetchTasks).addOnCompleteListener(allTasks -> {
                    totalExpensesTextView.setText("Total Monthly Expenses Pay: $" + totalExpenseAmount);

                    fetchNetPayForMonth(selectedYear,selectedMonth);
;

                });
            }
        });
        return totalExpenseAmount;
    }

    private boolean isExpenseInMonth(String startDateStr, String endDateStr, int year, int month, String repeatType) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr) : startDate;
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            // If it's a one-time expense, check if it falls in the month
            if (repeatType.equals("NEVER")) {
                return !startDate.isBefore(monthStart) && !startDate.isAfter(monthEnd);
            }

            // Recurring expenses
            switch (repeatType) {
                case "DAILY":
                    return startDate.isBefore(monthEnd) && endDate.isAfter(monthStart);
                case "WEEKLY":
                    return ChronoUnit.WEEKS.between(startDate, monthStart) >= 0 && endDate.isAfter(monthStart);
                case "MONTHLY":
                    return startDate.getDayOfMonth() <= monthEnd.getDayOfMonth() && endDate.isAfter(monthStart);
                case "ANNUALLY":
                    return startDate.getMonthValue() == month && endDate.isAfter(monthStart);
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e("FinancialSummary", "Error parsing expense dates", e);
            return false;
        }
    }


    private boolean isEventInMonth(String startDateStr, String endDateStr, String repeatUntilStr, int year, int month, String repeatType ) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr) : startDate;
            LocalDate repeatUntil = (repeatUntilStr != null) ? LocalDate.parse(repeatUntilStr) : endDate;
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            if ((startDate.isBefore(monthEnd) && endDate.isAfter(monthStart)) || startDate.equals(monthStart)) {
                return true;
            }

            switch (repeatType) {
                case "DAILY":
                case "WEEKLY":
                    return startDate.isBefore(monthEnd) && repeatUntil.isAfter(monthStart);
                case "MONTHLY":
                    return (startDate.getDayOfMonth() <= monthEnd.getDayOfMonth()) &&
                            (startDate.getMonthValue() <= month) &&
                            repeatUntil.isAfter(monthStart);
                case "ANNUALLY":
                    return (startDate.getMonthValue() == month) && repeatUntil.isAfter(monthStart);
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.e("FinancialSummary", "Error parsing event dates", e);
            return false;
        }
    }


    private double calculateExpense(double amount, String startDateStr, String endDateStr, int year, int month, String repeatType) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr) : startDate;
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            long totalOccurrences = 0;

            switch (repeatType) {
                case "NEVER":
                    if (!startDate.isBefore(monthStart) && !startDate.isAfter(monthEnd)) {
                        return amount;
                    }
                    break;
                case "DAILY":
                    totalOccurrences = ChronoUnit.DAYS.between(
                            startDate.isBefore(monthStart) ? monthStart : startDate,
                            endDate.isAfter(monthEnd) ? monthEnd.plusDays(1) : endDate.plusDays(1)
                    );
                    break;
                case "WEEKLY":
                    totalOccurrences = ChronoUnit.WEEKS.between(
                            startDate.isBefore(monthStart) ? monthStart : startDate,
                            endDate.isAfter(monthEnd) ? monthEnd.plusDays(1) : endDate.plusDays(1)
                    );
                    break;
                case "MONTHLY":
                    totalOccurrences = 1; // If it repeats monthly, it happens once per month
                    break;
                case "ANNUALLY":
                    if (startDate.getMonthValue() == month) {
                        totalOccurrences = 1; // Occurs only if the start month matches the selected month
                    }
                    break;
            }

            return totalOccurrences * amount;
        } catch (Exception e) {
            Log.e("FinancialSummary", "Error calculating expense", e);
            return 0.0;
        }
    }

    private double calculateEventPay(double amount, String startDateStr, String endDateStr, String repeatUntilStr, int year, int month, String repeatType) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr) : startDate;
        LocalDate repeatUntil = (repeatUntilStr != null) ? LocalDate.parse(repeatUntilStr) : endDate;
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        long totalOccurrences = 0;

        switch (repeatType) {
            case "NEVER":
                if (!startDate.isBefore(monthStart) && !startDate.isAfter(monthEnd)) {
                    return amount;
                }
                break;
            case "DAILY":
                totalOccurrences = ChronoUnit.DAYS.between(
                        startDate.isBefore(monthStart) ? monthStart : startDate,
                        repeatUntil.isAfter(monthEnd) ? monthEnd.plusDays(1) : repeatUntil.plusDays(1)
                );
                break;
            case "WEEKLY":
                totalOccurrences = ChronoUnit.WEEKS.between(
                        startDate.isBefore(monthStart) ? monthStart : startDate,
                        repeatUntil.isAfter(monthEnd) ? monthEnd.plusDays(1) : repeatUntil.plusDays(1)
                );
                break;
            case "MONTHLY":
                totalOccurrences = 1;
                break;
            case "ANNUALLY":
                if (startDate.getMonthValue() == month) {
                    totalOccurrences = 1;
                }
                break;
        }
        return totalOccurrences * amount;
    }




}
