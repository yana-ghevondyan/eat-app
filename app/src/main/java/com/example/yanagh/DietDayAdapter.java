package com.example.yanagh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanagh.data.Recipe;
import com.example.yanagh.data.RemoteRecipe;
import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ItemDietMealBinding;

import java.util.ArrayList;
import java.util.List;

public class DietDayAdapter extends RecyclerView.Adapter<DietDayAdapter.MealVH> {

    public interface Listener {
        void onToggleDone(String slotId, boolean done);

        void onChangeMeal(String slotId, String mealTypeEn, String categoryHint, Recipe local, RemoteRecipe remote);

        void onOpenDetail(Recipe recipe, RemoteRecipe remote);
    }

    public static final class Row {
        public final String slotId;
        public final int mealLabelRes;
        public final String mealTypeEn;
        public final String categoryHint;
        public Recipe recipe;
        public RemoteRecipe remote;

        public Row(
                String slotId,
                int mealLabelRes,
                String mealTypeEn,
                String categoryHint,
                Recipe recipe,
                RemoteRecipe remote
        ) {
            this.slotId = slotId;
            this.mealLabelRes = mealLabelRes;
            this.mealTypeEn = mealTypeEn;
            this.categoryHint = categoryHint;
            this.recipe = recipe;
            this.remote = remote;
        }

        public boolean hasRemote() {
            return remote != null;
        }

        public boolean hasLocal() {
            return recipe != null;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    public DietDayAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> next) {
        rows.clear();
        if (next != null) {
            rows.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDietMealBinding b = ItemDietMealBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MealVH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull MealVH holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    final class MealVH extends RecyclerView.ViewHolder {
        private final ItemDietMealBinding binding;

        MealVH(ItemDietMealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Row row) {
            Context ctx = binding.getRoot().getContext();
            binding.tvMealKind.setText(row.mealLabelRes);

            if (!row.hasRemote() && !row.hasLocal()) {
                binding.tvRecipeName.setText(R.string.diet_empty_slot);
                binding.tvMeta.setText("");
            } else if (row.hasRemote()) {
                binding.tvRecipeName.setText(row.remote.getTitle(ctx));
                binding.tvMeta.setText(ctx.getString(R.string.diet_row_remote_meta));
            } else {
                binding.tvRecipeName.setText(row.recipe.getName(ctx));
                binding.tvMeta.setText(ctx.getString(
                        R.string.diet_row_subtitle,
                        row.recipe.getCalories(),
                        row.recipe.getMinutes()));
            }

            boolean done = UserPrefs.dailyDietMealDone(ctx, row.slotId);

            binding.checkDone.setOnCheckedChangeListener(null);
            binding.checkDone.setChecked(done);
            binding.checkDone.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                if (listener != null) {
                    listener.onToggleDone(row.slotId, isChecked);
                }
            });

            styleCard(done);

            binding.btnChangeMeal.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChangeMeal(
                            row.slotId,
                            row.mealTypeEn,
                            row.categoryHint,
                            row.recipe,
                            row.remote
                    );
                }
            });

            View.OnClickListener open = v -> {
                if (listener == null) return;
                if (row.hasRemote()) {
                    listener.onOpenDetail(null, row.remote);
                } else if (row.hasLocal()) {
                    listener.onOpenDetail(row.recipe, null);
                }
            };
            binding.mealContent.setOnClickListener(open);
            binding.tvRecipeName.setOnClickListener(open);
            binding.tvMeta.setOnClickListener(open);
            binding.tvMealKind.setOnClickListener(open);
        }

        private void styleCard(boolean done) {
            float d = binding.mealCard.getResources().getDisplayMetrics().density;
            int primary = ContextCompat.getColor(binding.mealCard.getContext(), R.color.primary);
            int container = ContextCompat.getColor(binding.mealCard.getContext(), R.color.primary_container);
            int cardBg = ContextCompat.getColor(binding.mealCard.getContext(), R.color.card_background);

            if (done) {
                binding.mealCard.setStrokeWidth(Math.round(2 * d));
                binding.mealCard.setStrokeColor(primary);
                binding.mealCard.setCardBackgroundColor(container);
                binding.mealContent.animate().cancel();
                binding.mealContent.animate().alpha(0.84f).setDuration(220).start();
            } else {
                binding.mealCard.setStrokeWidth(0);
                binding.mealCard.setCardBackgroundColor(cardBg);
                binding.mealContent.animate().cancel();
                binding.mealContent.animate().alpha(1f).setDuration(220).start();
            }
        }
    }
}
