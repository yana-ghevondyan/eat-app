package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.example.yanagh.databinding.ActivityLoginBinding;
import com.example.yanagh.data.UserPrefs;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class LoginActivity extends BaseActivity {
    
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        
        setupClickListeners();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                navigateToMain();
            } else {
                auth.signOut();
            }
        }
    }
    
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> performEmailLogin());
        
        binding.btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());
        
        binding.tvSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
        
        binding.tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }
    
    private void performEmailLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        if (!validateInputs(email, password)) {
            return;
        }
        
        showLoading(true);
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        showLoading(false);
                        navigateToMain();
                    } else {
                        showLoading(false);
                        auth.signOut();
                        showError("Please verify your email address before logging in. Check your inbox.");
                    }
                } else {
                    showLoading(false);
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : getString(R.string.error_login_failed);
                    showError(errorMessage);
                }
            });
    }
    
    private void performGoogleSignIn() {
        showLoading(true);
        
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build();
        
        GetCredentialRequest request = new GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build();

        credentialManager.getCredentialAsync(
            this,
            request,
            null,
            executor,
            new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    runOnUiThread(() -> handleSignInResult(result));
                }

                @Override
                public void onError(@NonNull GetCredentialException e) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        showError(getString(R.string.error_google_signin_failed));
                    });
                }
            }
        );
    }
    
    private void handleSignInResult(GetCredentialResponse result) {
        androidx.credentials.Credential credential = result.getCredential();
        
        if (credential instanceof androidx.credentials.CustomCredential) {
            androidx.credentials.CustomCredential customCredential = (androidx.credentials.CustomCredential) credential;
            if (customCredential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                    String idToken = googleIdTokenCredential.getIdToken();
                    firebaseAuthWithGoogle(idToken);
                } catch (Exception e) {
                    showLoading(false);
                    showError(getString(R.string.error_google_signin_failed));
                }
            } else {
                showLoading(false);
                showError(getString(R.string.error_google_signin_failed));
            }
        } else {
            showLoading(false);
            showError(getString(R.string.error_google_signin_failed));
        }
    }
    
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    // Google users are usually considered verified by default, 
                    // but we can still check or just let them in.
                    navigateToMain();
                } else {
                    showError(task.getException() != null ? 
                        task.getException().getMessage() : getString(R.string.error_google_signin_failed));
                }
            });
    }
    
    private void handleForgotPassword() {
        String email = binding.etEmail.getText().toString().trim();
        
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        
        showLoading(true);
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    Toast.makeText(this, getString(R.string.success_password_reset), Toast.LENGTH_LONG).show();
                } else {
                    showError(task.getException() != null ? task.getException().getMessage() : "Failed to send reset email");
                }
            });
    }
    
    private boolean validateInputs(String email, String password) {
        boolean isValid = true;
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        
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
        
        return isValid;
    }
    
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnGoogleSignIn.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void navigateToMain() {
        Class<?> destination = UserPrefs.isOnboardingDone(this) ? FoodMainActivity.class : OnboardingActivity.class;
        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
