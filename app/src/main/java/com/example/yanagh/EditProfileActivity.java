package com.example.yanagh;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.yanagh.databinding.ActivityEditProfileBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;


public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupProfilePhotoPreview();
        setupClickListeners();
        loadProfile();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupProfilePhotoPreview() {
        binding.etProfilePhotoUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String url = s != null ? s.toString().trim() : "";
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    Glide.with(EditProfileActivity.this)
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.ivProfilePhoto);
                } else {
                    binding.ivProfilePhoto.setImageResource(R.drawable.ic_person);
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        String uid = user.getUid();
        showLoading(true);

        firestore.collection(UserProfile.COLLECTION_NAME)
            .document(uid)
            .get()
            .addOnSuccessListener(doc -> {
                showLoading(false);
                if (doc.exists()) {
                    UserProfile profile = doc.toObject(UserProfile.class);
                    if (profile != null) fillForm(profile);
                } else {
                    fillFormFromAuth();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, getString(R.string.error_profile_load), Toast.LENGTH_LONG).show();
                finish();
            });
    }

    private void fillForm(UserProfile profile) {
        binding.etName.setText(profile.getDisplayName());
        binding.etEmail.setText(profile.getEmail());
        binding.etProfilePhotoUrl.setText(profile.getProfilePhotoUrl());
        
        String photoUrl = profile.getProfilePhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivProfilePhoto);
        }
    }

    private void fillFormFromAuth() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        binding.etName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
        binding.etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        binding.etProfilePhotoUrl.setText("");
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String photoUrl = binding.etProfilePhotoUrl.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_empty_name));
            return;
        }
        binding.tilName.setError(null);

        showLoading(true);
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        String uid = user.getUid();
        String email = user.getEmail() != null ? user.getEmail() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put(UserProfile.FIELD_DISPLAY_NAME, name);
        updates.put(UserProfile.FIELD_EMAIL, email);
        updates.put(UserProfile.FIELD_PROFILE_PHOTO_URL, photoUrl);
        updates.put(UserProfile.FIELD_UPDATED_AT, Timestamp.now());

        firestore.collection(UserProfile.COLLECTION_NAME)
            .document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener(aVoid -> updateAuthDisplayName(name))
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, getString(R.string.error_profile_save), Toast.LENGTH_LONG).show();
            });
    }

    private void updateAuthDisplayName(String name) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
                
            user.updateProfile(request)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    Toast.makeText(this, getString(R.string.success_profile_saved), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
        } else {
            showLoading(false);
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!show);
    }
}
