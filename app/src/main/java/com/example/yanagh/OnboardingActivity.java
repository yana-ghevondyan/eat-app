package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivityOnboardingBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends BaseActivity {
    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnContinue.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String weightText = String.valueOf(binding.etWeight.getText()).trim();
        String heightText = String.valueOf(binding.etHeight.getText()).trim();
        String ageText = String.valueOf(binding.etAge.getText()).trim();
        if (weightText.isEmpty() || heightText.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(this, R.string.onboarding_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }
        float weight = Float.parseFloat(weightText);
        float height = Float.parseFloat(heightText);
        int age = Integer.parseInt(ageText);
        UserPrefs.saveOnboarding(this, weight, height, age);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> data = new HashMap<>();
            data.put("weight", weight);
            data.put("height", height);
            data.put("age", age);
            data.put("updatedAt", Timestamp.now());
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(data, SetOptions.merge());
        }

        Intent i = new Intent(this, FoodMainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
