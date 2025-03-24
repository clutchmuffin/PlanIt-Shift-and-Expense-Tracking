package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Food;
import com.example.myapplication.R;
import com.example.myapplication.model.Shopping;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;


public class BudgetMainActivity extends AppCompatActivity {

    TextView mainBalance;

    Button traveling, food, shopping, entertainment, updateBudget;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    PieChart pieChart;

    long totalBudget = 0, totalExpenses = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budgetmain);


        pieChart = findViewById(R.id.pieMainChart);
        getBudgetData();


        // Button Click Listeners
        food = findViewById(R.id.food);
        shopping = findViewById(R.id.shopping);
        entertainment = findViewById(R.id.entertainment);
        traveling = findViewById(R.id.traveling);

        food.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Food.class)));
        shopping.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Shopping.class)));
        entertainment.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Entertainment.class)));
        traveling.setOnClickListener(v -> startActivity(new Intent(BudgetMainActivity.this, Traveling.class)));
        updateBudget = findViewById(R.id.updateBudget);

        updateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BudgetMainActivity.this,SetBudget.class));
            }
        });



        food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Food.class));


            }
        });

        shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Shopping.class));


            }
        });

        entertainment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Entertainment.class));


            }
        });

        traveling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BudgetMainActivity.this, Traveling.class));


            }
        });


    }


    @Override
    protected void onResume() {
        //showData();
        super.onResume();
    }

    //setting up the pie chart

    private void getBudgetData() {
        db.collection("Budgy").get().addOnSuccessListener(queryDocumentSnapshots -> {
            totalBudget = 0;
            totalExpenses = 0;

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                if (doc.contains("budget") && doc.contains("totalExpenses")) {
                    long budget = doc.getLong("budget");
                    long expenses = doc.getLong("totalExpenses");
                    totalBudget += budget;
                    totalExpenses += expenses;
                }
            }
            setUpGraph();
        });
    }

    private void setUpGraph() {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (totalExpenses > 0) {
            pieEntries.add(new PieEntry(totalExpenses, "Expenses"));
            colors.add(getResources().getColor(R.color.red));
        }
        if (totalBudget - totalExpenses > 0) {
            pieEntries.add(new PieEntry(totalBudget - totalExpenses, "Remaining"));
            colors.add(getResources().getColor(R.color.teal_700));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Budget Overview");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.white));
        pieDataSet.setValueTextSize(18f);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }




}

