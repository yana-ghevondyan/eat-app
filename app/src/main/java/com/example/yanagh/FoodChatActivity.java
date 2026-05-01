package com.example.yanagh;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yanagh.api.GeminiTextService;
import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.chat.ChatAdapter;
import com.example.yanagh.chat.ChatItem;
import com.example.yanagh.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.List;

public class FoodChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private final List<ChatItem> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupTopNavigation();
        setupRecyclerView();
        setupSend();

        if (messages.isEmpty()) {
            messages.add(new ChatItem("welcome_" + System.currentTimeMillis(), "assistant",
                    getString(R.string.chat_welcome)));
            chatAdapter.submitList(new ArrayList<>(messages));
            scrollToBottom();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setTitle(R.string.food_ai_assistant_title);
        com.google.android.material.tabs.TabLayout.Tab aiTab = binding.tabLayout.getTabAt(1);
        if (aiTab != null) {
            aiTab.select();
        }
    }

    private void setupTopNavigation() {
        // Tab 0: Food (navigate to analyzer)
        // Tab 1: AI (current)
        // Tab 2: Profile
        if (binding.tabLayout != null) {
            binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    if (tab == null) return;
                    switch (tab.getPosition()) {
                        case 0:
                            startActivity(new android.content.Intent(FoodChatActivity.this, AnalyzeFoodActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            break;
                        case 1:
                            // current screen
                            break;
                        case 2:
                            startActivity(new android.content.Intent(FoodChatActivity.this, ProfileActivity.class));
                            overridePendingTransition(0, 0);
                            finish();
                            break;
                    }
                }

                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerChat.setLayoutManager(layoutManager);
        binding.recyclerChat.setAdapter(chatAdapter);
    }

    private void setupSend() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = binding.etInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_chat_empty_message), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.etInput.setText("");

        ChatItem userItem = new ChatItem("user_" + System.currentTimeMillis(), "user", text);
        messages.add(userItem);
        chatAdapter.submitList(new ArrayList<>(messages));
        scrollToBottom();

        setControlsEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        String lang = detectLanguage(text);
        new GeminiTextService().ask(lang, text, new GeminiTextService.FoodCallback() {
            @Override
            public void onSuccess(String aiText) {
                binding.progressBar.setVisibility(View.GONE);
                setControlsEnabled(true);
                messages.add(new ChatItem("assistant_" + System.currentTimeMillis(), "assistant", aiText));
                chatAdapter.submitList(new ArrayList<>(messages));
                scrollToBottom();
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                setControlsEnabled(true);
                Toast.makeText(FoodChatActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
}

