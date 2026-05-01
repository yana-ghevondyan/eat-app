package com.example.yanagh.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.yanagh.R;

import java.util.List;

public class RecipeListAdapter extends ArrayAdapter<Recipe> {
    public RecipeListAdapter(@NonNull Context context, @NonNull List<Recipe> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_recipe_row, parent, false);
        }

        Recipe r = getItem(position);
        if (r == null) return v;

        ImageView image = v.findViewById(R.id.ivRecipe);
        TextView title = v.findViewById(R.id.tvTitle);
        TextView sub = v.findViewById(R.id.tvSub);

        title.setText(r.getName(getContext()));
        sub.setText(getContext().getString(R.string.diet_row_subtitle, r.getCalories(), r.getMinutes()));

        Glide.with(getContext())
                .load(r.getImageResId(getContext()))
                .centerCrop()
                .into(image);

        return v;
    }
}

