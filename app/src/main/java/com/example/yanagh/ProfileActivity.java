package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import com.example.yanagh.databinding.ActivityProfileBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> loadProfile()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, R.string.please_sign_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupClickListeners();
        setupTopNavigation();
        loadProfile();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void setupTopNavigation() {
        TabLayout.Tab tab = binding.tabLayout.getTabAt(2);
        if (tab != null) tab.select();
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        startActivity(new Intent(ProfileActivity.this, AnalyzeFoodActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case 1:
                        startActivity(new Intent(ProfileActivity.this, FoodChatActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case 2: break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });
        
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                auth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton(getString(R.string.no), null)
            .show();
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
                    if (profile != null) displayProfile(profile);
                } else {
                    createProfileFromAuthThenLoad(uid);
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e("ProfileActivity", "Error loading profile", e);
                // Display the actual error message to help debug
                Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void createProfileFromAuthThenLoad(String uid) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        
        Map<String, Object> data = new HashMap<>();
        data.put(UserProfile.FIELD_DISPLAY_NAME, user.getDisplayName() != null ? user.getDisplayName() : "");
        data.put(UserProfile.FIELD_EMAIL, user.getEmail() != null ? user.getEmail() : "");
        data.put(UserProfile.FIELD_PROFILE_PHOTO_URL, "");
        data.put(UserProfile.FIELD_UPDATED_AT, Timestamp.now());

        firestore.collection(UserProfile.COLLECTION_NAME)
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener(aVoid -> loadProfile())
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e("ProfileActivity", "Error creating profile", e);
                Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void displayProfile(UserProfile profile) {
        String name = profile.getDisplayName();
        binding.tvName.setText(name != null && !name.isEmpty() ? name : getString(R.string.profile_no_name));
        
        String email = profile.getEmail();
        binding.tvEmail.setText(email != null && !email.isEmpty() ? email : getString(R.string.profile_no_email));

        String photoUrl = profile.getProfilePhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivProfilePhoto);
        } else {
            binding.ivProfilePhoto.setImageResource(R.drawable.ic_person);
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnEditProfile.setEnabled(!show);
    }
}
