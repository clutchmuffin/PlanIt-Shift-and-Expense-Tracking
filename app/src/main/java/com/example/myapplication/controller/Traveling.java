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
import com.example.myapplication.model.EXP;
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

public class Traveling extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<EXP> travelingExpenses;
    private TextView totalTravelingExpense;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "TravelingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveling);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        totalTravelingExpense = findViewById(R.id.totalTravelingExpense);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize expense list and adapter
        travelingExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter(travelingExpenses);
        recyclerView.setAdapter(adapter);

        // Load expenses immediately when this activity opens
        loadTravelingExpenses();
        showMainBalance();

        // Load expenses when "Show All Data" is clicked
        findViewById(R.id.expenseTravelingShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTravelingExpenses();
            }
        });
    }


    private void loadTravelingExpenses() {
        db.collection("Jobs")  // Access the 'Jobs' collection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final double[] totalTravelingExpenseAmount = {0.0}; // Using an array to hold the total
                        travelingExpenses.clear(); // Clear old data

                        // List to keep track of tasks for parallel execution
                        List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                        // Iterate through each job document and trigger parallel requests for expenses
                        for (DocumentSnapshot jobDocument : task.getResult()) {
                            String jobId = jobDocument.getId(); // Get the job ID

                            // Fetch expenses for this job in parallel
                            Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                    .document(jobId)
                                    .collection("EXP")
                                    .whereEqualTo("description", "Traveling")
                                    .get();

                            expenseFetchTasks.add(expenseTask);
                        }

                        // When all expense fetch operations are completed, process the results
                        Tasks.whenAllComplete(expenseFetchTasks)
                                .addOnCompleteListener(allTask -> {
                                    for (Task<QuerySnapshot> expenseTask : expenseFetchTasks) {
                                        if (expenseTask.isSuccessful()) {
                                            double jobFoodExpense = 0.0;

                                            // Iterate through each food expense document
                                            for (DocumentSnapshot expenseDocument : expenseTask.getResult()) {
                                                EXP expense = expenseDocument.toObject(EXP.class);
                                                if (expense != null) {
                                                    travelingExpenses.add(expense);
                                                    jobFoodExpense += expense.getAmount(); // Add to the total for this job
                                                }
                                            }

                                            // Add this job's total food expense to the overall total
                                            totalTravelingExpenseAmount[0] += jobFoodExpense;
                                        } else {
                                            Log.e(TAG, "Error fetching expenses", expenseTask.getException());
                                        }
                                    }

                                    // Update the RecyclerView and total only after all tasks are completed
                                    adapter.notifyDataSetChanged();
                                    totalTravelingExpense.setText("BDT: " + totalTravelingExpenseAmount[0]);
                                    updateBudgetTotal(totalTravelingExpenseAmount[0]);

                                });
                    } else {
                        Log.e(TAG, "Error fetching jobs", task.getException());
                    }
                });
    }

    private void updateBudgetTotal(double totalExp) {
        // Reference the "Food" document in the "Budgy" collection
        db.collection("Budgy").document("Traveling")
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
        db.collection("Budgy").document("Traveling")
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
