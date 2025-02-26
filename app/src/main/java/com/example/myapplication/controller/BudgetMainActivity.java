package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Entertainment;
import com.example.myapplication.model.ExpenseSqlite;
import com.example.myapplication.model.Food;
import com.example.myapplication.R;
import com.example.myapplication.model.Shopping;
import com.example.myapplication.model.Traveling;

public class BudgetMainActivity extends AppCompatActivity {

    TextView mainBalance;

    Button traveling, food, shopping, entertainment, updateBudget;
    ExpenseSqlite sqlite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budgetmain);

        sqlite = new ExpenseSqlite(this);

        mainBalance = findViewById(R.id.mainBalance);
        //totalExpense = findViewById(R.id.totalExpense);
        // addExpense = findViewById(R.id.addExpense);
        //addIncome = findViewById(R.id.addIncome);
        traveling = findViewById(R.id.traveling);
        food = findViewById(R.id.food);
        shopping = findViewById(R.id.shopping);
        entertainment = findViewById(R.id.entertainment);
        updateBudget = findViewById(R.id.updateBudget);
        //showExpense = findViewById(R.id.expenseShow);
        // showIncome = findViewById(R.id.incomeShow);
        //totalIncome = findViewById(R.id.totalIncome);


        updateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BudgetMainActivity.this,AddActivity.class));
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
/*
        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddActivity.EXPENSE = true;
                startActivity(new Intent(MainActivity.this, AddActivity.class));


            }
        });

        addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddActivity.EXPENSE = false;
                startActivity(new Intent(MainActivity.this, AddActivity.class));


            }
        });

        showExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerViewActivity.REC_VIEW=true;
                startActivity(new Intent(MainActivity.this,RecyclerViewActivity.class));


                showData();
            }
        });


        showIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerViewActivity.REC_VIEW=false;
                startActivity(new Intent(MainActivity.this,RecyclerViewActivity.class));


                showData();
            }
        });

*/


    }


    @Override
    protected void onResume() {
        //showData();
        super.onResume();
    }
}

