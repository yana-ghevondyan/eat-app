package com.example.yanagh.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class UserPrefs {
    private static final String PREF_NAME = "yanagh_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_AGE = "age";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_ACTIVITY_LEVEL = "activity_level";
    private static final String KEY_WEIGHT_GOAL = "weight_goal";
    private static final String KEY_GOAL_DELTA_KCAL = "goal_delta_kcal";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_SHOPPING = "shopping";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_LANGUAGE = "language";

    private static final String PREFIX_DIET_SEED = "diet_seed_";
    private static final String PREFIX_DIET_SLOT = "diet_slot_";
    private static final String PREFIX_DIET_DONE = "diet_done_";
    private static final String PREFIX_DIET_PLAN = "diet_plan_json_";

    private UserPrefs() {}

    private static String dietDayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    public static long dailyDietPlanSeed(Context context) {
        String key = PREFIX_DIET_SEED + dietDayKey();
        long fallback = System.currentTimeMillis() / (24L * 60L * 60L * 1000L);
        return prefs(context).getLong(key, fallback);
    }

    public static void setDailyDietPlanSeed(Context context, long seed) {
        prefs(context).edit().putLong(PREFIX_DIET_SEED + dietDayKey(), seed).apply();
    }

    public static String dailyDietSlotRecipeId(Context context, String slot) {
        return prefs(context).getString(PREFIX_DIET_SLOT + dietDayKey() + "_" + slot, null);
    }

    public static void setDailyDietSlotRecipeId(Context context, String slot, String recipeId) {
        prefs(context).edit().putString(PREFIX_DIET_SLOT + dietDayKey() + "_" + slot, recipeId).apply();
    }

    public static void clearDailyDietSlots(Context context) {
        String d = dietDayKey();
        prefs(context).edit()
                .remove(PREFIX_DIET_SLOT + d + "_breakfast")
                .remove(PREFIX_DIET_SLOT + d + "_lunch")
                .remove(PREFIX_DIET_SLOT + d + "_dinner")
                .remove(PREFIX_DIET_SLOT + d + "_snack")
                .remove(PREFIX_DIET_PLAN + d)
                .apply();
    }

    public static DietDayPlan getDietDayPlan(Context context) {
        String json = prefs(context).getString(PREFIX_DIET_PLAN + dietDayKey(), null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return new Gson().fromJson(json, DietDayPlan.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setDietDayPlan(Context context, DietDayPlan plan) {
        if (plan == null) {
            prefs(context).edit().remove(PREFIX_DIET_PLAN + dietDayKey()).apply();
            return;
        }
        prefs(context).edit()
                .putString(PREFIX_DIET_PLAN + dietDayKey(), new Gson().toJson(plan))
                .apply();
    }

    public static void clearDailyDietMealDone(Context context) {
        String d = dietDayKey();
        prefs(context).edit()
                .putBoolean(PREFIX_DIET_DONE + d + "_breakfast", false)
                .putBoolean(PREFIX_DIET_DONE + d + "_lunch", false)
                .putBoolean(PREFIX_DIET_DONE + d + "_dinner", false)
                .putBoolean(PREFIX_DIET_DONE + d + "_snack", false)
                .apply();
    }

    /** If today's diet plan contains this remote recipe id, replace it (e.g. after Armenian translation). */
    public static void upsertRemoteRecipeInDietPlan(Context context, RemoteRecipe updated) {
        if (updated == null) return;
        DietDayPlan p = getDietDayPlan(context);
        if (p == null) return;
        boolean changed = false;
        String[] slots = {"breakfast", "lunch", "dinner", "snack"};
        for (String slot : slots) {
            DietSlotEntry e = p.get(slot);
            if (e != null && "remote".equals(e.kind) && e.remote != null
                    && updated.getId().equals(e.remote.getId())) {
                e.remote = updated;
                changed = true;
            }
        }
        if (changed) {
            setDietDayPlan(context, p);
        }
    }

    public static boolean dailyDietMealDone(Context context, String slot) {
        return prefs(context).getBoolean(PREFIX_DIET_DONE + dietDayKey() + "_" + slot, false);
    }

    public static void setDailyDietMealDone(Context context, String slot, boolean done) {
        prefs(context).edit().putBoolean(PREFIX_DIET_DONE + dietDayKey() + "_" + slot, done).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveOnboarding(Context context, float weight, float height, int age) {
        prefs(context).edit()
                .putBoolean(KEY_ONBOARDING_DONE, true)
                .putFloat(KEY_WEIGHT, weight)
                .putFloat(KEY_HEIGHT, height)
                .putInt(KEY_AGE, age)
                .apply();
    }

    public enum WeightGoal {
        LOSE, MAINTAIN, GAIN
    }

    public enum Gender {
        MALE, FEMALE, UNSPECIFIED
    }

    public enum ActivityLevel {
        LOW, MODERATE, HIGH
    }

    public static void setWeightGoal(Context context, WeightGoal goal) {
        String v = goal == null ? "maintain" : goal.name().toLowerCase();
        prefs(context).edit().putString(KEY_WEIGHT_GOAL, v).apply();
    }

    public static WeightGoal weightGoal(Context context) {
        String s = prefs(context).getString(KEY_WEIGHT_GOAL, "maintain");
        if ("lose".equalsIgnoreCase(s)) return WeightGoal.LOSE;
        if ("gain".equalsIgnoreCase(s)) return WeightGoal.GAIN;
        return WeightGoal.MAINTAIN;
    }

    public static void setGoalDeltaKcal(Context context, int deltaKcal) {
        int v = Math.max(100, Math.min(deltaKcal, 700));
        prefs(context).edit().putInt(KEY_GOAL_DELTA_KCAL, v).apply();
    }

    public static int goalDeltaKcal(Context context) {
        return prefs(context).getInt(KEY_GOAL_DELTA_KCAL, 350);
    }

    public static void setGender(Context context, Gender gender) {
        String v = gender == null ? "unspecified" : gender.name().toLowerCase();
        prefs(context).edit().putString(KEY_GENDER, v).apply();
    }

    public static Gender gender(Context context) {
        String s = prefs(context).getString(KEY_GENDER, "unspecified");
        if ("male".equalsIgnoreCase(s)) return Gender.MALE;
        if ("female".equalsIgnoreCase(s)) return Gender.FEMALE;
        return Gender.UNSPECIFIED;
    }

    public static void setActivityLevel(Context context, ActivityLevel level) {
        String v = level == null ? "moderate" : level.name().toLowerCase();
        prefs(context).edit().putString(KEY_ACTIVITY_LEVEL, v).apply();
    }

    public static ActivityLevel activityLevel(Context context) {
        String s = prefs(context).getString(KEY_ACTIVITY_LEVEL, "moderate");
        if ("low".equalsIgnoreCase(s)) return ActivityLevel.LOW;
        if ("high".equalsIgnoreCase(s)) return ActivityLevel.HIGH;
        return ActivityLevel.MODERATE;
    }

    public static boolean isOnboardingDone(Context context) {
        return prefs(context).getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public static int dailyCalorieTarget(Context context) {
        // Fallback behavior remains similar to old logic, but we try to be a bit more accurate when data is present.
        int age = prefs(context).getInt(KEY_AGE, 25);
        float weightKg = prefs(context).getFloat(KEY_WEIGHT, 70f);
        float heightCm = prefs(context).getFloat(KEY_HEIGHT, 170f);

        // Mifflin-St Jeor BMR.
        double bmr;
        switch (gender(context)) {
            case MALE:
                bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
                break;
            case FEMALE:
                bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
                break;
            default:
                // Approx midpoint between male/female constants.
                bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 78;
                break;
        }

        double activity = 1.55; // moderate default
        switch (activityLevel(context)) {
            case LOW:
                activity = 1.375;
                break;
            case HIGH:
                activity = 1.725;
                break;
            default:
                activity = 1.55;
                break;
        }

        int base = (int) Math.round(bmr * activity);
        if (base <= 0) {
            base = (int) (weightKg * 30);
        }

        int delta = goalDeltaKcal(context);
        int adjusted = base;
        switch (weightGoal(context)) {
            case LOSE:
                adjusted = base - delta;
                break;
            case GAIN:
                adjusted = base + delta;
                break;
            default:
                adjusted = base;
                break;
        }

        if (adjusted < 1300) adjusted = 1300;
        return adjusted;
    }

    public static Set<String> getFavoriteIds(Context context) {
        return new LinkedHashSet<>(prefs(context).getStringSet(KEY_FAVORITES, new LinkedHashSet<>()));
    }

    public static void toggleFavorite(Context context, String recipeId) {
        Set<String> set = getFavoriteIds(context);
        if (set.contains(recipeId)) set.remove(recipeId);
        else set.add(recipeId);
        prefs(context).edit().putStringSet(KEY_FAVORITES, set).apply();
    }

    public static boolean isFavorite(Context context, String recipeId) {
        return getFavoriteIds(context).contains(recipeId);
    }

    public static List<String> getShoppingItems(Context context) {
        String raw = prefs(context).getString(KEY_SHOPPING, "");
        if (raw == null || raw.trim().isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split("\\|")));
    }

    public static void addShoppingItems(Context context, List<String> items) {
        Set<String> all = new LinkedHashSet<>(getShoppingItems(context));
        all.addAll(items);
        prefs(context).edit().putString(KEY_SHOPPING, String.join("|", all)).apply();
    }

    public static void saveShoppingItems(Context context, List<String> items) {
        prefs(context).edit().putString(KEY_SHOPPING, String.join("|", items)).apply();
    }

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public static boolean notificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    /** App UI language: {@code hy} (Armenian) or {@code en} (English). */
    public static String language(Context context) {
        String s = prefs(context).getString(KEY_LANGUAGE, "hy");
        if ("en".equalsIgnoreCase(s)) {
            return "en";
        }
        return "hy";
    }

    public static void setLanguage(Context context, String lang) {
        String v = "en".equalsIgnoreCase(lang) ? "en" : "hy";
        prefs(context).edit().putString(KEY_LANGUAGE, v).apply();
    }
}
