package com.example.myapplication.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.Expense;
import com.example.myapplication.view.adapter.ExpenseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class Entertainment extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense> entertainmentExpenses;
    private TextView totalEntertainmentExpense;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "EntertainmentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entertainment);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        totalEntertainmentExpense = findViewById(R.id.totalEntertainmentExpense);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize expense list and adapter
        entertainmentExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter(entertainmentExpenses, null);
        recyclerView.setAdapter(adapter);

        //load expense amount when activty starts
        loadEntertainmentExpenses();
        showMainBalance();

        // Load expenses when "Show All Data" is clicked
        findViewById(R.id.expenseEntertainmentShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEntertainmentExpenses();
            }
        });
    }


    private void loadEntertainmentExpenses() {
        db.collection("Jobs")  // Access the 'Jobs' collection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalEntertainmentExpenseAmount = {0.0}; // Using an array to hold the total
                        entertainmentExpenses.clear(); // Clear old data

                        // List to keep track of tasks for parallel execution
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        // Iterate through each job document and trigger parallel requests for expenses
                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId(); // Get the job ID

                            // Fetch expenses for this job in parallel
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", "Entertainment")
                                    .get();

                            expenseFetchTasks.add(expenseTask);
                        }

                        // When all expense fetch operations are completed, process the results
                        Tasks.whenAllComplete(expenseFetchTasks)
                                .addOnCompleteListener(allTask -> {
                                    for (Task<QuerySnapshot> expenseTask : expenseFetchTasks) {
                                        if (expenseTask.isSuccessful()) {
                                            double jobEntertainmentExpense = 0.0;

                                            // Iterate through each food expense document
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                Expense expense = expenseDocument.toObject(Expense.class);
                                                if (expense != null) {
                                                    entertainmentExpenses.add(expense);
                                                    jobEntertainmentExpense += expense.getAmount(); // Add to the total for this job
                                                }
                                            }

                                            // Add this job's total food expense to the overall total
                                            totalEntertainmentExpenseAmount[0] += jobEntertainmentExpense;
                                        } else {
                                            Log.e(TAG, "Error fetching expenses", expenseTask.getException());
                                        }
                                    }

                                    // Update the RecyclerView and total only after all tasks are completed
                                    adapter.notifyDataSetChanged();
                                    totalEntertainmentExpense.setText("BDT: " + totalEntertainmentExpenseAmount[0]);
                                    updateBudgetTotal(totalEntertainmentExpenseAmount[0]);
                                });
                    } else {
                        Log.e(TAG, "Error fetching jobs", task.getException());
                    }
                });
    }


    private void updateBudgetTotal(double totalExp) {
        // Reference the "Food" document in the "Budgy" collection
        db.collection("Budgy").document("Entertainment")
                .update("totalExpenses", totalExp) // Update the "totalExp" field
                .addOnSuccessListener(aVoid -> {
                    // You can log success or do anything else here
                    Log.d(TAG, "Successfully updated the total expense in Budgy.");
                })
                .addOnFailureListener(e -> {
                    // Handle failure if any
                    Log.e(TAG, "Error updating total expense in Budgy.", e);
                });
    }

    private void showMainBalance() {
        db.collection("Budgy").document("Entertainment")
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching budget data", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        double budget = documentSnapshot.contains("budget") ? documentSnapshot.getDouble("budget") : 0.0;
                        double totalExp = documentSnapshot.contains("totalExpenses") ? documentSnapshot.getDouble("totalExpenses") : 0.0;
                        double mainBalanceAmount = budget - totalExp;

                        TextView mainBalanceText = findViewById(R.id.mainBalance);
                        mainBalanceText.setText("BDT: " + mainBalanceAmount);
                    }
                });
    }

}
