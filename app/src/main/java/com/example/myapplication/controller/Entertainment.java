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
        adapter = new ExpenseListAdapter(entertainmentExpenses);
        recyclerView.setAdapter(adapter);

        // Load expenses when "Show All Data" is clicked
        findViewById(R.id.expenseEntertainmentShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFoodExpenses();
            }
        });
    }

    private void loadFoodExpenses() {
        db.collection("Jobs")  // Access the 'jobs' collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Use final array to hold total food expense amount
                            final double[] totalEntertainmentExpenseAmount = {0.0}; // Using an array to hold the total
                            entertainmentExpenses.clear(); // Clear old data

                            // Iterate through each job document
                            for (DocumentSnapshot jobDocument : task.getResult()) {
                                String jobId = jobDocument.getId();

                                // Access the 'expenses' subcollection for each job
                                db.collection("Jobs")
                                        .document(jobId)
                                        .collection("Expenses") // Expenses subcollection
                                        .whereEqualTo("description", "Entertainment")  // Filter for "Food" expenses
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    double jobFoodExpense = 0.0;

                                                    // Iterate through each food expense document
                                                    for (DocumentSnapshot expenseDocument : task.getResult()) {
                                                        Expense expense = expenseDocument.toObject(Expense.class);
                                                        if (expense != null) {
                                                            entertainmentExpenses.add(expense);
                                                            jobFoodExpense += expense.getAmount(); // Add to the total for this job
                                                        }
                                                    }

                                                    // Add this job's total food expense to the overall total
                                                    totalEntertainmentExpenseAmount[0] += jobFoodExpense;

                                                    // Update the RecyclerView after processing each job
                                                    adapter.notifyDataSetChanged();

                                                    // Update the total food expense UI
                                                    totalEntertainmentExpense.setText("BDT: " + totalEntertainmentExpenseAmount[0]);
                                                } else {
                                                    Log.e(TAG, "Error fetching expenses for job " + jobId, task.getException());
                                                }
                                            }
                                        });
                            }

                            if (entertainmentExpenses.isEmpty()) {
                                Log.d(TAG, "No Entertainment expenses found.");
                            }
                        } else {
                            Log.e(TAG, "Error fetching jobs", task.getException());
                        }
                    }
                });
    }
}
