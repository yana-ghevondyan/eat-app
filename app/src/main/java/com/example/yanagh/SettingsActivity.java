package com.example.yanagh;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivitySettingsBinding;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends BaseActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        BottomNavHelper.wire(this, binding.bottomNav, R.id.nav_settings);
        setupControls();
    }

    private void setupControls() {
        binding.swDarkMode.setChecked(UserPrefs.isDarkMode(this));
        binding.swNotifications.setChecked(UserPrefs.notificationsEnabled(this));
        refreshLanguageButtons();

        binding.swDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserPrefs.setDarkMode(this, isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        binding.swNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> UserPrefs.setNotificationsEnabled(this, isChecked));

        binding.btnLangHy.setOnClickListener(v -> applyLanguage("hy"));
        binding.btnLangEn.setOnClickListener(v -> applyLanguage("en"));
    }

    private void refreshLanguageButtons() {
        String lang = UserPrefs.language(this);
        styleLangButton(binding.btnLangHy, "hy".equals(lang));
        styleLangButton(binding.btnLangEn, "en".equals(lang));
    }

    private void styleLangButton(MaterialButton btn, boolean selected) {
        int strokePx = (int) (2 * getResources().getDisplayMetrics().density);
        if (selected) {
            btn.setStrokeWidth(strokePx);
            btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        } else {
            btn.setStrokeWidth(0);
        }
    }

    private void applyLanguage(String code) {
        UserPrefs.setLanguage(this, code);
        Toast.makeText(this, R.string.settings_language_applied, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, FoodMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
