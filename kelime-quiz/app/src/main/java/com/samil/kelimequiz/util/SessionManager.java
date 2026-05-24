package com.samil.kelimequiz.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "kelime_quiz_session";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void clear() {
        clearSavedSession(prefs);
    }

    public static void clearSavedSession(Context context) {
        clearSavedSession(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE));
    }

    private static void clearSavedSession(SharedPreferences prefs) {
        prefs.edit().clear().apply();
    }
}
