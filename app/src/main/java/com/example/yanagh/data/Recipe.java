package com.example.yanagh.data;

import android.content.Context;

import com.example.yanagh.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Recipe implements Serializable {
    private final String id;
    private final String type;
    private final int calories;
    private final int minutes;
    private final String nameHy;
    private final String nameEn;
    private final List<String> ingredientsHy;
    private final List<String> ingredientsEn;
    private final List<String> stepsHy;
    private final List<String> stepsEn;
    private final int imageResId;

    public Recipe(
            String id,
            String type,
            int calories,
            int minutes,
            String nameHy,
            String nameEn,
            List<String> ingredientsHy,
            List<String> ingredientsEn,
            List<String> stepsHy,
            List<String> stepsEn
    ) {
        this(id, type, calories, minutes, nameHy, nameEn, ingredientsHy, ingredientsEn, stepsHy, stepsEn, 0);
    }

    public Recipe(
            String id,
            String type,
            int calories,
            int minutes,
            String nameHy,
            String nameEn,
            List<String> ingredientsHy,
            List<String> ingredientsEn,
            List<String> stepsHy,
            List<String> stepsEn,
            int imageResId
    ) {
        this.id = id;
        this.type = type;
        this.calories = calories;
        this.minutes = minutes;
        this.nameHy = nameHy;
        this.nameEn = nameEn;
        this.ingredientsHy = new ArrayList<>(ingredientsHy);
        this.ingredientsEn = new ArrayList<>(ingredientsEn);
        this.stepsHy = new ArrayList<>(stepsHy);
        this.stepsEn = new ArrayList<>(stepsEn);
        this.imageResId = imageResId;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getCalories() {
        return calories;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getName(Context context) {
        return "en".equals(UserPrefs.language(context)) ? nameEn : nameHy;
    }

    public int getImageResId(Context context) {
        if (imageResId != 0) {
            return imageResId;
        }
        return R.drawable.bg_image_placeholder;
    }

    /** For search matching (both languages). */
    public boolean matchesSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return nameHy.toLowerCase(Locale.ROOT).contains(q)
                || nameEn.toLowerCase(Locale.ROOT).contains(q);
    }

    public List<String> getIngredients(Context context) {
        return "en".equals(UserPrefs.language(context))
                ? new ArrayList<>(ingredientsEn)
                : new ArrayList<>(ingredientsHy);
    }

    public List<String> getSteps(Context context) {
        return "en".equals(UserPrefs.language(context))
                ? new ArrayList<>(stepsEn)
                : new ArrayList<>(stepsHy);
    }

    public String toRowText(Context context) {
        return getName(context) + "  •  " + calories + " kcal  •  " + minutes + " min";
    }
}
