package com.example.bariaxorjak;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    
    // UI Components
    private TextView welcomeText, userEmailText;
    private MaterialButton browseMenuButton, logoutButton;
    
    // User email (received from login)
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        // TODO: Initialize Firebase Auth when google-services.json is added
        // mAuth = FirebaseAuth.getInstance();
        
        // Get user email from login intent
        if (getIntent() != null && getIntent().hasExtra("USER_EMAIL")) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        } else {
            userEmail = "demo@foodapp.com"; // Default demo email
        }
        
        // Initialize UI components
        initViews();
        
        // Display user information
        displayUserInfo();
        
        // Set click listeners
        setupClickListeners();
    }
    
    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        userEmailText = findViewById(R.id.userEmailText);
        browseMenuButton = findViewById(R.id.browseMenuButton);
        logoutButton = findViewById(R.id.logoutButton);
    }
    
    private void displayUserInfo() {
        userEmailText.setText("Logged in as: " + userEmail);
        Log.d(TAG, "User logged in: " + userEmail);
    }
    
    private void setupClickListeners() {
        browseMenuButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, 
                "Browse Menu feature coming soon!", 
                Toast.LENGTH_SHORT).show();
            // TODO: Navigate to menu/browse food items
        });
        
        logoutButton.setOnClickListener(v -> performLogout());
    }
    
    private void performLogout() {
        Toast.makeText(HomeActivity.this, 
            "Logged out successfully", 
            Toast.LENGTH_SHORT).show();
        
        // Navigate back to login screen
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
