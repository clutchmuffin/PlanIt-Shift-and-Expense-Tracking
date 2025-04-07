package com.example.myapplication.controller;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.EXP;
import com.example.myapplication.model.Expense;
import com.example.myapplication.model.Job;
import com.example.myapplication.view.adapter.JobSummaryAdapter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.C;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JobSummaryActivity extends AppCompatActivity {
    private static final String TAG = "JobSummaryActivity";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String currentUserId;
    private FirebaseFirestore db;
    private RecyclerView jobSummaryRecyclerView;
    private List<JobSummaryData> jobSummaries;
    private JobSummaryAdapter adapter;
    private TextView currentMonthText, totalEarningsText, totalExpensesText, netIncomeText;
    private ImageButton prevMonthButton, nextMonthButton;
    
    private YearMonth selectedMonth;
    private double totalEarnings = 0.0;
    private double totalExpenses = 0.0;

    private PieChart summaryPieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_summary);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        
        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);
        
        // Set the default month to current month
        selectedMonth = YearMonth.now();
        
        // Initialize views
        initializeViews();
        
        // Load job summaries for current month
        loadJobSummaries();
        
        // Set up month navigation
        setupMonthNavigation();
    }
    
    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        jobSummaryRecyclerView = findViewById(R.id.jobSummaryRecyclerView);
        jobSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        jobSummaries = new ArrayList<>();
        adapter = new JobSummaryAdapter(jobSummaries);
        jobSummaryRecyclerView.setAdapter(adapter);
        
        currentMonthText = findViewById(R.id.currentMonthText);
        totalEarningsText = findViewById(R.id.totalEarningsText);
        totalExpensesText = findViewById(R.id.totalExpensesText);
        netIncomeText = findViewById(R.id.netIncomeText);
        
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);

        summaryPieChart = findViewById(R.id.summaryPieChart);
        setupPieChart();
        
        updateMonthDisplay();
    }
    
    private void setupMonthNavigation() {
        prevMonthButton.setOnClickListener(v -> {
            selectedMonth = selectedMonth.minusMonths(1);
            updateMonthDisplay();
            loadJobSummaries();
        });
        
        nextMonthButton.setOnClickListener(v -> {
            selectedMonth = selectedMonth.plusMonths(1);
            updateMonthDisplay();
            loadJobSummaries();
        });
    }
    
    private void updateMonthDisplay() {
        currentMonthText.setText(selectedMonth.format(MONTH_FORMATTER));
    }

    private void setupPieChart() {
        summaryPieChart.setUsePercentValues(true);
        summaryPieChart.getDescription().setEnabled(false);
        summaryPieChart.setExtraOffsets(10, 10, 10, 10);
        
        summaryPieChart.setDragDecelerationFrictionCoef(0.95f);
        
        summaryPieChart.setCenterText("Income\nBreakdown");
        summaryPieChart.setCenterTextSize(14f);
        summaryPieChart.setDrawCenterText(true);
        
        summaryPieChart.setDrawHoleEnabled(true);
        summaryPieChart.setHoleColor(Color.WHITE);
        summaryPieChart.setHoleRadius(58f);
        summaryPieChart.setTransparentCircleRadius(61f);
        
        summaryPieChart.setDrawEntryLabels(false);
        summaryPieChart.setEntryLabelTextSize(12f);
        summaryPieChart.setEntryLabelColor(Color.BLACK);
        
        summaryPieChart.setRotationAngle(0);
        summaryPieChart.setRotationEnabled(true);
        summaryPieChart.setHighlightPerTapEnabled(true);
        
        summaryPieChart.getLegend().setEnabled(true);
        summaryPieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        summaryPieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        summaryPieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        summaryPieChart.getLegend().setDrawInside(false);
    }
    
    private void loadJobSummaries() {
        // Clear previous data
        jobSummaries.clear();
        totalEarnings = 0.0;
        totalExpenses = 0.0;

        summaryPieChart.clear();
        summaryPieChart.setNoDataText("Loading data...");
        summaryPieChart.invalidate();
        
        // Get jobs for current user
        db.collection("Jobs")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Job> jobs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            jobs.add(document.toObject(Job.class));
                        }
                        
                        // After getting all jobs, load events and expenses for each job
                        loadEventsAndExpenses(jobs);
                    } else {
                        Log.e(TAG, "Error getting jobs: ", task.getException());
                    }
                });
    }

    private void loadEventsAndExpenses(List<Job> jobs) {
        // Track which jobs have events or expenses in the selected month
        Map<Job, JobSummaryData> jobDataMap = new HashMap<>();
        
        // Count of async operations we're waiting for
        final int[] pendingOperations = {jobs.size() * 2}; // Events and expenses for each job
        
        for (Job job : jobs) {
            JobSummaryData summaryData = new JobSummaryData(job);
            jobDataMap.put(job, summaryData);
            
            // Get start and end date of the month we're viewing
            LocalDate startDate = selectedMonth.atDay(1);
            LocalDate endDate = selectedMonth.atEndOfMonth();
    
            // Load events (shifts) for the job
            loadJobEvents(job, summaryData, startDate, endDate, pendingOperations, jobDataMap);
    
            // Load expenses for the job
            loadJobExpenses(job, summaryData, startDate, endDate, pendingOperations, jobDataMap);
        }
    }
    
    private void loadJobEvents(Job job, JobSummaryData summaryData, LocalDate startDate, LocalDate endDate, 
                          final int[] pendingOperations, Map<Job, JobSummaryData> jobDataMap) {
        // Fix: Use the correct path to events collection
        if (job.getJobId() != null && !job.getJobId().isEmpty()) {
            db.collection("Jobs").document(job.getJobId())
                    .collection("Events")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean hasEventsInMonth = false;
                            
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CalendarEvent event = document.toObject(CalendarEvent.class);
    
                                // Check if event falls within the selected month
                                LocalDate eventStartDate = LocalDate.parse(event.getBegin_date(), DATE_FORMATTER);
                                LocalDate eventEndDate = LocalDate.parse(event.getEnd_date(), DATE_FORMATTER);
    
                                // Skip events that are completely outside our month range
                                if (eventEndDate.isBefore(startDate) || eventStartDate.isAfter(endDate)) {
                                    continue;
                                }
    
                                // If we get here, the job has at least one event in the month
                                hasEventsInMonth = true;
    
                                // Calculate hours worked for this event
                                LocalTime startTime = LocalTime.parse(event.getBegin_time(), TIME_FORMATTER);
                                LocalTime endTime = LocalTime.parse(event.getEnd_time(), TIME_FORMATTER);
    
                                // Simple calculation - can be refined for multi-day events
                                double hours = startTime.until(endTime, ChronoUnit.MINUTES) / 60.0;
    
                                // Add to summary
                                summaryData.addHours(hours);
                                summaryData.addEarnings(hours * job.getPayRate());
    
                                // Add to totals
                                totalEarnings += hours * job.getPayRate();
                            }
                            
                            // Mark this job as having events if applicable
                            summaryData.setHasEvents(hasEventsInMonth);
                        } else {
                            Log.e(TAG, "Error getting events: ", task.getException());
                        }
                        // Signal that this async operation is complete
                        checkIfComplete(pendingOperations, jobDataMap);
                    });
        } else {
            // If no job ID, just decrement the counter
            checkIfComplete(pendingOperations, jobDataMap);
        }
    }
    
    private void loadJobExpenses(Job job, JobSummaryData summaryData, LocalDate startDate, LocalDate endDate, 
                        final int[] pendingOperations, Map<Job, JobSummaryData> jobDataMap) {
    if (job.getJobId() != null && !job.getJobId().isEmpty()) {
        db.collection("Jobs").document(job.getJobId())
                .collection("EXP")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasExpensesInMonth = false;
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            EXP expense = document.toObject(EXP.class);

                            // Calculate expense amount for the current month only
                            try {
                                if (expense.getStartDate() != null) {
                                    LocalDate expStartDate = LocalDate.parse(expense.getStartDate(), DATE_FORMATTER);
                                    LocalDate expEndDate = expense.getEndDate() != null ? 
                                            LocalDate.parse(expense.getEndDate(), DATE_FORMATTER) : expStartDate;
                                    
                                    double monthlyAmount = calculateMonthlyExpense(
                                            expense.getAmount(), 
                                            expStartDate, 
                                            expEndDate, 
                                            startDate, 
                                            endDate, 
                                            expense.getRepeatType().toString());
                                    
                                    if (monthlyAmount > 0) {
                                        // Add expense data for this month
                                        summaryData.addExpenses(monthlyAmount);
                                        summaryData.addExpenseDetail(expense.getDescription(), monthlyAmount);
                
                                        // Add to totals
                                        totalExpenses += monthlyAmount;
                                        hasExpensesInMonth = true;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing expense: " + e.getMessage(), e);
                            }
                        }
                        
                        summaryData.setHasExpenses(hasExpensesInMonth);
                    } else {
                        Log.e(TAG, "Error getting expenses: ", task.getException());
                    }
                    // Signal that this async operation is complete
                    checkIfComplete(pendingOperations, jobDataMap);
                });
        } else {
            checkIfComplete(pendingOperations, jobDataMap);
        }
    }

    private double calculateMonthlyExpense(double amount, LocalDate expStartDate, LocalDate expEndDate,
                                          LocalDate monthStart, LocalDate monthEnd, String repeatType) {
        // If expense doesn't overlap with the month at all
        if (expEndDate.isBefore(monthStart) || expStartDate.isAfter(monthEnd)) {
            return 0.0;
        }

        long totalOccurrences = 0;

        switch (repeatType) {
            case "NEVER":
                // One-time expense, only count if it starts in this month
                if (!expStartDate.isBefore(monthStart) && !expStartDate.isAfter(monthEnd)) {
                    return amount;
                }
                break;
            case "DAILY":
                // Count days in this month
                LocalDate periodStart = expStartDate.isBefore(monthStart) ? monthStart : expStartDate;
                LocalDate periodEnd = expEndDate.isAfter(monthEnd) ? monthEnd : expEndDate;
                totalOccurrences = ChronoUnit.DAYS.between(periodStart, periodEnd.plusDays(1));
                break;
            case "WEEKLY":
                // Count weeks in this month
                periodStart = expStartDate.isBefore(monthStart) ? monthStart : expStartDate;
                periodEnd = expEndDate.isAfter(monthEnd) ? monthEnd : expEndDate;
                totalOccurrences = Math.max(1, ChronoUnit.WEEKS.between(periodStart, periodEnd.plusDays(1)));
                break;
            case "MONTHLY":
                // If monthly expense is active during this month, count it once
                if (!(expEndDate.isBefore(monthStart) || expStartDate.isAfter(monthEnd))) {
                    totalOccurrences = 1;
                }
                break;
            case "ANNUALLY":
                // If annual expense occurs this month, count it once
                if (expStartDate.getMonthValue() == monthStart.getMonthValue()) {
                    totalOccurrences = 1;
                }
                break;
        }

        return amount * Math.max(0, totalOccurrences);
    }
    
    private void checkIfComplete(final int[] pendingOperations, Map<Job, JobSummaryData> jobDataMap) {
        pendingOperations[0]--;
        
        if (pendingOperations[0] <= 0) {
            // All async operations complete
            // Add jobs with either events or expenses to the final list
            for (JobSummaryData summaryData : jobDataMap.values()) {
                if (summaryData.hasEvents() || summaryData.hasExpenses()) {
                    jobSummaries.add(summaryData);
                }
            }
            
            // Update UI
            updateSummaryUI();
        }
    }
    
    private void updateSummaryUI() {
        // Update totals
        totalEarningsText.setText(String.format(Locale.US, "Total Earnings: $%.2f", totalEarnings));
        totalExpensesText.setText(String.format(Locale.US, "Total Expenses: $%.2f", totalExpenses));
        netIncomeText.setText(String.format(Locale.US, "Net Income: $%.2f", totalEarnings - totalExpenses));

        // Update pie chart
        updatePieChart();

        // Update recycler view
        adapter.notifyDataSetChanged();
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        
        // Only proceed if we have earnings
        if (totalEarnings > 0) {
            double amountSpent = totalExpenses;
            double amountLeft = totalEarnings - totalExpenses;
            
            // Handle case where expenses exceed earnings
            if (amountLeft < 0) {
                amountLeft = 0;
            }
            
            if (amountSpent > 0) {
                entries.add(new PieEntry((float) amountSpent, "Amount Spent"));
            }
            
            if (amountLeft > 0) {
                entries.add(new PieEntry((float) amountLeft, "Amount Left"));
            }
            
            PieDataSet dataSet = new PieDataSet(entries, "");
            
            int[] pieColors = {
                Color.rgb(244, 67, 54),   // Red for amount spent
                Color.rgb(33, 150, 243),  // Blue for amount left
//                Color.rgb(76, 175, 80)    // Green for amount left
            };
            dataSet.setColors(pieColors);
            
            PieData data = new PieData(dataSet);
            // data.setValueFormatter(new PercentFormatter(summaryPieChart));
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.WHITE);
            
            summaryPieChart.setData(data);
            summaryPieChart.setUsePercentValues(false);
            summaryPieChart.setCenterText("Earnings\nBreakdown");
            summaryPieChart.invalidate();
            
            summaryPieChart.animateY(1000);
        } else {
            summaryPieChart.clear();
            summaryPieChart.setNoDataText("No earnings available for this month");
            summaryPieChart.invalidate();
        }
    }
    
    // Class to hold job summary data
    public static class JobSummaryData {
        private Job job;
        private double hoursWorked = 0.0;
        private double earnings = 0.0;
        private double expenses = 0.0;
        private Map<String, Double> expenseDetails = new HashMap<>();
        private boolean hasEvents = false;
        private boolean hasExpenses = false;
        
        public JobSummaryData(Job job) {
            this.job = job;
        }
        
        public Job getJob() {
            return job;
        }
        
        public double getHoursWorked() {
            return hoursWorked;
        }
        
        public double getEarnings() {
            return earnings;
        }
        
        public double getExpenses() {
            return expenses;
        }
        
        public void addHours(double hours) {
            this.hoursWorked += hours;
        }
        
        public void addEarnings(double amount) {
            this.earnings += amount;
        }
        
        public void addExpenses(double amount) {
            this.expenses += amount;
        }

        public Map<String, Double> getExpenseDetails() {
            return expenseDetails;
        }
        
        public void addExpenseDetail(String description, double amount) {
            this.expenseDetails.put(description, amount);
        }

        public boolean hasEvents() {
            return hasEvents;
        }
        
        public void setHasEvents(boolean hasEvents) {
            this.hasEvents = hasEvents;
        }
        
        public boolean hasExpenses() {
            return hasExpenses;
        }
        
        public void setHasExpenses(boolean hasExpenses) {
            this.hasExpenses = hasExpenses;
        }
    }
}