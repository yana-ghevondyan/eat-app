package com.example.yanagh.api;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Calls Gemini with an image (vision) to generate: identified dish + recipe + cooking tips.
 */
public class GeminiFoodService {

    public interface FoodAnalyzeCallback {
        void onSuccess(String text);

        void onError(String message);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void analyzeMeal(Bitmap bitmap, String languageCode, String userQuestion, FoodAnalyzeCallback callback) {
        String apiKey = GeminiApi.Factory.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("GEMINI_API_KEY is not set. Add it to gradle.properties.");
            return;
        }

        if (bitmap == null) {
            callback.onError("Image is missing.");
            return;
        }

        String base64Image = bitmapToBase64Jpeg(bitmap);
        if (base64Image == null) {
            callback.onError("Failed to encode image.");
            return;
        }

        String prompt = buildPrompt(languageCode, userQuestion);

        List<GeminiDto.GeminiPart> parts = new ArrayList<>();
        parts.add(new GeminiDto.GeminiPart(prompt));
        parts.add(new GeminiDto.GeminiPart(new GeminiDto.GeminiInlineData("image/jpeg", base64Image)));

        GeminiDto.GeminiContent content = new GeminiDto.GeminiContent("user", parts);
        GeminiDto.GeminiRequest request = new GeminiDto.GeminiRequest(Collections.singletonList(content));

        GeminiApi api = GeminiApi.Factory.create();
        // Fixed: Added "gemini-1.5-flash" as the first argument to match updated GeminiApi interface
        Call<GeminiDto.GeminiResponse> call = api.generateContent("gemini-1.5-flash", apiKey, request);

        call.enqueue(new Callback<GeminiDto.GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiDto.GeminiResponse> call, Response<GeminiDto.GeminiResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    onErrorSafe(callback, "AI request failed. Check network and try again.");
                    return;
                }
                String text = extractFirstText(response.body());
                if (text == null || text.trim().isEmpty()) {
                    onErrorSafe(callback, "AI returned empty response.");
                    return;
                }
                onSuccessSafe(callback, text);
            }

            @Override
            public void onFailure(Call<GeminiDto.GeminiResponse> call, Throwable t) {
                onErrorSafe(callback, t != null ? t.getMessage() : "AI request failed.");
            }
        });
    }

    private void onSuccessSafe(FoodAnalyzeCallback callback, String text) {
        mainHandler.post(() -> callback.onSuccess(text));
    }

    private void onErrorSafe(FoodAnalyzeCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private String bitmapToBase64Jpeg(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
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

    private String buildPrompt(String languageCode, String userQuestion) {
        String lang = languageCode;
        if (lang == null || (!lang.equals("hy") && !lang.equals("ru") && !lang.equals("en"))) {
            lang = "en";
        }

        // Keep prompt consistent and simple. We rely on model to format the output.
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return "You are a smart food assistant. Analyze the provided image and identify what dish it is (top-1). " +
                    "Then generate a complete recipe with ingredients and step-by-step instructions. " +
                    "Finally provide practical cooking tips and ingredient substitutions.\n\n" +
                    "Return the answer in " + languageDescription(lang) + ". Use this structure exactly:\n" +
                    "Dish: <dish name>\n" +
                    "Variants (if uncertain): <1-3 variants>\n" +
                    "Recipe Ingredients:\n- ...\n" +
                    "Recipe Steps:\n1. ...\n2. ...\n3. ...\n" +
                    "Cooking Tips:\n- ...\n";
        }

        return "You are a smart food assistant. Use the provided image to understand the dish. " +
                "Answer the user's question and, if relevant, include recipe corrections/extra steps and cooking tips. " +
                "Write the answer in " + languageDescription(lang) + ".\n\n" +
                "Dish identification (short):\n- <dish name>\n\n" +
                "User question: " + userQuestion.trim() + "\n\n" +
                "Answer format:\n- Recipe adjustments / relevant steps:\n- Cooking tips / substitutions:\n";
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
}
