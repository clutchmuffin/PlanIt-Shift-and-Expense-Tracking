package com.example.myapplication.model;

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

public class Entertainment extends AppCompatActivity {
    public static boolean EXPENSE = true;

    TextView expenseEntertainmentShow, addEntertainmentExpense, totalEntertainmentExpense;
    ExpenseSqlite sqlite;
    //String food;
    Context context;

    //MainActivity mainAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entertainment);

        addEntertainmentExpense = findViewById(R.id.addEntertainmentExpense);
        expenseEntertainmentShow = findViewById(R.id.expenseEntertainmentShow);
        totalEntertainmentExpense = findViewById(R.id.totalEntertainmentExpense);

        sqlite = new ExpenseSqlite(this);

        // Calculate and show total food expenses
        //double total = calculateTotalFoodExpense();
        //totalFoodExpense.setText("BDT : " + sqlite.showFoodExpenses("Food"));


        addEntertainmentExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddActivity.EXPENSE = true;
                startActivity(new Intent(Entertainment.this, AddActivity.class));


            }
        });

        expenseEntertainmentShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerViewActivity.REC_VIEW=true;
                //startActivity(new Intent(Food.this,RecyclerViewActivity.class));
                Intent intent = new Intent(Entertainment.this, RecyclerViewActivity.class);
                intent.putExtra("reason", "Entertainment");
                startActivity(intent);


                showData();
            }
        });

    }

    public void showData(){

        totalEntertainmentExpense.setText("BDT: " +sqlite.EntertainmentExpense("Entertainment"));
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
