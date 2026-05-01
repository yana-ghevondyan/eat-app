package com.example.yanagh;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.yanagh.data.UserPrefs;

public class YanaghApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (UserPrefs.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
