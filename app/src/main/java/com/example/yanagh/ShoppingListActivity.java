package com.example.yanagh;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivityShoppingListBinding;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListActivity extends BaseActivity {
    private ActivityShoppingListBinding binding;
    private final List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShoppingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadItems();
        binding.btnClear.setOnClickListener(v -> {
            items.clear();
            UserPrefs.saveShoppingItems(this, items);
            loadItems();
        });
    }

    private void loadItems() {
        items.clear();
        items.addAll(UserPrefs.getShoppingItems(this));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
        binding.listShopping.setAdapter(adapter);
        binding.listShopping.setChoiceMode(android.widget.ListView.CHOICE_MODE_MULTIPLE);
        binding.tvEmpty.setText(items.isEmpty() ? getString(R.string.shopping_empty) : "");
    }
}
