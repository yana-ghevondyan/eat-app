package com.example.yanagh.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeRepository {
    public interface RemoteRecipesCallback {
        void onSuccess(List<RemoteRecipe> recipes);

        void onError(String message);
    }

    public interface SingleRecipeCallback {
        void onSuccess(RemoteRecipe recipe);

        void onError(String message);
    }

    /** Meals in order breakfast, lunch, dinner, snack + TheMealDB category used for each slot. */
    public interface DietPlanCallback {
        void onSuccess(List<RemoteRecipe> meals, String[] categoriesUsed);

        void onError(String message);
    }

    private static final String TAG = "RecipeRepository";
    private static final String BASE_URL = "https://www.themealdb.com/";

    public static final String[] LUNCH_CATEGORIES = {
            "Chicken", "Beef", "Vegetarian", "Pasta", "Miscellaneous"
    };
    public static final String[] DINNER_CATEGORIES = {
            "Seafood", "Lamb", "Pork", "Side", "Vegan", "Goat"
    };

    private final TheMealDbApi api;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RecipeRepository() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(TheMealDbApi.class);
    }

    public List<Recipe> localAll() {
        return RecipeStore.all();
    }

    public void searchRemoteByName(String query, RemoteRecipesCallback callback) {
        String q = query == null ? "" : query.trim();
        Call<TheMealDbDto.SearchResponse> call = api.searchByName(q);
        call.enqueue(new Callback<TheMealDbDto.SearchResponse>() {
            @Override
            public void onResponse(Call<TheMealDbDto.SearchResponse> call, Response<TheMealDbDto.SearchResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Failed to load recipes (Code: " + response.code() + ")");
                    return;
                }
                List<TheMealDbDto.Meal> meals = response.body().meals;
                if (meals == null) {
                    callback.onSuccess(Collections.emptyList());
                    return;
                }
                List<RemoteRecipe> out = new ArrayList<>();
                for (TheMealDbDto.Meal m : meals) {
                    RemoteRecipe r = mapMeal(m);
                    if (r != null) out.add(r);
                }
                callback.onSuccess(out);
            }

            @Override
            public void onFailure(Call<TheMealDbDto.SearchResponse> call, Throwable t) {
                Log.e(TAG, "Remote search failed", t);
                callback.onError("Network error: " + (t != null ? t.getMessage() : "unknown"));
            }
        });
    }

    private static final int ONLINE_SEARCH_TARGET_MIN = 40;

    /**
     * Name search on TheMealDB, then fills up with unique random meals so the list stays large and varied.
     */
    public void searchOnlineMerged(String query, RemoteRecipesCallback callback) {
        searchRemoteByName(query, new RemoteRecipesCallback() {
            @Override
            public void onSuccess(List<RemoteRecipe> recipes) {
                List<RemoteRecipe> base = recipes != null ? new ArrayList<>(recipes) : new ArrayList<>();
                LinkedHashSet<String> seen = new LinkedHashSet<>();
                for (RemoteRecipe r : base) {
                    seen.add(r.getId());
                }
                int need = Math.max(0, ONLINE_SEARCH_TARGET_MIN - base.size());
                if (need == 0) {
                    runOnMain(() -> callback.onSuccess(base));
                    return;
                }
                appendRandomRecipes(need, seen, base, () -> runOnMain(() -> callback.onSuccess(base)));
            }

            @Override
            public void onError(String message) {
                List<RemoteRecipe> base = new ArrayList<>();
                LinkedHashSet<String> seen = new LinkedHashSet<>();
                appendRandomRecipes(ONLINE_SEARCH_TARGET_MIN, seen, base, () -> runOnMain(() -> callback.onSuccess(base)));
            }
        });
    }

    /**
     * Appends up to {@code target} unique random meals into {@code appendTo} (also updates {@code seenIds}).
     * {@code onComplete} runs on the main thread.
     */
    public void appendRandomRecipes(int target, Set<String> seenIds, List<RemoteRecipe> appendTo, Runnable onComplete) {
        if (target <= 0) {
            runOnMain(onComplete);
            return;
        }
        final int[] remaining = {target};
        final int[] attempts = {0};
        final int maxAttempts = Math.max(80, target * 14);

        final Runnable[] step = new Runnable[1];
        step[0] = () -> {
            if (remaining[0] <= 0) {
                runOnMain(onComplete);
                return;
            }
            if (attempts[0] >= maxAttempts) {
                runOnMain(onComplete);
                return;
            }
            attempts[0]++;
            fetchRandomFullMeal(new SingleRecipeCallback() {
                @Override
                public void onSuccess(RemoteRecipe recipe) {
                    if (!seenIds.contains(recipe.getId())) {
                        seenIds.add(recipe.getId());
                        appendTo.add(recipe);
                        remaining[0]--;
                    }
                    step[0].run();
                }

                @Override
                public void onError(String message) {
                    runOnMain(onComplete);
                }
            });
        };
        step[0].run();
    }

    /**
     * Four meals for the diet screen: breakfast, lunch, dinner, snack-style dessert.
     * Runs network calls sequentially on a background thread; callbacks on main.
     */
    public void fetchFourMealsForDietPlan(DietPlanCallback callback) {
        Random rnd = new Random();
        String[] categories = new String[]{
                "Breakfast",
                LUNCH_CATEGORIES[rnd.nextInt(LUNCH_CATEGORIES.length)],
                DINNER_CATEGORIES[rnd.nextInt(DINNER_CATEGORIES.length)],
                "Dessert"
        };
        List<RemoteRecipe> acc = new ArrayList<>();
        fetchChainedCategory(0, categories, acc, callback);
    }

    private void fetchChainedCategory(int index, String[] categories, List<RemoteRecipe> acc, DietPlanCallback done) {
        if (index >= categories.length) {
            runOnMain(() -> done.onSuccess(acc, categories));
            return;
        }
        fetchRandomFromCategoryOrRandom(categories[index], new SingleRecipeCallback() {
            @Override
            public void onSuccess(RemoteRecipe recipe) {
                acc.add(recipe);
                fetchChainedCategory(index + 1, categories, acc, done);
            }

            @Override
            public void onError(String message) {
                fetchRandomFullMeal(new SingleRecipeCallback() {
                    @Override
                    public void onSuccess(RemoteRecipe recipe) {
                        acc.add(recipe);
                        fetchChainedCategory(index + 1, categories, acc, done);
                    }

                    @Override
                    public void onError(String message) {
                        runOnMain(() -> done.onError(message));
                    }
                });
            }
        });
    }

    public void fetchRandomFromCategoryOrRandom(String category, SingleRecipeCallback cb) {
        if (category == null || category.trim().isEmpty()) {
            fetchRandomFullMeal(cb);
            return;
        }
        api.filterByCategory(category.trim()).enqueue(new Callback<TheMealDbDto.FilterResponse>() {
            @Override
            public void onResponse(Call<TheMealDbDto.FilterResponse> call, Response<TheMealDbDto.FilterResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().meals == null
                        || response.body().meals.isEmpty()) {
                    fetchRandomFullMeal(cb);
                    return;
                }
                List<TheMealDbDto.FilterMealPreview> previews = new ArrayList<>(response.body().meals);
                Collections.shuffle(previews, new Random());
                lookupFirstWorking(previews, 0, cb);
            }

            @Override
            public void onFailure(Call<TheMealDbDto.FilterResponse> call, Throwable t) {
                fetchRandomFullMeal(cb);
            }
        });
    }

    private void lookupFirstWorking(List<TheMealDbDto.FilterMealPreview> previews, int i, SingleRecipeCallback cb) {
        if (i >= previews.size()) {
            fetchRandomFullMeal(cb);
            return;
        }
        TheMealDbDto.FilterMealPreview p = previews.get(i);
        if (p == null || p.idMeal == null || p.idMeal.trim().isEmpty()) {
            lookupFirstWorking(previews, i + 1, cb);
            return;
        }
        lookupByIdInternal(p.idMeal.trim(), new SingleRecipeCallback() {
            @Override
            public void onSuccess(RemoteRecipe recipe) {
                cb.onSuccess(recipe);
            }

            @Override
            public void onError(String message) {
                lookupFirstWorking(previews, i + 1, cb);
            }
        });
    }

    public void fetchRandomFullMeal(SingleRecipeCallback cb) {
        api.randomMeal().enqueue(new Callback<TheMealDbDto.SearchResponse>() {
            @Override
            public void onResponse(Call<TheMealDbDto.SearchResponse> call, Response<TheMealDbDto.SearchResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().meals == null
                        || response.body().meals.isEmpty()) {
                    runOnMain(() -> cb.onError("Random meal unavailable"));
                    return;
                }
                RemoteRecipe r = mapMeal(response.body().meals.get(0));
                if (r == null) {
                    runOnMain(() -> cb.onError("Invalid meal data"));
                    return;
                }
                runOnMain(() -> cb.onSuccess(r));
            }

            @Override
            public void onFailure(Call<TheMealDbDto.SearchResponse> call, Throwable t) {
                runOnMain(() -> cb.onError(t != null ? t.getMessage() : "network error"));
            }
        });
    }

    /**
     * Up to {@code limit} random recipes from a TheMealDB category, excluding {@code excludeRecipeId}.
     */
    public void fetchAlternativesFromCategory(String category, String excludeRecipeId, int limit, RemoteRecipesCallback cb) {
        if (category == null || category.trim().isEmpty()) {
            fetchRandomPool(excludeRecipeId, limit, cb);
            return;
        }
        api.filterByCategory(category.trim()).enqueue(new Callback<TheMealDbDto.FilterResponse>() {
            @Override
            public void onResponse(Call<TheMealDbDto.FilterResponse> call, Response<TheMealDbDto.FilterResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().meals == null
                        || response.body().meals.isEmpty()) {
                    fetchRandomPool(excludeRecipeId, limit, cb);
                    return;
                }
                List<TheMealDbDto.FilterMealPreview> previews = new ArrayList<>(response.body().meals);
                final String excludeNum = normalizeThemealId(excludeRecipeId);
                previews.removeIf(p -> p != null && excludeNum != null && excludeNum.equals(p.idMeal));
                Collections.shuffle(previews, new Random());
                collectLookups(previews, 0, new ArrayList<>(), limit, cb);
            }

            @Override
            public void onFailure(Call<TheMealDbDto.FilterResponse> call, Throwable t) {
                fetchRandomPool(excludeRecipeId, limit, cb);
            }
        });
    }

    private void fetchRandomPool(String excludeRecipeId, int limit, RemoteRecipesCallback cb) {
        List<RemoteRecipe> out = new ArrayList<>();
        fetchRandomChain(excludeRecipeId, out, limit, cb);
    }

    private void fetchRandomChain(String excludeId, List<RemoteRecipe> out, int target, RemoteRecipesCallback cb) {
        if (out.size() >= target) {
            runOnMain(() -> cb.onSuccess(out));
            return;
        }
        fetchRandomFullMeal(new SingleRecipeCallback() {
            @Override
            public void onSuccess(RemoteRecipe recipe) {
                if (excludeId != null && excludeId.equals(recipe.getId())) {
                    fetchRandomChain(excludeId, out, target, cb);
                    return;
                }
                boolean dup = false;
                for (RemoteRecipe r : out) {
                    if (r.getId().equals(recipe.getId())) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    out.add(recipe);
                }
                if (out.size() >= target) {
                    runOnMain(() -> cb.onSuccess(out));
                } else {
                    fetchRandomChain(excludeId, out, target, cb);
                }
            }

            @Override
            public void onError(String message) {
                if (out.isEmpty()) {
                    runOnMain(() -> cb.onError(message));
                } else {
                    runOnMain(() -> cb.onSuccess(out));
                }
            }
        });
    }

    private void collectLookups(
            List<TheMealDbDto.FilterMealPreview> previews,
            int i,
            List<RemoteRecipe> out,
            int target,
            RemoteRecipesCallback cb
    ) {
        if (out.size() >= target || i >= previews.size()) {
            if (out.isEmpty()) {
                fetchRandomPool(null, target, cb);
            } else {
                runOnMain(() -> cb.onSuccess(out));
            }
            return;
        }
        TheMealDbDto.FilterMealPreview p = previews.get(i);
        if (p == null || p.idMeal == null) {
            collectLookups(previews, i + 1, out, target, cb);
            return;
        }
        lookupByIdInternal(p.idMeal.trim(), new SingleRecipeCallback() {
            @Override
            public void onSuccess(RemoteRecipe recipe) {
                boolean dup = false;
                for (RemoteRecipe r : out) {
                    if (r.getId().equals(recipe.getId())) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    out.add(recipe);
                }
                collectLookups(previews, i + 1, out, target, cb);
            }

            @Override
            public void onError(String message) {
                collectLookups(previews, i + 1, out, target, cb);
            }
        });
    }

    private void lookupByIdInternal(String idMeal, SingleRecipeCallback cb) {
        api.lookupById(idMeal).enqueue(new Callback<TheMealDbDto.SearchResponse>() {
            @Override
            public void onResponse(Call<TheMealDbDto.SearchResponse> call, Response<TheMealDbDto.SearchResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().meals == null
                        || response.body().meals.isEmpty()) {
                    runOnMain(() -> cb.onError("lookup failed"));
                    return;
                }
                RemoteRecipe r = mapMeal(response.body().meals.get(0));
                if (r == null) {
                    runOnMain(() -> cb.onError("bad meal"));
                    return;
                }
                runOnMain(() -> cb.onSuccess(r));
            }

            @Override
            public void onFailure(Call<TheMealDbDto.SearchResponse> call, Throwable t) {
                runOnMain(() -> cb.onError(t != null ? t.getMessage() : "network"));
            }
        });
    }

    private void runOnMain(Runnable r) {
        mainHandler.post(r);
    }

    /** Strip {@code themealdb_} prefix so we can compare with API {@code idMeal}. */
    static String normalizeThemealId(String id) {
        if (id == null) return null;
        if (id.startsWith("themealdb_")) {
            return id.substring("themealdb_".length());
        }
        return id;
    }

    private RemoteRecipe mapMeal(TheMealDbDto.Meal m) {
        if (m == null) return null;
        String id = m.idMeal;
        String title = m.strMeal;
        if (id == null || id.trim().isEmpty() || title == null || title.trim().isEmpty()) return null;

        List<String> ingredients = new ArrayList<>();
        addIngredient(ingredients, m.strIngredient1, m.strMeasure1);
        addIngredient(ingredients, m.strIngredient2, m.strMeasure2);
        addIngredient(ingredients, m.strIngredient3, m.strMeasure3);
        addIngredient(ingredients, m.strIngredient4, m.strMeasure4);
        addIngredient(ingredients, m.strIngredient5, m.strMeasure5);
        addIngredient(ingredients, m.strIngredient6, m.strMeasure6);
        addIngredient(ingredients, m.strIngredient7, m.strMeasure7);
        addIngredient(ingredients, m.strIngredient8, m.strMeasure8);
        addIngredient(ingredients, m.strIngredient9, m.strMeasure9);
        addIngredient(ingredients, m.strIngredient10, m.strMeasure10);
        addIngredient(ingredients, m.strIngredient11, m.strMeasure11);
        addIngredient(ingredients, m.strIngredient12, m.strMeasure12);
        addIngredient(ingredients, m.strIngredient13, m.strMeasure13);
        addIngredient(ingredients, m.strIngredient14, m.strMeasure14);
        addIngredient(ingredients, m.strIngredient15, m.strMeasure15);
        addIngredient(ingredients, m.strIngredient16, m.strMeasure16);
        addIngredient(ingredients, m.strIngredient17, m.strMeasure17);
        addIngredient(ingredients, m.strIngredient18, m.strMeasure18);
        addIngredient(ingredients, m.strIngredient19, m.strMeasure19);
        addIngredient(ingredients, m.strIngredient20, m.strMeasure20);

        String instructions = m.strInstructions != null ? m.strInstructions.trim() : "";
        List<String> steps = splitSteps(instructions);

        return new RemoteRecipe(
                "themealdb_" + id,
                title.trim(),
                m.strMealThumb,
                ingredients,
                steps,
                instructions
        );
    }

    private void addIngredient(List<String> out, String ingredient, String measure) {
        if (ingredient == null) return;
        String ing = ingredient.trim();
        if (ing.isEmpty()) return;

        String mea = measure != null ? measure.trim() : "";
        if (!mea.isEmpty()) {
            out.add(mea + " " + ing);
        } else {
            out.add(ing);
        }
    }

    private List<String> splitSteps(String instructions) {
        if (instructions == null) return Collections.emptyList();
        String raw = instructions.trim();
        if (raw.isEmpty()) return Collections.emptyList();

        String[] lines = raw.split("\\r?\\n+");
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            String s = line.trim();
            if (s.isEmpty()) continue;
            out.add(s);
        }
        if (out.size() >= 3) return out;

        String[] byDot = raw.split("\\.\\s+");
        out.clear();
        for (String p : byDot) {
            String s = p.trim();
            if (s.isEmpty()) continue;
            if (!s.endsWith(".")) s = s + ".";
            out.add(s);
        }
        return out.isEmpty() ? Collections.singletonList(raw) : out;
    }
}
