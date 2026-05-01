package com.example.yanagh;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yanagh.api.GeminiFoodService;
import com.example.yanagh.chat.ChatAdapter;
import com.example.yanagh.chat.ChatItem;
import com.example.yanagh.databinding.ActivityFoodAnalysisChatBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.yanagh.data.UserPrefs;

public class FoodAnalysisChatActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_PATH = "image_path";

    private ActivityFoodAnalysisChatBinding binding;
    private ChatAdapter chatAdapter;
    private final List<ChatItem> messages = new ArrayList<>();
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoodAnalysisChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupSendMessage();

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (imagePath == null || imagePath.trim().isEmpty()) {
            Toast.makeText(this, R.string.error_image_path_missing, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        selectedImageBitmap = decodeBitmapOrNull(imagePath);
        if (selectedImageBitmap == null) {
            Toast.makeText(this, R.string.error_load_image_failed, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initial Gemini analysis (MVP A).
        String lang = getDeviceLanguageCode();
        requestInitialAnalysis(lang);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setTitle(R.string.food_assistant_title);
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.etInput.setHint(R.string.food_analysis_hint);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerChat.setLayoutManager(layoutManager);
        binding.recyclerChat.setAdapter(chatAdapter);
    }

    private void setupSendMessage() {
        binding.btnSend.setOnClickListener(v -> sendUserMessage());
        binding.etInput.setOnEditorActionListener((v, actionId, event) -> {
            // Reuse send button behavior for IME send
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendUserMessage();
                return true;
            }
            return false;
        });
    }

    private void requestInitialAnalysis(String languageCode) {
        setControlsEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        new GeminiFoodService().analyzeMeal(selectedImageBitmap, languageCode, null, new GeminiFoodService.FoodAnalyzeCallback() {
            @Override
            public void onSuccess(String text) {
                addAssistantMessage(text);
                setControlsEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FoodAnalysisChatActivity.this, message, Toast.LENGTH_LONG).show();
                setControlsEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sendUserMessage() {
        String text = binding.etInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.error_enter_question, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.etInput.setText("");

        ChatItem userItem = new ChatItem("user_" + System.currentTimeMillis(), "user", text);
        messages.add(userItem);
        chatAdapter.submitList(new ArrayList<>(messages));
        scrollToBottom();

        String lang = detectLanguage(text);
        setControlsEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        new GeminiFoodService().analyzeMeal(selectedImageBitmap, lang, text, new GeminiFoodService.FoodAnalyzeCallback() {
            @Override
            public void onSuccess(String aiText) {
                addAssistantMessage(aiText);
                setControlsEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FoodAnalysisChatActivity.this, message, Toast.LENGTH_LONG).show();
                setControlsEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void addAssistantMessage(String content) {
        ChatItem analysisItem = new ChatItem("assistant_" + System.currentTimeMillis(), "assistant", content);
        messages.add(analysisItem);
        chatAdapter.submitList(new ArrayList<>(messages));
        scrollToBottom();
    }

    private void setControlsEnabled(boolean enabled) {
        binding.btnSend.setEnabled(enabled);
        binding.etInput.setEnabled(enabled);
    }

    private void scrollToBottom() {
        binding.recyclerChat.post(() -> {
            int n = chatAdapter.getItemCount();
            if (n > 0) binding.recyclerChat.smoothScrollToPosition(n - 1);
        });
    }

    private Bitmap decodeBitmapOrNull(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) return null;
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }

    private String getDeviceLanguageCode() {
        String appLang = UserPrefs.language(this);
        if ("hy".equals(appLang)) {
            return "hy";
        }
        return "en";
    }

    private String detectLanguage(String message) {
        if (message == null) {
            return UserPrefs.language(this);
        }
        if (message.matches(".*[\\u0530-\\u058F\\uFB10-\\uFB4F].*")) {
            return "hy";
        }
        if (message.matches(".*[\\u0400-\\u04FF].*")) {
            return "ru";
        }
        return UserPrefs.language(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File f = new File(imagePath);
                // Clean up temp file from cache.
                if (f.exists()) f.delete();
            } catch (Exception ignored) {
            }
        }
    }
}

