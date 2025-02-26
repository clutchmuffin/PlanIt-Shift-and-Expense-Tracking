package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.myapplication.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.ExpenseSqlite;

public class UpdateActivity extends AppCompatActivity {

    EditText updateReason, updateBuy;
    Button updateButton;
    ExpenseSqlite sqlite;
    int id; // Store the expense ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);

        updateReason = findViewById(R.id.updateReason);
        updateBuy = findViewById(R.id.updateBuy);
        updateButton = findViewById(R.id.updateButton);
        sqlite = new ExpenseSqlite(this);

        // Retrieve and set intent data
        getIntentData();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExpense();
            }
        });
    }

    private void getIntentData() {
        if (getIntent().hasExtra("reason") && getIntent().hasExtra("amount") && getIntent().hasExtra("id")) {
            id = getIntent().getIntExtra("id", -1);
            String reason = getIntent().getStringExtra("reason");
            String buy = getIntent().getStringExtra("amount");

            updateReason.setText(reason);
            updateBuy.setText(buy);
        } else {
            Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateExpense() {
        String newReason = updateReason.getText().toString().trim();
        String newBuy = updateBuy.getText().toString().trim();

        if (!newReason.isEmpty() && !newBuy.isEmpty() && id != -1) {
            sqlite.updateData(newBuy, newReason, String.valueOf(id));
            Toast.makeText(this, "Data Successfully Updated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UpdateActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
