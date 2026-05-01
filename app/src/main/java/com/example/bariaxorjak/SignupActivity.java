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

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    
    // UI Components
    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton signupButton;
    private TextView backToLoginText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        
        // TODO: Initialize Firebase Auth when google-services.json is added
        // mAuth = FirebaseAuth.getInstance();
        
        // Initialize UI components
        initViews();
        
        // Set click listeners
        setupClickListeners();
    }
    
    private void initViews() {
        emailEditText = findViewById(R.id.signupEmailEditText);
        passwordEditText = findViewById(R.id.signupPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.signupConfirmPasswordEditText);
        signupButton = findViewById(R.id.signupButton);
        backToLoginText = findViewById(R.id.backToLoginText);
        progressBar = findViewById(R.id.signupProgressBar);
    }
    
    private void setupClickListeners() {
        signupButton.setOnClickListener(v -> attemptSignup());
        
        backToLoginText.setOnClickListener(v -> {
            finish(); // Go back to login activity
        });
    }
    
    private void attemptSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        
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
        
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please confirm your password");
            confirmPasswordEditText.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }
        
        // Show progress bar and disable button
        showLoading(true);
        
        // Simulate signup delay (replace with Firebase later)
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoading(false);
                
                // Demo signup - just show success message
                Log.d(TAG, "Demo signup successful: " + email);
                
                Toast.makeText(SignupActivity.this, 
                    getString(R.string.success_signup) + "\n\nNote: This is a demo. Add google-services.json to enable real authentication.",
                    Toast.LENGTH_LONG).show();
                
                // Navigate to home screen
                Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
                finish();
            }
        }, 1500); // 1.5 second delay
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signupButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
    }
}
