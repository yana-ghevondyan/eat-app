package com.example.yanagh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.yanagh.data.Recipe;
import com.example.yanagh.data.RecipeStore;
import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivityFavoritesBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends BaseActivity {
    private ActivityFavoritesBinding binding;
    private final List<Recipe> shown = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavHelper.wire(this, binding.bottomNav, R.id.nav_favorites);
        binding.listFavorites.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("recipe", shown.get(position));
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        Set<String> ids = UserPrefs.getFavoriteIds(this);
        shown.clear();
        for (String id : ids) {
            Recipe recipe = RecipeStore.byId(id);
            if (recipe != null) shown.add(recipe);
        }
        List<String> rows = new ArrayList<>();
        for (Recipe recipe : shown) rows.add(recipe.toRowText(this));
        binding.listFavorites.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
        binding.tvEmpty.setText(shown.isEmpty() ? getString(R.string.favorites_empty) : "");
    }
}
