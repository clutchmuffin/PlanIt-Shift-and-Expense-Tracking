package com.example.myapplication.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;


public class NewShared extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNav;
    private TextInputEditText nameInput;
    private RecyclerView jobListSelectable;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        bottomNav = findViewById(R.id.bottomNav);
        topAppBar = findViewById(R.id.topAppBar);
        nameInput = findViewById(R.id.nameInput);
        jobListSelectable = findViewById(R.id.jobListSelectable);


    }
}
