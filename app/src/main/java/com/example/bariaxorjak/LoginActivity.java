package com.example.bariaxorjak;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    // UI Components
    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView signupLinkText, forgotPasswordText;
    private ProgressBar progressBar;
    
    // Demo credentials (replace with Firebase later)
    private static final String DEMO_EMAIL = "test@foodapp.com";
    private static final String DEMO_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        // TODO: Initialize Firebase Auth when google-services.json is added
        // mAuth = FirebaseAuth.getInstance();
        
        // Check if user is already logged in (using SharedPreferences later)
        // For now, just show login screen
        
        // Initialize UI components
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }
    
    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupLinkText = findViewById(R.id.signupLinkText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        
        signupLinkText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
        
        forgotPasswordText.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, 
                "Password reset feature coming soon", 
                Toast.LENGTH_SHORT).show();
        });
    }
    
    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            emailEditText.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            emailEditText.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_short_password));
            passwordEditText.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.error_short_password));
            passwordEditText.requestFocus();
            return;
        }
        
        // Show progress bar and disable button
        showLoading(true);
        
        // Simulate login delay (replace with Firebase later)
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoading(false);
                
                // Demo login check (replace with Firebase authentication)
                if (email.equals(DEMO_EMAIL) && password.equals(DEMO_PASSWORD)) {
                    // Login successful
                    Log.d(TAG, "Demo login successful");
                    
                    Toast.makeText(LoginActivity.this, 
                        getString(R.string.success_login), 
                        Toast.LENGTH_SHORT).show();
                    
                    // Navigate to home screen
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("USER_EMAIL", email); // Pass email to home
                    startActivity(intent);
                    finish();
                } else {
                    // Login failed
                    Log.w(TAG, "Invalid credentials");
                    Toast.makeText(LoginActivity.this, 
                        "Invalid credentials. Try:\nEmail: " + DEMO_EMAIL + "\nPassword: " + DEMO_PASSWORD,
                        Toast.LENGTH_LONG).show();
                }
            }
        }, 1500); // 1.5 second delay
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
    }
}
