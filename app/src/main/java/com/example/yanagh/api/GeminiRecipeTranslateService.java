package com.example.yanagh.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Translates online recipe text to Eastern Armenian via Gemini when the UI language is hy.
 */
public class GeminiRecipeTranslateService {

    public interface TranslateCallback {
        void onSuccess(String titleHy, List<String> ingredientsHy, List<String> stepsHy);

        void onError(String message);
    }

    private static final String TAG = "RecipeTranslate";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public void translateRecipeToArmenian(
            String titleEn,
            List<String> ingredientsEn,
            List<String> stepsEn,
            TranslateCallback callback
    ) {
        String apiKey = GeminiApi.Factory.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            onErrorSafe(callback, "no_api_key");
            return;
        }
        if (titleEn == null || titleEn.trim().isEmpty()) {
            onErrorSafe(callback, "empty");
            return;
        }

        List<String> ingIn = ingredientsEn != null ? ingredientsEn : Collections.emptyList();
        List<String> stIn = stepsEn != null ? stepsEn : Collections.emptyList();

        String prompt = "You translate cooking recipes into Eastern Armenian (Հայերեն).\n"
                + "Reply with ONLY a single JSON object, no markdown fences, no commentary.\n"
                + "Schema: {\"title\":\"...\",\"ingredients\":[\"...\"],\"steps\":[\"...\"]}\n"
                + "CRITICAL: \"ingredients\" must have exactly " + ingIn.size() + " strings and \"steps\" exactly "
                + stIn.size() + " strings — same order as input.\n"
                + "Each \"steps\" item must be the FULL Armenian text for that cooking step (complete sentences), "
                + "not a summary, not \"see above\", not English.\n"
                + "Keep numbers, °C, minutes, and measures readable; translate technique words to Armenian.\n\n"
                + "Title: " + titleEn.trim() + "\n"
                + "Ingredients JSON: " + gson.toJson(ingIn) + "\n"
                + "Steps JSON: " + gson.toJson(stIn) + "\n";

        List<GeminiDto.GeminiPart> parts = new ArrayList<>();
        parts.add(new GeminiDto.GeminiPart(prompt));
        GeminiDto.GeminiContent content = new GeminiDto.GeminiContent("user", parts);
        GeminiDto.GeminiRequest request = new GeminiDto.GeminiRequest(Collections.singletonList(content));

        GeminiApi api = GeminiApi.Factory.create();
        Call<GeminiDto.GeminiResponse> call = api.generateContent("gemini-1.5-flash", apiKey, request);
        call.enqueue(new Callback<GeminiDto.GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiDto.GeminiResponse> call, Response<GeminiDto.GeminiResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String err = "HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }
                    Log.e(TAG, err);
                    onErrorSafe(callback, "translate_failed");
                    return;
                }
                String raw = extractFirstText(response.body());
                if (raw == null || raw.trim().isEmpty()) {
                    onErrorSafe(callback, "empty_response");
                    return;
                }
                try {
                    Parsed p = parseTranslationJson(stripCodeFence(raw.trim()));
                    List<String> ingAligned = alignToExpected(ingIn, p.ingredients);
                    List<String> stAligned = alignToExpected(stIn, p.steps);
                    onSuccessSafe(callback, p.title, ingAligned, stAligned);
                } catch (Exception e) {
                    Log.e(TAG, "parse", e);
                    onErrorSafe(callback, "parse_error");
                }
            }

            @Override
            public void onFailure(Call<GeminiDto.GeminiResponse> call, Throwable t) {
                Log.e(TAG, "network", t);
                onErrorSafe(callback, t != null ? t.getMessage() : "network");
            }
        });
    }

    /** One output string per expected row; missing slots keep English so UI/TTS never drops a step. */
    private static List<String> alignToExpected(List<String> expected, List<String> translated) {
        List<String> got = translated != null ? translated : Collections.emptyList();
        List<String> out = new ArrayList<>();
        for (int i = 0; i < expected.size(); i++) {
            if (i < got.size()) {
                String t = got.get(i);
                if (t != null && !t.trim().isEmpty()) {
                    out.add(t.trim());
                    continue;
                }
            }
            out.add(expected.get(i));
        }
        return out;
    }

    private static String stripCodeFence(String s) {
        String t = s;
        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            if (nl > 0) {
                t = t.substring(nl + 1);
            }
            int end = t.indexOf("```");
            if (end >= 0) {
                t = t.substring(0, end);
            }
        }
        return t.trim();
    }

    private Parsed parseTranslationJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("not object");
        }
        JsonObject o = root.getAsJsonObject();
        String title = o.has("title") && !o.get("title").isJsonNull()
                ? o.get("title").getAsString().trim() : "";

        List<String> ing = new ArrayList<>();
        if (o.has("ingredients") && o.get("ingredients").isJsonArray()) {
            JsonArray arr = o.getAsJsonArray("ingredients");
            for (JsonElement e : arr) {
                if (e != null && e.isJsonPrimitive()) {
                    ing.add(e.getAsString().trim());
                }
            }
        }

        List<String> st = new ArrayList<>();
        if (o.has("steps") && o.get("steps").isJsonArray()) {
            JsonArray arr = o.getAsJsonArray("steps");
            for (JsonElement e : arr) {
                if (e != null && e.isJsonPrimitive()) {
                    st.add(e.getAsString().trim());
                }
            }
        }

        if (title.isEmpty()) {
            throw new IllegalArgumentException("no title");
        }
        return new Parsed(title, ing, st);
    }

    private static final class Parsed {
        final String title;
        final List<String> ingredients;
        final List<String> steps;

        Parsed(String title, List<String> ingredients, List<String> steps) {
            this.title = title;
            this.ingredients = ingredients;
            this.steps = steps;
        }
    }

    private void onSuccessSafe(TranslateCallback callback, String t, List<String> i, List<String> s) {
        mainHandler.post(() -> callback.onSuccess(t, i, s));
    }

    private void onErrorSafe(TranslateCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
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
