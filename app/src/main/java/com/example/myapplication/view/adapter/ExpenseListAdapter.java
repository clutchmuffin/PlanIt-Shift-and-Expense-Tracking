package com.example.myapplication.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.EXP;
import com.example.myapplication.model.Expense;
import com.example.myapplication.model.Job;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder> {
    private List<EXP> expenses;
    private Job job;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ExpenseListAdapter(List<EXP> expenses, Job job) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        this.job = job;
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
        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText(String.valueOf(expense.getAmount()));

        holder.expenseDelete.setOnClickListener(
                v -> {
                    if (job.getJobId() != null) {
                        // Query for the expense document ID based on expense data
                        db.collection("Jobs").document(job.getJobId())
                                .collection("EXP")
                                .whereEqualTo("description", expense.getDescription())
                                .whereEqualTo("amount", expense.getAmount())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {

                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        // Get the document ID of the first matching expense
                                        String expenseDocId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                        // Delete the expense from Firestore
                                        db.collection("Jobs").document(job.getJobId())
                                                .collection("EXP")
                                                .document(expenseDocId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {

                                                    // Delete from local list and update RecyclerView
                                                    job.getExpenses().remove(position);
                                                    notifyItemRemoved(position);
                                                    Log.d("JobDetailActivity", "Expense successfully deleted from Firestore");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("JobDetailActivity", "Error deleting expense from Firestore", e);
                                                });
                                    } else {
                                        Log.e("JobDetailActivity", "Could not find expense document to delete");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("JobDetailActivity", "Error querying for expense to delete", e);
                                });
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount;
        ImageButton expenseDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.expenseDescription);
            tvAmount = itemView.findViewById(R.id.expenseAmount);
            expenseDelete = itemView.findViewById(R.id.expenseDeleteBtn);
        }
    }
}
