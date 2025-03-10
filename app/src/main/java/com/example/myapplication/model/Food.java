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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class Food extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense> foodExpenses;
    private TextView totalFoodExpense;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FoodActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        totalFoodExpense = findViewById(R.id.totalFoodExpense);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize expense list and adapter
        foodExpenses = new ArrayList<>();
        adapter = new ExpenseListAdapter(foodExpenses);
        recyclerView.setAdapter(adapter);

        //Immediately show Food Expenses
        loadFoodExpenses();

        // Load expenses when "Show All Data" is clicked
        findViewById(R.id.expenseFoodShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFoodExpenses();
            }
        });
    }

private void loadFoodExpenses() {
    db.collection("Jobs")  // Access the 'Jobs' collection
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    final double[] totalFoodExpenseAmount = {0.0}; // Using an array to hold the total
                    foodExpenses.clear(); // Clear old data

                    // List to keep track of tasks for parallel execution
                    List<Task<QuerySnapshot>> expenseFetchTasks = new ArrayList<>();

                    // Iterate through each job document and trigger parallel requests for expenses
                    for (DocumentSnapshot jobDocument : task.getResult()) {
                        String jobId = jobDocument.getId(); // Get the job ID

                        // Fetch expenses for this job in parallel
                        Task<QuerySnapshot> expenseTask = db.collection("Jobs")
                                .document(jobId)
                                .collection("Expenses")
                                .whereEqualTo("description", "Food")
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
                                            Expense expense = expenseDocument.toObject(Expense.class);
                                            if (expense != null) {
                                                foodExpenses.add(expense);
                                                jobFoodExpense += expense.getAmount(); // Add to the total for this job
                                            }
                                        }

                                        // Add this job's total food expense to the overall total
                                        totalFoodExpenseAmount[0] += jobFoodExpense;
                                    } else {
                                        Log.e(TAG, "Error fetching expenses", expenseTask.getException());
                                    }
                                }

                                // Update the RecyclerView and total only after all tasks are completed
                                adapter.notifyDataSetChanged();
                                totalFoodExpense.setText("BDT: " + totalFoodExpenseAmount[0]);
                            });
                } else {
                    Log.e(TAG, "Error fetching jobs", task.getException());
                }
            });
}


}
