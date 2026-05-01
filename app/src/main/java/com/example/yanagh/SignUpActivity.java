package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.yanagh.api.MailjetMailService;
import com.example.yanagh.databinding.ActivitySignupBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends BaseActivity {
    
    private ActivitySignupBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupClickListeners();
    }
    
    private void setupClickListeners() {
        binding.btnSignUp.setOnClickListener(v -> performSignUp());
        
        binding.tvLoginLink.setOnClickListener(v -> finish());
        
        binding.ivBack.setOnClickListener(v -> finish());
    }
    
    private void performSignUp() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        
        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }
        
        showLoading(true);
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    updateUserProfile(name);
                } else {
                    showLoading(false);
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : getString(R.string.error_signup_failed);
                    showError(errorMessage);
                }
            });
    }
    
    private void updateUserProfile(String name) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build();

        user.updateProfile(profileUpdates)
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    showLoading(false);
                    // Even if profile update fails, we should still try to send verification
                }
                
                String uid = user.getUid();
                String email = user.getEmail() != null ? user.getEmail() : "";
                
                Map<String, Object> data = new HashMap<>();
                data.put(UserProfile.FIELD_DISPLAY_NAME, name);
                data.put(UserProfile.FIELD_EMAIL, email);
                data.put(UserProfile.FIELD_PROFILE_PHOTO_URL, "");
                data.put(UserProfile.FIELD_UPDATED_AT, Timestamp.now());

                firestore.collection(UserProfile.COLLECTION_NAME)
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .addOnCompleteListener(task2 -> {
                        user.sendEmailVerification()
                            .addOnCompleteListener(verifyTask -> {
                                showLoading(false);
                                if (verifyTask.isSuccessful()) {
                                    Toast.makeText(this, "Verification email sent. Please check your inbox and verify your email before logging in.", Toast.LENGTH_LONG).show();
                                    auth.signOut(); // Log out immediately after registration
                                    navigateToLogin();
                                } else {
                                    showError("Failed to send verification email. Please try again later.");
                                }
                            });
                    });
            });
    }
    
    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_empty_name));
            isValid = false;
        }
        
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_short_password));
            isValid = false;
        }
        
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            isValid = false;
        }
        
        return isValid;
    }
    
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSignUp.setEnabled(!isLoading);
        binding.etName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
