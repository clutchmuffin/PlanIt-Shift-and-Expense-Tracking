package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.JobSummaryActivity;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JobSummaryAdapter extends RecyclerView.Adapter<JobSummaryAdapter.JobSummaryViewHolder> {
    
    private List<JobSummaryActivity.JobSummaryData> jobSummaries;
    
    public JobSummaryAdapter(List<JobSummaryActivity.JobSummaryData> jobSummaries) {
        this.jobSummaries = jobSummaries;
    }
    
    @NonNull
    @Override
    public JobSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_summary, parent, false);
        return new JobSummaryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull JobSummaryViewHolder holder, int position) {
        JobSummaryActivity.JobSummaryData summaryData = jobSummaries.get(position);
        
        // Set job details - existing code
        holder.jobTitleText.setText(summaryData.getJob().getTitle());
        holder.jobEmployerText.setText("at " + summaryData.getJob().getEmployer());
        
        int jobColor = 0;
        try {
            jobColor = summaryData.getJob().getColor();
        } catch (Exception e) {
            jobColor = 0xFF6200EE; // Purple default
        }
        holder.jobColorIndicator.setBackgroundColor(jobColor);
        
        holder.jobHoursText.setText(String.format(Locale.US, "%.1f", summaryData.getHoursWorked()));
        
        double payRate = 0.0;
        try {
            payRate = Double.parseDouble(String.valueOf(summaryData.getJob().getPayRate()));
        } catch (Exception e) {
            payRate = 0.0;
        }
        holder.jobRateText.setText(String.format(Locale.US, "%.2f", payRate));
        
        holder.jobEarningsText.setText(String.format(Locale.US, "%.2f", summaryData.getEarnings()));
        holder.jobExpensesText.setText(String.format(Locale.US, "%.2f", summaryData.getExpenses()));
        
        // Add expense details
        holder.expenseContainer.removeAllViews(); // Clear existing expense views
        
        Map<String, Double> expenseDetails = summaryData.getExpenseDetails();
        if (expenseDetails != null && !expenseDetails.isEmpty()) {
            for (Map.Entry<String, Double> expense : expenseDetails.entrySet()) {
                // Inflate individual expense item
                View expenseView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_job_summary_expense, holder.expenseContainer, false);
                
                // Set expense details
                TextView expenseNameTextView = expenseView.findViewById(R.id.expenseNameTextView);
                TextView expenseAmountTextView = expenseView.findViewById(R.id.expenseAmountTextView);
                
                expenseNameTextView.setText(expense.getKey());
                expenseAmountTextView.setText(String.format(Locale.US, "$%.2f", expense.getValue()));
                
                // Add to container
                holder.expenseContainer.addView(expenseView);
            }
            holder.expenseContainer.setVisibility(View.VISIBLE);
        } else {
            // No expenses to show
            TextView noExpensesView = new TextView(holder.itemView.getContext());
            noExpensesView.setText("No expenses for this period");
            noExpensesView.setTextSize(12);
            holder.expenseContainer.addView(noExpensesView);
        }
    }
    
    @Override
    public int getItemCount() {
        return jobSummaries.size();
    }
    
    static class JobSummaryViewHolder extends RecyclerView.ViewHolder {
        View jobColorIndicator;
        TextView jobTitleText, jobEmployerText, jobHoursText, jobRateText, jobEarningsText, jobExpensesText;
        LinearLayout expenseContainer;
        
        public JobSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            jobColorIndicator = itemView.findViewById(R.id.jobColorIndicator);
            jobTitleText = itemView.findViewById(R.id.jobTitleText);
            jobEmployerText = itemView.findViewById(R.id.jobEmployerText);
            jobHoursText = itemView.findViewById(R.id.jobHoursText);
            jobRateText = itemView.findViewById(R.id.jobRateText);
            jobEarningsText = itemView.findViewById(R.id.jobEarningsText);
            jobExpensesText = itemView.findViewById(R.id.jobExpensesText);
            expenseContainer = itemView.findViewById(R.id.expenseContainer);
        }
    }
}