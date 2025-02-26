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

public class Traveling extends AppCompatActivity {
    public static boolean EXPENSE = true;

    TextView expenseTravelingShow, addTravelingExpense, totalTravelingExpense;
    ExpenseSqlite sqlite;
    //String food;
    Context context;

  //  MainActivity mainAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_traveling);

        addTravelingExpense = findViewById(R.id.addTravelingExpense);
        expenseTravelingShow = findViewById(R.id.expenseTravelingShow);
        totalTravelingExpense = findViewById(R.id.totalTravelingExpense);

        sqlite = new ExpenseSqlite(this);

        // Calculate and show total food expenses
        //double total = calculateTotalFoodExpense();
        //totalFoodExpense.setText("BDT : " + sqlite.showFoodExpenses("Food"));


        addTravelingExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddActivity.EXPENSE = true;
                startActivity(new Intent(Traveling.this, AddActivity.class));


            }
        });

        expenseTravelingShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerViewActivity.REC_VIEW=true;
                //startActivity(new Intent(Food.this,RecyclerViewActivity.class));
                Intent intent = new Intent(Traveling.this, RecyclerViewActivity.class);
                intent.putExtra("reason", "Traveling");
                startActivity(intent);


                showData();
            }
        });

    }

    public void showData(){

        totalTravelingExpense.setText("BDT: " +sqlite.TravelingExpense("Traveling"));
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
