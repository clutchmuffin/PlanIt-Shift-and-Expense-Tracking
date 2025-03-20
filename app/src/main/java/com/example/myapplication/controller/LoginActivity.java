package com.example.myapplication.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "PlanITPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_USERNAME = "userUsername";
    private static final String KEY_USER_EMAIL = "userEmail";

    private TextInputEditText nameEditText, usernameEditText, emailEditText;
    private MaterialButton loginButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        
        setContentView(R.layout.activity_login);
        initialize();
    }

    private void initialize() {
        nameEditText = findViewById(R.id.nameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> registerUser());
    }
    
    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        
        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }
        
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }
        
        // Disable login button to prevent multiple submissions
        loginButton.setEnabled(false);

        checkUserExists(name, username, email);
    }

    private void checkUserExists(String name, String username, String email) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    // Enable button if task failed
                    if (!task.isSuccessful()) {
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error checking for existing user", task.getException());
                        return;
                    }
    
                    // Create new user if no matching username found
                    if (task.getResult().isEmpty()) {
                        createNewUser(name, username, email);
                        return;
                    }
    
                    // Get user document
                    DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                    User existingUser = userDoc.toObject(User.class);
    
                    // Handle null user object
                    if (existingUser == null) {
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
    
                    // Verify email matches
                    if (!email.equals(existingUser.getEmail())) {
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Invalid email for this username", Toast.LENGTH_SHORT).show();
                        return;
                    }
    
                    // Login successful
                    saveUserToPrefs(
                            existingUser.getUserId(),
                            existingUser.getName(),
                            existingUser.getUsername(),
                            existingUser.getEmail()
                    );
                    Toast.makeText(LoginActivity.this, "Welcome back to PlanIT!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                });
    }
    
    // New method to handle new user creation
    private void createNewUser(String name, String username, String email) {
        // Create or increment the user ID counter in a transaction
        db.runTransaction(transaction -> {
            try {
                int newUserId;
                DocumentSnapshot counterDoc = transaction.get(db.collection("counters").document("users"));

                if (counterDoc.exists()) {
                    newUserId = counterDoc.getLong("nextId").intValue();
                    transaction.update(db.collection("counters").document("users"), "nextId", newUserId + 1);
                } else {
                    // First user, initialize counter
                    newUserId = 1;
                    transaction.set(db.collection("counters").document("users"),
                        java.util.Collections.singletonMap("nextId", 2));
                }

                return newUserId;
            } catch (Exception e) {
                Log.e(TAG, "Error in transaction", e);
                throw new RuntimeException(e);
            }

        }).addOnSuccessListener(userId -> {
            // Create a new user with the auto-incremented ID
            String userIdStr = String.valueOf(userId);
            User user = new User(userIdStr, name, username, email);

            // Add user to Firestore
            db.collection("users").document(userIdStr)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Save user data to SharedPreferences
                            saveUserToPrefs(userIdStr, name, username, email);
                            navigateToMainActivity();
                        } else {
                            // Re-enable login button
                            loginButton.setEnabled(true);
                            Log.e(TAG, "Error creating user", task.getException());
                        }
                    }
                });

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Re-enable login button
                loginButton.setEnabled(true);
                Log.e(TAG, "Error in transaction", e);
            }
        });
    }
    
    private void saveUserToPrefs(String userId, String name, String username, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_USERNAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
}