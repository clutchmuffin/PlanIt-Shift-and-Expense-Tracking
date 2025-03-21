package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.EXP;
import com.example.myapplication.model.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder> {
    private List<EXP> expenses;

    public ExpenseListAdapter(List<EXP> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        EXP expense = expenses.get(position);
        List<Double> expenseDetails = expense.calculateExpenseDetails();
        holder.tvDescription.setText(expense.getDescription());
        //holder.tvAmount.setText(String.valueOf(expense.getAmount()));
        holder.tvAmount.setText("Total: " + "$" + expenseDetails.get(1));
        holder.tvRate.setText( "Rate: "+ "$" + expenseDetails.get(0));
        holder.tvRepeatType.setText(expense.getRepeatType().toString());
        holder.tvDateRange.setText(expense.getStartDate().substring(5) + " to " + expense.getEndDate().substring(5));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount,tvDateRange, tvRepeatType, tvRate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.expenseDescription);
            tvAmount = itemView.findViewById(R.id.expenseAmount);
            tvRate =  itemView.findViewById(R.id.expenseRate);
            tvRepeatType = itemView.findViewById(R.id.eventRepeatInfo); // New for recurrence info
            tvDateRange = itemView.findViewById(R.id.eventDateRange); // New for date range
        }
    }
}
