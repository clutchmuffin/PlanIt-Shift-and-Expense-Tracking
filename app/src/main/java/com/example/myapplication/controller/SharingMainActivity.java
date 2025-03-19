package com.example.myapplication.controller;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.SharedCard;
import com.example.myapplication.view.adapter.JobListAdapter;
import com.example.myapplication.view.adapter.SharedAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SharingMainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MaterialToolbar topAppBar;
    private RecyclerView sharedRecyclerView;
    private SharedAdapter sharedListAdapter;
    private BottomNavigationView bottomNav;
    private List<SharedCard> sharedCals;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharingmain);

        topAppBar = findViewById(R.id.topAppBar);
        sharedRecyclerView = findViewById(R.id.sharedRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);

        sharedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedCals = new ArrayList<>();

        // need to check with team
        db.collection("Jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    sharedCals.add(job);
                }
                sharedListAdapter.notifyDataSetChanged();
            } else {
                Log.e("MainActivity", "Error getting documents: ", task.getException());
            }
        });

        sharedListAdapter = new SharedAdapter(sharedCals);
        sharedRecyclerView.setAdapter(sharedListAdapter);

    }
}
