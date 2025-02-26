package com.example.myapplication.model;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.example.myapplication.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.controller.AddActivity;
import com.example.myapplication.controller.RecyclerViewActivity;

public class Food extends AppCompatActivity {
    public static boolean EXPENSE = true;

    TextView expenseFoodShow, addExpense, totalFoodExpense;
    ExpenseSqlite sqlite;
    //String food;
    Context context;

    // MainActivity mainAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food);

        addExpense = findViewById(R.id.addExpense);
        expenseFoodShow = findViewById(R.id.expenseFoodShow);
        totalFoodExpense = findViewById(R.id.totalFoodExpense);

        sqlite = new ExpenseSqlite(this);

        // Calculate and show total food expenses
        //double total = calculateTotalFoodExpense();
        //totalFoodExpense.setText("BDT : " + sqlite.showFoodExpenses("Food"));


        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddActivity.EXPENSE = true;
                startActivity(new Intent(Food.this, AddActivity.class));


            }
        });

        expenseFoodShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerViewActivity.REC_VIEW=true;
                //startActivity(new Intent(Food.this,RecyclerViewActivity.class));
                Intent intent = new Intent(Food.this, RecyclerViewActivity.class);
                intent.putExtra("reason", "Food");
                startActivity(intent);


                showData();
            }
        });

    }

    public void showData(){

        totalFoodExpense.setText("BDT: " +sqlite.FoodExpense("Food"));
        //totalIncome.setText("BDT: "+sqlite.showIncome());

        //double balance = sqlite.showIncome() - sqlite.showExpense();

        //mainBalance.setText("BDT : "+balance);
    }



    @Override
    protected void onResume(){
        showData();
        super.onResume();
    }

}
