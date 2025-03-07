package com.example.myapplication.model;

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

public class Shopping extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense> shoppingExpenses;
    private TextView totalShoppingExpense;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ShoppingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        totalShoppingExpense = findViewById(R.id.totalShoppingExpense);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize expense list and adapter
        shoppingExpenses= new ArrayList<>();
        adapter = new ExpenseListAdapter(shoppingExpenses);
        recyclerView.setAdapter(adapter);



        // Load expenses when "Show All Data" is clicked
        findViewById(R.id.expenseShoppingShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadShoppingExpenses();
            }
        });
    }

    private void loadShoppingExpenses() {
        db.collection("Jobs")  // Access the 'jobs' collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Use final array to hold total food expense amount
                            final double[] totalShoppingExpenseAmount = {0.0}; // Using an array to hold the total
                            shoppingExpenses.clear(); // Clear old data

                            // Iterate through each job document
                            for (DocumentSnapshot jobDocument : task.getResult()) {
                                String jobId = jobDocument.getId();

                                // Access the 'expenses' subcollection for each job
                                db.collection("Jobs")
                                        .document(jobId)
                                        .collection("Expenses") // Expenses subcollection
                                        .whereEqualTo("description", "Shopping")  // Filter for "Food" expenses
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
                                                            shoppingExpenses.add(expense);
                                                            jobFoodExpense += expense.getAmount(); // Add to the total for this job
                                                        }
                                                    }

                                                    // Add this job's total food expense to the overall total
                                                    totalShoppingExpenseAmount[0] += jobFoodExpense;

                                                    // Update the RecyclerView after processing each job
                                                    adapter.notifyDataSetChanged();

                                                    // Update the total food expense UI
                                                    totalShoppingExpense.setText("BDT: " + totalShoppingExpenseAmount[0]);
                                                } else {
                                                    Log.e(TAG, "Error fetching expenses for job " + jobId, task.getException());
                                                }
                                            }
                                        });
                            }

                            if (shoppingExpenses.isEmpty()) {
                                Log.d(TAG, "No Entertainment expenses found.");
                            }
                        } else {
                            Log.e(TAG, "Error fetching jobs", task.getException());
                        }
                    }
                });
    }
}
