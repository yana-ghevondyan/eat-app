package com.example.yanagh;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yanagh.util.LocaleHelper;

/**
 * Applies the user-selected locale ({@link com.example.yanagh.data.UserPrefs}) to every screen.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
