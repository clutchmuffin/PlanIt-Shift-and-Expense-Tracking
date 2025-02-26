package com.example.myapplication.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.ExpenseSqlite;

public class AddActivity extends AppCompatActivity {

    TextView buyDisplay, reasonDisplay, button, addTv;

    EditText edBuy, edReason;



    ExpenseSqlite SQLiteOpenHelper;
    public static boolean EXPENSE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);


        button = findViewById(R.id.button);
        //addExpense = findViewById(R.id.addExpense);
        edBuy = findViewById(R.id.edBuy);
        edReason = findViewById(R.id.edReason);

        buyDisplay = findViewById(R.id.buyDisplay);
        reasonDisplay = findViewById(R.id.reasonDisplay);

        addTv = findViewById(R.id.addTv);

        SQLiteOpenHelper = new ExpenseSqlite(this);


        if (EXPENSE == true) {

            addTv.setText("Add Expense");
            buyDisplay.setText("How much do you want to spend: ");
            reasonDisplay.setText("What is your reason?");
            button.setText("Add expense to SQLite");


        } else {

            addTv.setText("Add Income");
            buyDisplay.setText("How much did you earn: ");
            reasonDisplay.setText("Where did you earn this money?");
            button.setText("Add income to SQLite");


        }

        button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edBuy.length() > 0 && edReason.length() > 0) {


                    String reason = edReason.getText().toString();
                    String buy = edBuy.getText().toString();
                    double amount = Double.parseDouble(buy);


                    if (EXPENSE == true) {

                        SQLiteOpenHelper.addExpense(amount, reason);
                        edBuy.setText("");
                        edReason.setText("");
                        Toast.makeText(AddActivity.this, "This data was successfully Added", Toast.LENGTH_LONG).show();

                    } else {

                        SQLiteOpenHelper.addIncome(amount, reason);
                        edBuy.setText("");
                        edReason.setText("");
                        Toast.makeText(AddActivity.this, "This data was successfully Added", Toast.LENGTH_LONG).show();

                    }
                } else {


                    Toast.makeText(AddActivity.this, "This edit text is empty!", Toast.LENGTH_LONG).show();

                }


            }
        }));


    }



}