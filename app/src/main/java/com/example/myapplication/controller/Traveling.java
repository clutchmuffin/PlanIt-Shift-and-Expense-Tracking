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

public class Traveling extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense> travelingExpenses;
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

        // Load expenses when "Show All Data" is clicked
        findViewById(R.id.expenseTravelingShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTravelingExpenses();
            }
        });
    }

    private void loadTravelingExpenses() {
        db.collection("Jobs")  // Fix collection name to lowercase
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final double[] totalTravelingExpenseAmount = {0.0}; // Array for total amount
                            travelingExpenses.clear(); // Clear previous data
                            final int[] jobsProcessed = {0}; // Track processed jobs

                            List<DocumentSnapshot> jobDocuments = task.getResult().getDocuments();
                            if (jobDocuments.isEmpty()) {
                                Log.d(TAG, "No jobs found.");
                                totalTravelingExpense.setText("BDT: 0.0");
                                return;
                            }

                            for (DocumentSnapshot jobDocument : jobDocuments) {
                                String jobId = jobDocument.getId();

                                // Access the 'expenses' subcollection for each job
                                db.collection("Jobs")  // Fix collection name to lowercase
                                        .document(jobId)
                                        .collection("Expenses")  // Fix collection name to lowercase
                                        .whereEqualTo("description", "Traveling")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    double jobTravelingExpense = 0.0;

                                                    for (DocumentSnapshot expenseDocument : task.getResult()) {
                                                        Expense expense = expenseDocument.toObject(Expense.class);
                                                        if (expense != null) {
                                                            travelingExpenses.add(expense);
                                                            jobTravelingExpense += expense.getAmount();
                                                        }
                                                    }

                                                    totalTravelingExpenseAmount[0] += jobTravelingExpense;
                                                    jobsProcessed[0]++;

                                                    // Check if all jobs have been processed before updating UI
                                                    if (jobsProcessed[0] == jobDocuments.size()) {
                                                        adapter.notifyDataSetChanged();
                                                        totalTravelingExpense.setText("BDT: " + totalTravelingExpenseAmount[0]);
                                                    }
                                                } else {
                                                    Log.e(TAG, "Error fetching expenses for job " + jobId, task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "Error fetching jobs", task.getException());
                        }
                    }
                });
    }
}
