package com.samil.kelimequiz;

import android.app.Application;

import com.samil.kelimequiz.util.ThemeManager;

public class KelimeQuizApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
    }
}
