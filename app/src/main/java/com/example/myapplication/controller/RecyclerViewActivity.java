package com.example.myapplication.controller;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.myapplication.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.ExpenseModel;
import com.example.myapplication.model.ExpenseSqlite;
import com.example.myapplication.view.adapter.ExpenseAdapter;

import java.util.ArrayList;


public class RecyclerViewActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    ExpenseAdapter adapter;
    ArrayList<ExpenseModel> arrayList = new ArrayList<>();

    TextView edBuy, edReason;
    Button updtButton, delButton;
    ExpenseSqlite sqlite;
    public static  boolean REC_VIEW = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updtButton= findViewById(R.id.updtButton);
        //updtButton = findViewById(R.id.delButton);
        edBuy= findViewById(R.id.edBuy);
        edReason = findViewById(R.id.edReason);
        sqlite = new ExpenseSqlite(this);

        String reasonn = getIntent().getStringExtra("reason");

        loadData(reasonn);

    }

    public void loadData(String reasonn){

        // Cursor cursor = null;

        Cursor cursor = sqlite.showExpenseRecyclerView(reasonn);
        /*if(REC_VIEW==true) {


            cursor = sqlite.showExpenseRecyclerView();
        }else{

            cursor = sqlite.showIncomeRecyclerView();

        }*/
        if(cursor!= null && cursor.getCount()>0){
            while(cursor.moveToNext()) {

                int id = cursor.getInt(0);
                String buy = cursor.getString(1);
                String reason = cursor.getString(2);

                arrayList.add(new ExpenseModel(id,buy,reason));

            }


            adapter = new ExpenseAdapter(arrayList,RecyclerViewActivity.this);
            recyclerView.setAdapter(adapter);



        }
    }




}
