package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.Food;

public class SetBudget extends AppCompatActivity {

    EditText shoppingBudget, foodBudget, entertainmentBudget, travelingBudget;
    TextView addBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_budget);

        shoppingBudget = findViewById(R.id.shoppingBudget);
        foodBudget = findViewById(R.id.foodBudget);
        entertainmentBudget = findViewById(R.id.foodBudget);
        travelingBudget = findViewById(R.id.travelingBudget);
        addBudget = findViewById(R.id.addBudget);


        addBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SetBudget.this, Food.class));


            }
        });
    }
}