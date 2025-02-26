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

public class Shopping extends AppCompatActivity {
    public static boolean EXPENSE = true;

    TextView expenseShoppingShow, addShoppingExpense, totalShoppingExpense;
    ExpenseSqlite sqlite;
    //String food;
    Context context;

    //MainActivity mainAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);

        addShoppingExpense = findViewById(R.id.addShoppingExpense);
        expenseShoppingShow = findViewById(R.id.expenseShoppingShow);
        totalShoppingExpense = findViewById(R.id.totalShoppingExpense);

        sqlite = new ExpenseSqlite(this);

        // Calculate and show total food expenses
        //double total = calculateTotalFoodExpense();
        //totalFoodExpense.setText("BDT : " + sqlite.showFoodExpenses("Food"));


        addShoppingExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddActivity.EXPENSE = true;
                startActivity(new Intent(Shopping.this, AddActivity.class));


            }
        });

        expenseShoppingShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerViewActivity.REC_VIEW=true;
                //startActivity(new Intent(Food.this,RecyclerViewActivity.class));
                Intent intent = new Intent(Shopping.this, RecyclerViewActivity.class);
                intent.putExtra("reason", "Shopping");
                startActivity(intent);


                showData();
            }
        });

    }

    public void showData(){

        totalShoppingExpense.setText("BDT: " +sqlite.ShoppingExpense("Shopping"));
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
