package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;

import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivityFoodMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class FoodMainActivity extends BaseActivity {

    private ActivityFoodMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFoodMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }
        if (!UserPrefs.isOnboardingDone(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setupHomeActions();
        BottomNavHelper.wire(this, binding.bottomNav, R.id.nav_home);
    }

    private void setupHomeActions() {
        String name = auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : "";
        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.chef_default);
        }
        binding.tvWelcome.setText(getString(R.string.welcome_named, name));

        binding.searchInput.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.btnAiIdeas.setOnClickListener(v -> startActivity(new Intent(this, FoodChatActivity.class)));
        binding.btnQuickRecipes.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.btnIngredientRecipe.setOnClickListener(v -> startActivity(new Intent(this, FoodChatActivity.class)));
        binding.btnImageRecognition.setOnClickListener(v -> startActivity(new Intent(this, AnalyzeFoodActivity.class)));
        binding.btnShoppingList.setOnClickListener(v -> startActivity(new Intent(this, ShoppingListActivity.class)));
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
