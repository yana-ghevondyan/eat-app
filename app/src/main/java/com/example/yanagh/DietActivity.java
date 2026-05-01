package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yanagh.data.DietDayPlan;
import com.example.yanagh.data.DietPlanner;
import com.example.yanagh.data.DietSlotEntry;
import com.example.yanagh.data.Recipe;
import com.example.yanagh.data.RecipeRepository;
import com.example.yanagh.data.RecipeStore;
import com.example.yanagh.data.RemoteRecipe;
import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivityDietBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DietActivity extends BaseActivity implements DietDayAdapter.Listener {

    private static final String SLOT_BREAKFAST = "breakfast";
    private static final String SLOT_LUNCH = "lunch";
    private static final String SLOT_DINNER = "dinner";
    private static final String SLOT_SNACK = "snack";
    private static final String[] SLOTS = {
            SLOT_BREAKFAST, SLOT_LUNCH, SLOT_DINNER, SLOT_SNACK
    };

    private ActivityDietBinding binding;
    private DietDayAdapter adapter;
    private RecipeRepository repository;
    private boolean loadingOnlinePlan;
    private boolean autoFetchStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDietBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        repository = new RecipeRepository();

        adapter = new DietDayAdapter(this);
        binding.rvMeals.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMeals.setAdapter(adapter);

        setupGoalSpinner();

        binding.btnGenerateDay.setOnClickListener(v -> {
            UserPrefs.clearDailyDietSlots(this);
            UserPrefs.setDailyDietPlanSeed(this, new Random().nextLong());
            UserPrefs.clearDailyDietMealDone(this);
            fetchOnlineDietPlan(true);
        });

        BottomNavHelper.wire(this, binding.bottomNav, R.id.nav_diet);

        DietDayPlan saved = UserPrefs.getDietDayPlan(this);
        if (!isPlanComplete(saved) && !autoFetchStarted) {
            autoFetchStarted = true;
            fetchOnlineDietPlan(false);
        } else {
            refreshHeaderAndAdapter();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!loadingOnlinePlan) {
            refreshHeaderAndAdapter();
        }
    }

    private void setupGoalSpinner() {
        String[] labels = new String[]{
                getString(R.string.goal_lose),
                getString(R.string.goal_maintain),
                getString(R.string.goal_gain)
        };
        binding.spGoal.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, labels));

        UserPrefs.WeightGoal current = UserPrefs.weightGoal(this);
        int sel = 1;
        if (current == UserPrefs.WeightGoal.LOSE) sel = 0;
        else if (current == UserPrefs.WeightGoal.GAIN) sel = 2;
        binding.spGoal.setSelection(sel);

        binding.spGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserPrefs.WeightGoal goal = UserPrefs.WeightGoal.MAINTAIN;
                if (position == 0) goal = UserPrefs.WeightGoal.LOSE;
                else if (position == 2) goal = UserPrefs.WeightGoal.GAIN;
                UserPrefs.setWeightGoal(DietActivity.this, goal);
                refreshHeaderAndAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private boolean isPlanComplete(DietDayPlan p) {
        if (p == null) return false;
        for (String slot : SLOTS) {
            if (!entryOk(p.get(slot))) {
                return false;
            }
        }
        return true;
    }

    private boolean entryOk(DietSlotEntry e) {
        if (e == null) return false;
        if ("remote".equals(e.kind) && e.remote != null) {
            return true;
        }
        if ("local".equals(e.kind) && e.localId != null && RecipeStore.byId(e.localId) != null) {
            return true;
        }
        return false;
    }

    private void fetchOnlineDietPlan(boolean userRequested) {
        if (loadingOnlinePlan) {
            return;
        }
        loadingOnlinePlan = true;
        binding.dietPlanLoading.setVisibility(View.VISIBLE);
        if (userRequested) {
            Toast.makeText(this, R.string.diet_loading_plan, Toast.LENGTH_SHORT).show();
        }

        repository.fetchFourMealsForDietPlan(new RecipeRepository.DietPlanCallback() {
            @Override
            public void onSuccess(List<RemoteRecipe> meals, String[] categoriesUsed) {
                runOnUiThread(() -> {
                    loadingOnlinePlan = false;
                    binding.dietPlanLoading.setVisibility(View.GONE);
                    if (meals == null || meals.size() < 4 || categoriesUsed == null || categoriesUsed.length < 4) {
                        Toast.makeText(DietActivity.this, R.string.diet_online_unavailable, Toast.LENGTH_LONG).show();
                        refreshHeaderAndAdapter();
                        return;
                    }
                    UserPrefs.clearDailyDietMealDone(DietActivity.this);
                    DietDayPlan plan = new DietDayPlan();
                    for (int i = 0; i < 4; i++) {
                        DietSlotEntry e = new DietSlotEntry();
                        e.kind = "remote";
                        e.remote = meals.get(i);
                        e.categoryHint = categoriesUsed[i];
                        plan.put(SLOTS[i], e);
                    }
                    UserPrefs.setDietDayPlan(DietActivity.this, plan);
                    refreshHeaderAndAdapter();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingOnlinePlan = false;
                    binding.dietPlanLoading.setVisibility(View.GONE);
                    Toast.makeText(DietActivity.this, R.string.diet_online_unavailable, Toast.LENGTH_LONG).show();
                    refreshHeaderAndAdapter();
                });
            }
        });
    }

    private void refreshHeaderAndAdapter() {
        int target = UserPrefs.dailyCalorieTarget(this);
        DietDayPlan plan = UserPrefs.getDietDayPlan(this);

        int totalKcal = 0;
        List<DietDayAdapter.Row> rows = new ArrayList<>();

        if (isPlanComplete(plan)) {
            for (String slot : SLOTS) {
                DietSlotEntry e = plan.get(slot);
                rows.add(rowFromEntry(slot, e));
                if (e != null && "local".equals(e.kind) && e.localId != null) {
                    Recipe lr = RecipeStore.byId(e.localId);
                    if (lr != null) {
                        totalKcal += lr.getCalories();
                    }
                }
            }
        } else {
            long seed = UserPrefs.dailyDietPlanSeed(this);
            DietPlanner.MealPlan lp = DietPlanner.planForDay(RecipeStore.all(), target, seed);
            rows.add(makeLocalRow(SLOT_BREAKFAST, R.string.diet_meal_breakfast, "Breakfast", lp.breakfast));
            rows.add(makeLocalRow(SLOT_LUNCH, R.string.diet_meal_lunch, "Lunch", lp.lunch));
            rows.add(makeLocalRow(SLOT_DINNER, R.string.diet_meal_dinner, "Dinner", lp.dinner));
            rows.add(makeLocalRow(SLOT_SNACK, R.string.diet_meal_snack, "Snack", lp.snack));
            totalKcal = lp.totalCalories;
        }

        binding.tvSummaryKcal.setText(getString(R.string.diet_summary_kcal_fmt, totalKcal, target));
        adapter.setRows(rows);
        updateProgressUi();
    }

    private DietDayAdapter.Row rowFromEntry(String slot, DietSlotEntry e) {
        int labelRes = mealLabelRes(slot);
        String typeEn = mealTypeEn(slot);
        if (e != null && "remote".equals(e.kind) && e.remote != null) {
            return new DietDayAdapter.Row(slot, labelRes, typeEn, e.categoryHint, null, e.remote);
        }
        Recipe r = e != null && "local".equals(e.kind) ? RecipeStore.byId(e.localId) : null;
        return new DietDayAdapter.Row(slot, labelRes, typeEn, defaultCategoryHint(slot), r, null);
    }

    private DietDayAdapter.Row makeLocalRow(String slot, int labelRes, String typeEn, Recipe planned) {
        Recipe r = resolveSlotRecipe(slot, planned);
        return new DietDayAdapter.Row(slot, labelRes, typeEn, defaultCategoryHint(slot), r, null);
    }

    private static int mealLabelRes(String slot) {
        switch (slot) {
            case SLOT_BREAKFAST:
                return R.string.diet_meal_breakfast;
            case SLOT_LUNCH:
                return R.string.diet_meal_lunch;
            case SLOT_DINNER:
                return R.string.diet_meal_dinner;
            case SLOT_SNACK:
                return R.string.diet_meal_snack;
            default:
                return R.string.diet_meal_breakfast;
        }
    }

    private static String mealTypeEn(String slot) {
        switch (slot) {
            case SLOT_BREAKFAST:
                return "Breakfast";
            case SLOT_LUNCH:
                return "Lunch";
            case SLOT_DINNER:
                return "Dinner";
            case SLOT_SNACK:
                return "Snack";
            default:
                return "Miscellaneous";
        }
    }

    private String defaultCategoryHint(String slotId) {
        Random r = new Random();
        switch (slotId) {
            case SLOT_BREAKFAST:
                return "Breakfast";
            case SLOT_LUNCH:
                return RecipeRepository.LUNCH_CATEGORIES[r.nextInt(RecipeRepository.LUNCH_CATEGORIES.length)];
            case SLOT_DINNER:
                return RecipeRepository.DINNER_CATEGORIES[r.nextInt(RecipeRepository.DINNER_CATEGORIES.length)];
            case SLOT_SNACK:
                return "Dessert";
            default:
                return "Miscellaneous";
        }
    }

    private Recipe resolveSlotRecipe(String slot, Recipe planned) {
        String id = UserPrefs.dailyDietSlotRecipeId(this, slot);
        if (id != null) {
            Recipe byId = RecipeStore.byId(id);
            if (byId != null) {
                return byId;
            }
        }
        return planned;
    }

    private void updateProgressUi() {
        int total = 4;
        int done = 0;
        for (String s : SLOTS) {
            if (UserPrefs.dailyDietMealDone(this, s)) {
                done++;
            }
        }
        binding.tvProgressLabel.setText(getString(R.string.diet_progress_fmt, done, total));
        binding.mealProgress.setMax(total);
        binding.mealProgress.setProgressCompat(done, true);
    }

    @Override
    public void onToggleDone(String slotId, boolean done) {
        UserPrefs.setDailyDietMealDone(this, slotId, done);
        updateProgressUi();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onChangeMeal(
            String slotId,
            String mealTypeEn,
            String categoryHint,
            Recipe currentLocal,
            RemoteRecipe currentRemote
    ) {
        if (currentRemote != null) {
            String cat = categoryHint != null && !categoryHint.isEmpty()
                    ? categoryHint
                    : defaultCategoryHint(slotId);
            repository.fetchAlternativesFromCategory(cat, currentRemote.getId(), 5, new RecipeRepository.RemoteRecipesCallback() {
                @Override
                public void onSuccess(List<RemoteRecipe> recipes) {
                    runOnUiThread(() -> showRemotePickDialog(slotId, cat, recipes));
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() ->
                            Toast.makeText(DietActivity.this, R.string.diet_online_unavailable, Toast.LENGTH_SHORT).show());
                }
            });
            return;
        }

        List<Recipe> pool = new ArrayList<>(RecipeStore.byMealType(mealTypeEn));
        if (currentLocal != null) {
            pool.removeIf(r -> r.getId().equals(currentLocal.getId()));
        }
        if (pool.isEmpty()) {
            Toast.makeText(this, R.string.diet_no_alternatives, Toast.LENGTH_SHORT).show();
            return;
        }
        Collections.shuffle(pool, new Random());
        int n = Math.min(5, pool.size());
        List<Recipe> choices = new ArrayList<>(pool.subList(0, n));
        String[] names = new String[n];
        for (int i = 0; i < n; i++) {
            names[i] = choices.get(i).getName(this);
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.diet_pick_alternative)
                .setItems(names, (dialog, which) -> {
                    Recipe picked = choices.get(which);
                    DietDayPlan plan = UserPrefs.getDietDayPlan(this);
                    if (plan != null && isPlanComplete(plan)) {
                        DietSlotEntry e = new DietSlotEntry();
                        e.kind = "local";
                        e.localId = picked.getId();
                        e.categoryHint = defaultCategoryHint(slotId);
                        plan.put(slotId, e);
                        UserPrefs.setDietDayPlan(this, plan);
                    } else {
                        UserPrefs.setDailyDietSlotRecipeId(this, slotId, picked.getId());
                    }
                    refreshHeaderAndAdapter();
                })
                .show();
    }

    private void showRemotePickDialog(String slotId, String categoryUsed, List<RemoteRecipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            Toast.makeText(this, R.string.diet_no_alternatives, Toast.LENGTH_SHORT).show();
            return;
        }
        int n = recipes.size();
        String[] names = new String[n];
        for (int i = 0; i < n; i++) {
            names[i] = recipes.get(i).getTitle(this);
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.diet_pick_alternative)
                .setItems(names, (dialog, which) -> {
                    RemoteRecipe picked = recipes.get(which);
                    DietDayPlan plan = UserPrefs.getDietDayPlan(this);
                    if (plan == null) {
                        plan = new DietDayPlan();
                    }
                    DietSlotEntry e = new DietSlotEntry();
                    e.kind = "remote";
                    e.remote = picked;
                    e.categoryHint = categoryUsed;
                    plan.put(slotId, e);
                    UserPrefs.setDietDayPlan(this, plan);
                    refreshHeaderAndAdapter();
                })
                .show();
    }

    @Override
    public void onOpenDetail(Recipe recipe, RemoteRecipe remote) {
        if (remote != null) {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("remoteRecipe", remote);
            startActivity(i);
            return;
        }
        if (recipe != null) {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("recipe", recipe);
            startActivity(i);
        }
    }
}
