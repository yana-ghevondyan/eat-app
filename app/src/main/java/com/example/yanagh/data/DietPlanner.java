package com.example.yanagh.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DietPlanner {
    private DietPlanner() {}

    public static class MealPlan {
        public final Recipe breakfast;
        public final Recipe lunch;
        public final Recipe dinner;
        public final Recipe snack;
        public final int totalCalories;

        public MealPlan(Recipe breakfast, Recipe lunch, Recipe dinner, Recipe snack) {
            this.breakfast = breakfast;
            this.lunch = lunch;
            this.dinner = dinner;
            this.snack = snack;
            int sum = 0;
            if (breakfast != null) sum += breakfast.getCalories();
            if (lunch != null) sum += lunch.getCalories();
            if (dinner != null) sum += dinner.getCalories();
            if (snack != null) sum += snack.getCalories();
            this.totalCalories = sum;
        }
    }

    /**
     * One plan per {@code seed}: breakfast, lunch, dinner, snack sized to {@code targetCalories}.
     */
    public static MealPlan planForDay(List<Recipe> allLocal, int targetCalories, long seed) {
        if (allLocal == null || allLocal.isEmpty()) {
            return new MealPlan(null, null, null, null);
        }

        List<Recipe> breakfasts = filterType(allLocal, "Breakfast");
        List<Recipe> lunches = filterType(allLocal, "Lunch");
        List<Recipe> dinners = filterType(allLocal, "Dinner");
        List<Recipe> snacks = filterType(allLocal, "Snack");
        if (snacks.isEmpty()) {
            snacks = filterLightSnacks(lunches);
        }

        int safeTarget = Math.max(targetCalories, 1200);
        int sTarget = (int) Math.round(safeTarget * 0.12);
        int bTarget = (int) Math.round(safeTarget * 0.28);
        int lTarget = (int) Math.round(safeTarget * 0.35);
        int dTarget = Math.max(250, safeTarget - bTarget - lTarget - sTarget);

        Random rnd = new Random(seed);

        Recipe b = pickClosest(breakfasts, bTarget, rnd);
        Recipe l = pickClosest(lunches, lTarget, rnd);
        Recipe d = pickClosest(dinners, dTarget, rnd);
        Recipe s = pickClosest(snacks, sTarget, rnd);

        return new MealPlan(b, l, d, s);
    }

    public static MealPlan planForToday(List<Recipe> allLocal, int targetCalories) {
        long daySeed = System.currentTimeMillis() / (24L * 60L * 60L * 1000L);
        return planForDay(allLocal, targetCalories, daySeed);
    }

    private static List<Recipe> filterLightSnacks(List<Recipe> lunches) {
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : lunches) {
            if (r != null && r.getCalories() <= 320) {
                out.add(r);
            }
        }
        return out;
    }

    private static List<Recipe> filterType(List<Recipe> all, String type) {
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : all) {
            if (r == null) continue;
            if (type.equalsIgnoreCase(r.getType())) out.add(r);
        }
        return out;
    }

    private static Recipe pickClosest(List<Recipe> options, int target, Random rnd) {
        if (options == null || options.isEmpty()) return null;

        List<Recipe> copy = new ArrayList<>(options);
        Collections.shuffle(copy, rnd);

        Recipe best = copy.get(0);
        int bestDiff = Math.abs(best.getCalories() - target);
        for (Recipe r : copy) {
            int diff = Math.abs(r.getCalories() - target);
            if (diff < bestDiff) {
                best = r;
                bestDiff = diff;
            }
        }
        return best;
    }
}
