package com.example.yanagh;

import android.content.Context;

import com.example.yanagh.data.UserPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Formats recipe text for clearer Text-to-Speech in Armenian and English,
 * and splits long content so engines do not truncate.
 */
public final class RecipeTtsHelper {
    /** Larger chunks = fewer TTS hand-offs (smoother listening). Still below typical engine limits. */
    private static final int MAX_CHUNK_CHARS = 5200;
    private static final Pattern MULTISPACE = Pattern.compile("\\s+");

    private RecipeTtsHelper() {}

    public static String buildSpeakAll(
            Context context,
            String title,
            List<String> ingredients,
            List<String> steps
    ) {
        boolean hy = !"en".equals(UserPrefs.language(context));
        StringBuilder sb = new StringBuilder();
        sb.append(clean(title));
        if (ingredients == null) ingredients = Collections.emptyList();
        if (steps == null) steps = Collections.emptyList();

        if (hy) {
            sb.append("։ ");
            sb.append(context.getString(R.string.tts_intro_ingredients)).append(' ');
            for (int i = 0; i < ingredients.size(); i++) {
                if (i > 0) sb.append(' ');
                sb.append(clean(ingredients.get(i))).append('։');
            }
            sb.append(' ');
            sb.append(context.getString(R.string.tts_intro_steps)).append(' ');
            for (int i = 0; i < steps.size(); i++) {
                sb.append(i + 1).append("՝ ").append(clean(steps.get(i))).append('։');
                if (i < steps.size() - 1) sb.append(' ');
            }
        } else {
            sb.append(". ");
            sb.append(context.getString(R.string.tts_intro_ingredients)).append(' ');
            for (int i = 0; i < ingredients.size(); i++) {
                if (i > 0) sb.append(' ');
                sb.append(clean(ingredients.get(i))).append('.');
            }
            sb.append(' ');
            sb.append(context.getString(R.string.tts_intro_steps)).append(' ');
            for (int i = 0; i < steps.size(); i++) {
                sb.append(context.getString(R.string.tts_step_label, i + 1))
                        .append(' ')
                        .append(clean(steps.get(i)))
                        .append('.');
                if (i < steps.size() - 1) sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    public static String buildSpeakSteps(Context context, List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            return "";
        }
        boolean hy = !"en".equals(UserPrefs.language(context));
        StringBuilder sb = new StringBuilder();
        if (hy) {
            sb.append(context.getString(R.string.tts_intro_steps)).append(' ');
            for (int i = 0; i < steps.size(); i++) {
                sb.append(i + 1).append("՝ ").append(clean(steps.get(i))).append('։');
                if (i < steps.size() - 1) sb.append(' ');
            }
        } else {
            sb.append(context.getString(R.string.tts_intro_steps)).append(' ');
            for (int i = 0; i < steps.size(); i++) {
                sb.append(context.getString(R.string.tts_step_label, i + 1))
                        .append(' ')
                        .append(clean(steps.get(i)))
                        .append('.');
                if (i < steps.size() - 1) sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    /** Normalizes punctuation and symbols so TTS reads amounts and lists more naturally. */
    public static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "";
        }
        s = s.replace('\u00A0', ' ');
        s = s.replace("≈", " մոտ ").replace("\u2248", " մոտ ");
        s = s.replace("–", ", ").replace("—", ", ");
        s = s.replace("·", ", ").replace("•", "").replace("*", "");
        s = s.replace("(", ", ").replace(")", ", ");
        s = s.replace("/", " / ");
        s = MULTISPACE.matcher(s).replaceAll(" ");
        return s.trim();
    }

    /**
     * Splits long text at sentence or word boundaries for {@code speak} queueing.
     */
    public static List<String> chunks(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        String t = text.trim();
        if (t.length() <= MAX_CHUNK_CHARS) {
            return Collections.singletonList(t);
        }
        List<String> out = new ArrayList<>();
        int start = 0;
        while (start < t.length()) {
            int end = Math.min(t.length(), start + MAX_CHUNK_CHARS);
            if (end < t.length()) {
                int br = lastGoodBreak(t, start, end);
                if (br > start + MAX_CHUNK_CHARS / 3) {
                    end = br;
                }
            }
            String part = t.substring(start, end).trim();
            if (!part.isEmpty()) {
                out.add(part);
            }
            start = end;
            while (start < t.length() && Character.isWhitespace(t.charAt(start))) {
                start++;
            }
        }
        return out.isEmpty() ? Collections.singletonList(t) : out;
    }

    private static int lastGoodBreak(String text, int start, int end) {
        for (int i = end - 1; i > start + 80; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '։' || c == '!' || c == '?') {
                if (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                    return i + 1;
                }
            }
        }
        for (int i = end - 1; i > start + 40; i--) {
            if (text.charAt(i) == ' ') {
                return i;
            }
        }
        return end;
    }
}
