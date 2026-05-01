package com.example.yanagh;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yanagh.data.Recipe;
import com.example.yanagh.data.RecipeStore;
import com.example.yanagh.api.GeminiRecipeTranslateService;
import com.example.yanagh.data.RemoteRecipe;
import com.example.yanagh.data.UserPrefs;
import com.example.yanagh.databinding.ActivityRecipeDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RecipeDetailActivity extends BaseActivity {
    private ActivityRecipeDetailBinding binding;
    private Recipe recipe;
    private RemoteRecipe remoteRecipe;
    private TextToSpeech tts;
    private boolean ttsReady;
    /** True when no installed TTS engine reported Armenian as available. */
    private boolean ttsNeedsVoiceInstall;
    private Locale ttsLocale;
    private List<String> ttsEnginePackages;
    private int ttsEngineIndex;
    private static final String GOOGLE_TTS_ENGINE = "com.google.android.tts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        remoteRecipe = (RemoteRecipe) getIntent().getSerializableExtra("remoteRecipe");
        if (recipe == null && remoteRecipe == null) {
            finish();
            return;
        }
        ttsLocale = "hy".equals(UserPrefs.language(this))
                ? new Locale("hy", "AM")
                : Locale.US;

        startTextToSpeech(ttsLocale);
        render();
        maybeTranslateRemoteRecipe();
        binding.btnReadAll.setOnClickListener(v -> speakAll());
        binding.btnReadStep.setOnClickListener(v -> speakSteps());
        binding.btnFavorite.setOnClickListener(v -> {
            UserPrefs.toggleFavorite(this, getFavoriteId());
            renderFavoriteText();
        });
        binding.btnAddShopping.setOnClickListener(v -> {
            UserPrefs.addShoppingItems(this, getIngredientsForUi());
            Toast.makeText(this, R.string.toast_shopping_added, Toast.LENGTH_SHORT).show();
        });
    }

    private void startTextToSpeech(Locale loc) {
        ttsReady = false;
        ttsNeedsVoiceInstall = false;
        ttsEnginePackages = buildTtsEngineOrder(loc);
        ttsEngineIndex = 0;
        tryNextTtsEngine(loc);
    }

    /**
     * For Armenian, try the device default TTS first (often already configured for hy),
     * then Google and other engines. Google frequently returns LANG_MISSING_DATA until
     * the Armenian voice pack is downloaded.
     */
    private List<String> buildTtsEngineOrder(Locale loc) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        boolean hy = loc != null && "hy".equalsIgnoreCase(loc.getLanguage());
        if (hy) {
            set.add(null);
            set.add(GOOGLE_TTS_ENGINE);
        } else {
            set.add(GOOGLE_TTS_ENGINE);
            set.add(null);
        }
        PackageManager pm = getPackageManager();
        Intent probe = new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE);
        List<ResolveInfo> services = pm.queryIntentServices(probe, PackageManager.GET_META_DATA);
        if (services != null) {
            for (ResolveInfo ri : services) {
                if (ri.serviceInfo != null && ri.serviceInfo.packageName != null) {
                    set.add(ri.serviceInfo.packageName);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private void tryNextTtsEngine(Locale loc) {
        if (ttsEnginePackages == null || ttsEngineIndex >= ttsEnginePackages.size()) {
            ttsReady = false;
            ttsNeedsVoiceInstall = "hy".equalsIgnoreCase(loc.getLanguage());
            return;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        String pkg = ttsEnginePackages.get(ttsEngineIndex);
        ttsEngineIndex++;

        TextToSpeech.OnInitListener listener = status -> {
            if (status != TextToSpeech.SUCCESS) {
                if (tts != null) {
                    tts.stop();
                    tts.shutdown();
                    tts = null;
                }
                tryNextTtsEngine(loc);
                return;
            }
            if (tts == null) {
                tryNextTtsEngine(loc);
                return;
            }
            if (!applyLocaleToTts(tts, loc)) {
                tts.stop();
                tts.shutdown();
                tts = null;
                tryNextTtsEngine(loc);
                return;
            }
            ttsReady = true;
            ttsNeedsVoiceInstall = false;
            finishTtsVoiceTuning(loc);
        };

        try {
            if (pkg == null) {
                tts = new TextToSpeech(this, listener);
            } else {
                tts = new TextToSpeech(this, listener, pkg);
            }
        } catch (Exception e) {
            tryNextTtsEngine(loc);
        }
    }

    private static boolean isLanguageSupported(int result) {
        return result == TextToSpeech.LANG_AVAILABLE
                || result == TextToSpeech.LANG_COUNTRY_AVAILABLE
                || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
    }

    private boolean applyLocaleToTts(TextToSpeech engine, Locale loc) {
        if (engine == null) return false;
        int r = engine.setLanguage(loc);
        if (isLanguageSupported(r)) {
            return true;
        }
        if ("hy".equalsIgnoreCase(loc.getLanguage())) {
            if (loc.getCountry() != null && !loc.getCountry().isEmpty()) {
                r = engine.setLanguage(new Locale("hy"));
                if (isLanguageSupported(r)) {
                    return true;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    r = engine.setLanguage(Locale.forLanguageTag("hy"));
                    if (isLanguageSupported(r)) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
        return false;
    }

    private void finishTtsVoiceTuning(Locale loc) {
        if (tts == null) return;

        tts.setPitch(1.0f);
        tts.setSpeechRate("hy".equalsIgnoreCase(loc.getLanguage()) ? 0.88f : 0.90f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Voice best = pickBestVoice(tts, loc);
                if (best != null) {
                    int vResult = tts.setVoice(best);
                    if (vResult != TextToSpeech.SUCCESS) {
                        applyLocaleToTts(tts, loc);
                    }
                }
            } catch (Exception ignored) {
                applyLocaleToTts(tts, loc);
            }
        }
    }

    private void showTtsMissingDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.tts_dialog_title)
                .setMessage(R.string.tts_missing_voice)
                .setPositiveButton(R.string.tts_install_voices, (d, w) -> openTtsVoiceInstaller())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openTtsVoiceInstaller() {
        try {
            Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignored) {
                Toast.makeText(this, R.string.tts_missing_voice, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void render() {
        if (recipe != null) {
            Glide.with(this)
                    .load(recipe.getImageResId(this))
                    .centerCrop()
                    .into(binding.ivRecipeHeader);

            binding.tvRecipeName.setText(recipe.getName(this));
            binding.tvMeta.setText(getString(
                    R.string.recipe_meta_fmt,
                    recipe.getCalories(),
                    recipe.getMinutes(),
                    RecipeStore.localizedMealType(this, recipe.getType())));
        } else {
            Glide.with(this)
                    .load(remoteRecipe.getImageUrl())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(binding.ivRecipeHeader);

            binding.tvRecipeName.setText(remoteRecipe.getTitle(this));
            binding.tvMeta.setText(getString(R.string.online_recipe_subtitle));
        }

        StringBuilder ing = new StringBuilder();
        for (String line : getIngredientsForUi()) {
            if (ing.length() > 0) {
                ing.append('\n');
            }
            ing.append(getString(R.string.bullet_line, line));
        }
        binding.tvIngredients.setText(ing.toString());

        List<String> steps = getStepsForUi();
        StringBuilder stepText = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                stepText.append("\n\n");
            }
            stepText.append(i + 1).append(") ").append(steps.get(i));
        }
        binding.tvSteps.setText(stepText.toString());
        renderFavoriteText();
    }

    private void maybeTranslateRemoteRecipe() {
        if (remoteRecipe == null || !"hy".equals(UserPrefs.language(this)) || remoteRecipe.hasArmenian()) {
            return;
        }
        new GeminiRecipeTranslateService().translateRecipeToArmenian(
                remoteRecipe.getTitle(),
                remoteRecipe.getIngredients(),
                remoteRecipe.getSteps(),
                new GeminiRecipeTranslateService.TranslateCallback() {
                    @Override
                    public void onSuccess(String titleHy, List<String> ingredientsHy, List<String> stepsHy) {
                        List<String> ing = harmonizeLists(remoteRecipe.getIngredients(), ingredientsHy);
                        List<String> st = harmonizeLists(remoteRecipe.getSteps(), stepsHy);
                        remoteRecipe = remoteRecipe.withArmenian(titleHy, ing, st);
                        UserPrefs.upsertRemoteRecipeInDietPlan(RecipeDetailActivity.this, remoteRecipe);
                        render();
                    }

                    @Override
                    public void onError(String message) {
                        // Silent fallback: keep English text
                    }
                }
        );
    }

    private static List<String> harmonizeLists(List<String> expected, List<String> translated) {
        if (translated == null || translated.isEmpty()) {
            return expected;
        }
        if (expected == null) {
            return translated;
        }
        if (translated.size() >= expected.size()) {
            return new ArrayList<>(translated.subList(0, expected.size()));
        }
        List<String> out = new ArrayList<>(translated);
        for (int i = translated.size(); i < expected.size(); i++) {
            out.add(expected.get(i));
        }
        return out;
    }

    private void renderFavoriteText() {
        boolean isFavorite = UserPrefs.isFavorite(this, getFavoriteId());
        binding.btnFavorite.setText(isFavorite
                ? getString(R.string.remove_favorite)
                : getString(R.string.add_favorite));
    }

    private void speakAll() {
        if (!ensureTtsReady()) {
            return;
        }
        String title = recipe != null ? recipe.getName(this) : remoteRecipe.getTitle(this);
        String text = RecipeTtsHelper.buildSpeakAll(
                this,
                title,
                getIngredientsForUi(),
                getStepsForUi()
        );
        speakInChunks(text);
    }

    private void speakSteps() {
        if (!ensureTtsReady()) {
            return;
        }
        String text = RecipeTtsHelper.buildSpeakSteps(this, getStepsForUi());
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.tts_nothing_to_read, Toast.LENGTH_SHORT).show();
            return;
        }
        speakInChunks(text);
    }

    private boolean ensureTtsReady() {
        if (tts != null && ttsReady) {
            return true;
        }
        if (ttsNeedsVoiceInstall) {
            showTtsMissingDialog();
            return false;
        }
        Toast.makeText(this, R.string.tts_not_ready, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void speakInChunks(String fullText) {
        if (fullText == null || fullText.trim().isEmpty()) {
            Toast.makeText(this, R.string.tts_nothing_to_read, Toast.LENGTH_SHORT).show();
            return;
        }
        final List<String> parts = RecipeTtsHelper.chunks(fullText);
        if (parts.isEmpty()) {
            Toast.makeText(this, R.string.tts_nothing_to_read, Toast.LENGTH_SHORT).show();
            return;
        }
        tts.stop();
        final android.os.Bundle params = ttsParams();

        if (parts.size() == 1) {
            tts.speak(parts.get(0), TextToSpeech.QUEUE_FLUSH, params, "recipe_0");
            return;
        }

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            private int nextIndex = 1;

            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                if (nextIndex < parts.size()) {
                    String uid = "recipe_" + nextIndex;
                    tts.speak(parts.get(nextIndex), TextToSpeech.QUEUE_ADD, params, uid);
                    nextIndex++;
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    onDone(utteranceId);
                }
            }

            @Override
            public void onError(String utteranceId, int errorCode) {
                onDone(utteranceId);
            }
        });

        tts.speak(parts.get(0), TextToSpeech.QUEUE_FLUSH, params, "recipe_0");
    }

    private String getFavoriteId() {
        if (recipe != null) return recipe.getId();
        return remoteRecipe != null ? remoteRecipe.getId() : "";
    }

    private List<String> getIngredientsForUi() {
        if (recipe != null) return recipe.getIngredients(this);
        return remoteRecipe != null ? remoteRecipe.getIngredients(this) : java.util.Collections.emptyList();
    }

    private List<String> getStepsForUi() {
        if (recipe != null) return recipe.getSteps(this);
        return remoteRecipe != null ? remoteRecipe.getSteps(this) : java.util.Collections.emptyList();
    }

    private static Voice pickBestVoice(TextToSpeech tts, Locale target) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        Set<Voice> voices = tts.getVoices();
        if (voices == null || voices.isEmpty()) {
            return null;
        }
        Voice best = null;
        int bestScore = Integer.MIN_VALUE;
        String lang = target.getLanguage();
        String country = target.getCountry();
        for (Voice v : voices) {
            if (v == null) continue;
            Locale vl = v.getLocale();
            if (vl == null || !lang.equalsIgnoreCase(vl.getLanguage())) continue;

            int score = v.getQuality();
            if (country != null && !country.isEmpty()
                    && country.equalsIgnoreCase(vl.getCountry())) {
                score += 250;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String f : v.getFeatures()) {
                    if (f == null) continue;
                    String fl = f.toLowerCase(Locale.ROOT);
                    if (fl.contains("neural") || fl.contains("wavenet") || fl.contains("network")) {
                        score += 120;
                        break;
                    }
                }
            }
            String name = v.getName() != null ? v.getName().toLowerCase(Locale.ROOT) : "";
            if (name.contains("wavenet") || name.contains("neural") || name.contains("premium")) {
                score += 60;
            }
            if (!v.isNetworkConnectionRequired()) {
                score += 30;
            }
            if (score > bestScore) {
                bestScore = score;
                best = v;
            }
        }
        return best;
    }

    private android.os.Bundle ttsParams() {
        android.os.Bundle b = new android.os.Bundle();
        b.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.92f);
        return b;
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.setOnUtteranceProgressListener(null);
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
