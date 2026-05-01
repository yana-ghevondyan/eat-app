package com.example.yanagh.data;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Online recipe model (e.g. from TheMealDB).
 * Optional Armenian fields are filled by Gemini translation when UI language is hy.
 */
public class RemoteRecipe implements Serializable {
    private final String id;
    private final String title;
    private final String imageUrl;
    private final List<String> ingredients;
    private final List<String> steps;
    private final String instructionsRaw;
    private final String titleHy;
    private final List<String> ingredientsHy;
    private final List<String> stepsHy;

    public RemoteRecipe(
            String id,
            String title,
            String imageUrl,
            List<String> ingredients,
            List<String> steps,
            String instructionsRaw
    ) {
        this(id, title, imageUrl, ingredients, steps, instructionsRaw, null, null, null);
    }

    public RemoteRecipe(
            String id,
            String title,
            String imageUrl,
            List<String> ingredients,
            List<String> steps,
            String instructionsRaw,
            String titleHy,
            List<String> ingredientsHy,
            List<String> stepsHy
    ) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
        this.instructionsRaw = instructionsRaw;
        this.titleHy = titleHy;
        this.ingredientsHy = ingredientsHy != null ? new ArrayList<>(ingredientsHy) : null;
        this.stepsHy = stepsHy != null ? new ArrayList<>(stepsHy) : null;
    }

    public RemoteRecipe withArmenian(String hyTitle, List<String> hyIngredients, List<String> hySteps) {
        return new RemoteRecipe(
                id, title, imageUrl, ingredients, steps, instructionsRaw,
                hyTitle, hyIngredients, hySteps
        );
    }

    public boolean hasArmenian() {
        return titleHy != null && !titleHy.trim().isEmpty();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTitle(Context context) {
        if ("hy".equals(UserPrefs.language(context)) && titleHy != null && !titleHy.trim().isEmpty()) {
            return titleHy.trim();
        }
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public List<String> getIngredients(Context context) {
        if ("hy".equals(UserPrefs.language(context)) && ingredientsHy != null && !ingredientsHy.isEmpty()) {
            return new ArrayList<>(ingredientsHy);
        }
        return getIngredients();
    }

    public List<String> getSteps() {
        return new ArrayList<>(steps);
    }

    public List<String> getSteps(Context context) {
        if ("hy".equals(UserPrefs.language(context)) && stepsHy != null && !stepsHy.isEmpty()) {
            return new ArrayList<>(stepsHy);
        }
        return getSteps();
    }

    public String getInstructionsRaw() {
        return instructionsRaw;
    }
}
