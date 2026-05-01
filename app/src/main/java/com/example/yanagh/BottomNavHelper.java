package com.example.yanagh;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class BottomNavHelper {
    private BottomNavHelper() {}

    public static void wire(Activity activity, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> target = null;
            if (id == R.id.nav_home) target = FoodMainActivity.class;
            else if (id == R.id.nav_search) target = SearchActivity.class;
            else if (id == R.id.nav_diet) target = DietActivity.class;
            else if (id == R.id.nav_favorites) target = FavoritesActivity.class;
            else if (id == R.id.nav_settings) target = SettingsActivity.class;
            if (target != null && !activity.getClass().equals(target)) {
                Intent intent = new Intent(activity, target);
                activity.startActivity(intent);
            }
            return true;
        });
    }
}
