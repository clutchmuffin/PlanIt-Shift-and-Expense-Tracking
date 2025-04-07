package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.SharedCal;
import com.example.myapplication.view.adapter.SharedAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SharingMainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MaterialToolbar topAppBar;
    private RecyclerView sharedRecyclerView;
    private SharedAdapter sharedListAdapter;
    private BottomNavigationView bottomNav;
    private Button createbutton;
    private Button joinbutton;
    private List<SharedCal> sharedCals;
    private String currentUserId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharingmain);

        topAppBar = findViewById(R.id.topAppBar);
        sharedRecyclerView = findViewById(R.id.sharedRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        createbutton = findViewById(R.id.createbutton);
        joinbutton = findViewById(R.id.joinbutton);

        sharedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupBottomNavigation();

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        // If no user is logged in, redirect to login
        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        sharedCals = new ArrayList<>();

        db.collection("Shared").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SharedCal shared = document.toObject(SharedCal.class);
                    for (String mem : shared.getMembers()) {
                        if (Objects.equals(mem, currentUserId)) {
                            sharedCals.add(shared);
                        }
                    }
                }
                sharedListAdapter.notifyDataSetChanged();
            } else {
                Log.e("MainActivity", "Error getting documents: ", task.getException());
            }
        });

        sharedListAdapter = new SharedAdapter(sharedCals);
        sharedRecyclerView.setAdapter(sharedListAdapter);

        createbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SharingMainActivity.this, NewSharedActivity.class));
                sharedListAdapter.notifyDataSetChanged();
            }
        });

        joinbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SharingMainActivity.this, SharingJoinActivity.class));
            }
        });

    }


    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_jobs) {
                Intent intent = new Intent(SharingMainActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (itemId == R.id.nav_budget) {
                startActivity(new Intent(SharingMainActivity.this, BudgetMainActivity.class));
                return true;
            } else if (itemId == R.id.nav_sharing) {
                // already here
                return true;
            }
            return false;
        });
    }
}
