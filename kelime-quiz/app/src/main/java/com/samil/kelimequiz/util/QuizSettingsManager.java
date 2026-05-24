package com.samil.kelimequiz.util;

import android.content.Context;
import android.content.SharedPreferences;

public class QuizSettingsManager {
    public static final int DEFAULT_QUESTION_LIMIT = 10;
    public static final int MIN_QUESTION_LIMIT = 5;
    public static final int MAX_QUESTION_LIMIT = 20;

    private static final String PREF_NAME = "kelime_quiz_settings";
    private static final String KEY_QUESTION_LIMIT = "question_limit";

    private final SharedPreferences preferences;

    public QuizSettingsManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getQuestionLimit() {
        return preferences.getInt(KEY_QUESTION_LIMIT, DEFAULT_QUESTION_LIMIT);
    }

    public void saveQuestionLimit(int questionLimit) {
        preferences.edit().putInt(KEY_QUESTION_LIMIT, sanitizeQuestionLimit(questionLimit)).apply();
    }

    public int increaseQuestionLimit() {
        int questionLimit = sanitizeQuestionLimit(getQuestionLimit() + 1);
        saveQuestionLimit(questionLimit);
        return questionLimit;
    }

    public int decreaseQuestionLimit() {
        int questionLimit = sanitizeQuestionLimit(getQuestionLimit() - 1);
        saveQuestionLimit(questionLimit);
        return questionLimit;
    }

    private int sanitizeQuestionLimit(int questionLimit) {
        if (questionLimit < MIN_QUESTION_LIMIT) {
            return MIN_QUESTION_LIMIT;
        }
        if (questionLimit > MAX_QUESTION_LIMIT) {
            return MAX_QUESTION_LIMIT;
        }
        return questionLimit;
    }
}
