package com.example.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.SharedCal;
import com.example.myapplication.view.adapter.SharedAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton fabNewShared;
    private List<SharedCal> sharedCals;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharingmain);

        topAppBar = findViewById(R.id.topAppBar);
        sharedRecyclerView = findViewById(R.id.sharedRecyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        fabNewShared = findViewById(R.id.fabNewShared);

        sharedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedCals = new ArrayList<>();

        // not actually implemented in database yet -> finish NewShared
        db.collection("Shared").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SharedCal shared = document.toObject(SharedCal.class);
                    sharedCals.add(shared);
                }
                sharedListAdapter.notifyDataSetChanged();
            } else {
                Log.e("MainActivity", "Error getting documents: ", task.getException());
            }
        });

        sharedListAdapter = new SharedAdapter(sharedCals);
        sharedRecyclerView.setAdapter(sharedListAdapter);

        fabNewShared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SharingMainActivity.this, NewSharedActivity.class));
            }
        });

    }
}
