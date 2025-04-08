package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.SharedCal;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SharingJoinActivity extends AppCompatActivity {
    private Button joinButton;
    private TextInputEditText input;
    private String currentUserId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_join);

        joinButton = findViewById(R.id.join);
        input = findViewById(R.id.codeInput);

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PlanITPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        // If no user is logged in, redirect to login
        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        joinButton.setOnClickListener(v -> {
            if (input.getText() != null) {
                String code = input.getText().toString();

                db.collection("Shared").whereEqualTo("code", code)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    SharedCal shared = document.toObject(SharedCal.class);
                                    shared.addMember(currentUserId);
                                    DocumentReference ref = db.collection("Shared").document(shared.getSharedId());
                                    ref.update("members", shared.getMembers());
                                    Intent intent = new Intent(v.getContext(), SharedCalendarActivity.class);
                                    intent.putExtra(SharedCalendarActivity.EXTRA_SHARED, shared);
                                    v.getContext().startActivity(intent);
                                }
                            } else {
                                Log.e("SharingMainActivity", "Error getting documents: ", task.getException());
                            }
                        });
            }
        });
    }
}
