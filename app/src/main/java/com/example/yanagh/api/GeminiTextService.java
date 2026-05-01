package com.example.yanagh.api;

import android.os.Handler;
import android.os.Looper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Text-only Gemini calls (no image) for food Q&A.
 */
public class GeminiTextService {

    public interface FoodCallback {
        void onSuccess(String text);

        void onError(String message);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void ask(String languageCode, String userQuestion, FoodCallback callback) {
        String apiKey = GeminiApi.Factory.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("GEMINI_API_KEY is not set. Add it to gradle.properties.");
            return;
        }

        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            callback.onError("Question is empty.");
            return;
        }

        String prompt = buildPrompt(languageCode, userQuestion);

        List<GeminiDto.GeminiPart> parts = new ArrayList<>();
        parts.add(new GeminiDto.GeminiPart(prompt));

        GeminiDto.GeminiContent content = new GeminiDto.GeminiContent("user", parts);
        GeminiDto.GeminiRequest request = new GeminiDto.GeminiRequest(Collections.singletonList(content));

        GeminiApi api = GeminiApi.Factory.create();
        // Updated to pass model name as @Path parameter to avoid 404
        Call<GeminiDto.GeminiResponse> call = api.generateContent("gemini-1.5-flash", apiKey, request);

        call.enqueue(new Callback<GeminiDto.GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiDto.GeminiResponse> call, Response<GeminiDto.GeminiResponse> response) {
                if (!response.isSuccessful()) {
                    String errorMsg = "AI request failed (Code: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("GeminiTextService", "Error body: " + errorBody);
                            if (errorBody.contains("API_KEY_INVALID")) {
                                errorMsg = "Invalid API Key. Check gradle.properties.";
                            } else if (errorBody.contains("location is not supported")) {
                                errorMsg = "Gemini is not supported in your location. Use VPN (USA).";
                            }
                        }
                    } catch (IOException ignored) {}
                    onErrorSafe(callback, errorMsg);
                    return;
                }

                if (response.body() == null) {
                    onErrorSafe(callback, "AI returned empty response.");
                    return;
                }

                String text = extractFirstText(response.body());
                if (text == null || text.trim().isEmpty()) {
                    onErrorSafe(callback, "AI returned no text results.");
                    return;
                }

                onSuccessSafe(callback, text);
            }

            @Override
            public void onFailure(Call<GeminiDto.GeminiResponse> call, Throwable t) {
                Log.e("GeminiTextService", "Network failure", t);
                onErrorSafe(callback, "Connection error: " + t.getMessage());
            }
        });
    }

    private void onSuccessSafe(FoodCallback callback, String text) {
        mainHandler.post(() -> callback.onSuccess(text));
    }

    private void onErrorSafe(FoodCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private String buildPrompt(String languageCode, String userQuestion) {
        String lang = languageCode;
        if (lang == null || (!lang.equals("hy") && !lang.equals("ru") && !lang.equals("en"))) {
            lang = "en";
        }

        return "You are a smart food assistant. Answer the user's question concisely and practically. " +
                "If it makes sense, include a simple recipe or ingredient substitutions.\n\n" +
                "Language: " + languageDescription(lang) + "\n" +
                "User question: " + userQuestion.trim() + "\n";
    }

    private String languageDescription(String languageCode) {
        switch (languageCode) {
            case "hy":
                return "Armenian";
            case "ru":
                return "Russian";
            default:
                return "English";
        }
    }

    private String extractFirstText(GeminiDto.GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            return null;
        }

        GeminiDto.GeminiCandidate candidate = response.getCandidates().get(0);
        if (candidate == null || candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (GeminiDto.GeminiPart part : candidate.getContent().getParts()) {
            if (part == null) continue;
            String text = part.getText();
            if (text == null || text.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append(text);
        }

        String out = sb.toString();
        return out.isEmpty() ? null : out;
    }
}
