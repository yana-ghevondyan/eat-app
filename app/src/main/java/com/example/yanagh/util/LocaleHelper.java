package com.example.yanagh.util;

import android.content.Context;
import android.content.res.Configuration;

import com.example.yanagh.data.UserPrefs;

import java.util.Locale;

public final class LocaleHelper {
    private LocaleHelper() {}

    public static Context wrap(Context context) {
        String code = UserPrefs.language(context);
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
