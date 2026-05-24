package com.samil.kelimequiz.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;

    private static final String PREF_NAME = "kelime_quiz_theme";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ThemeManager() {
    }

    public static void applySavedTheme(Context context) {
        applyThemeIfChanged(getSavedTheme(context));
    }

    public static int getSavedTheme(Context context) {
        return getPreferences(context).getInt(KEY_THEME_MODE, THEME_LIGHT);
    }

    public static void saveAndApplyTheme(Context context, int themeMode) {
        if (getSavedTheme(context) == themeMode && AppCompatDelegate.getDefaultNightMode() == themeMode) {
            return;
        }
        getPreferences(context).edit().putInt(KEY_THEME_MODE, themeMode).apply();
        applyThemeIfChanged(themeMode);
    }

    private static void applyThemeIfChanged(int themeMode) {
        if (AppCompatDelegate.getDefaultNightMode() == themeMode) {
            return;
        }
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
