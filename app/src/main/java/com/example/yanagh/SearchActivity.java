package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.yanagh.data.Recipe;
import com.example.yanagh.data.RecipeListAdapter;
import com.example.yanagh.data.RecipeStore;
import com.example.yanagh.data.RecipeRepository;
import com.example.yanagh.data.RemoteRecipe;
import com.example.yanagh.data.RemoteRecipeListAdapter;
import com.example.yanagh.databinding.ActivitySearchBinding;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends BaseActivity {
    private ActivitySearchBinding binding;
    private final List<Recipe> shown = new ArrayList<>();
    private final List<RemoteRecipe> shownRemote = new ArrayList<>();
    private RecipeRepository repository;
    private boolean showOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new RecipeRepository();

        String[] labels = getResources().getStringArray(R.array.recipe_type_labels);
        binding.spType.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, labels));

        BottomNavHelper.wire(this, binding.bottomNav, R.id.nav_search);

        binding.tgSource.check(binding.btnSourceLocal.getId());
        binding.tgSource.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            showOnline = checkedId == binding.btnSourceOnline.getId();
            loadData();
        });

        binding.btnApplyFilters.setOnClickListener(v -> loadData());

        binding.btnLoadMoreOnline.setOnClickListener(v -> loadMoreOnline());

        loadData();
    }

    private void loadData() {
        binding.progressOnline.setVisibility(View.GONE);
        binding.tvOnlineHint.setVisibility(showOnline ? View.VISIBLE : View.GONE);
        binding.btnLoadMoreOnline.setVisibility(showOnline ? View.VISIBLE : View.GONE);

        String query = String.valueOf(binding.etSearch.getText());
        int maxCalories = parseIntSafe(String.valueOf(binding.etCalories.getText()));
        int maxMinutes = parseIntSafe(String.valueOf(binding.etMinutes.getText()));
        int pos = binding.spType.getSelectedItemPosition();
        String[] values = getResources().getStringArray(R.array.recipe_type_values);
        String type = values[Math.max(0, Math.min(pos, values.length - 1))];

        if (!showOnline) {
            shown.clear();
            shown.addAll(RecipeStore.filter(query, maxCalories, maxMinutes, type));
            binding.listRecipes.setAdapter(new RecipeListAdapter(this, shown));
            binding.listRecipes.setOnItemClickListener((parent, view, position, id) -> {
                Intent i = new Intent(this, RecipeDetailActivity.class);
                i.putExtra("recipe", shown.get(position));
                startActivity(i);
            });
            return;
        }

        binding.progressOnline.setVisibility(View.VISIBLE);
        repository.searchOnlineMerged(query, new RecipeRepository.RemoteRecipesCallback() {
            @Override
            public void onSuccess(List<RemoteRecipe> recipes) {
                runOnUiThread(() -> {
                    binding.progressOnline.setVisibility(View.GONE);
                    shownRemote.clear();
                    if (recipes != null) {
                        shownRemote.addAll(recipes);
                    }
                    binding.listRecipes.setAdapter(new RemoteRecipeListAdapter(SearchActivity.this, shownRemote));
                    binding.listRecipes.setOnItemClickListener((parent, view, position, id) -> {
                        Intent i = new Intent(SearchActivity.this, RecipeDetailActivity.class);
                        i.putExtra("remoteRecipe", shownRemote.get(position));
                        startActivity(i);
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    binding.progressOnline.setVisibility(View.GONE);
                    Toast.makeText(SearchActivity.this, message, Toast.LENGTH_LONG).show();
                    shownRemote.clear();
                    binding.listRecipes.setAdapter(new RemoteRecipeListAdapter(SearchActivity.this, shownRemote));
                });
            }
        });
    }

    private void loadMoreOnline() {
        if (!showOnline) {
            return;
        }
        binding.progressOnline.setVisibility(View.VISIBLE);
        Set<String> seen = new LinkedHashSet<>();
        for (RemoteRecipe r : shownRemote) {
            seen.add(r.getId());
        }
        repository.appendRandomRecipes(20, seen, shownRemote, () -> runOnUiThread(() -> {
            binding.progressOnline.setVisibility(View.GONE);
            if (binding.listRecipes.getAdapter() instanceof RemoteRecipeListAdapter) {
                ((RemoteRecipeListAdapter) binding.listRecipes.getAdapter()).notifyDataSetChanged();
            }
        }));
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }
}
