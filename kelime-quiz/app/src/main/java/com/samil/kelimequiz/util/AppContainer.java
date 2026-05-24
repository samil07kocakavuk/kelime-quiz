package com.samil.kelimequiz.util;

import android.content.Context;

import com.samil.kelimequiz.data.bootstrap.WordSeedBootstrapper;
import com.samil.kelimequiz.data.local.AppDatabase;
import com.samil.kelimequiz.data.repository.AuthRepository;
import com.samil.kelimequiz.data.repository.QuizRepository;
import com.samil.kelimequiz.data.repository.WordRepository;
import com.samil.kelimequiz.util.security.PasswordHasher;

public class AppContainer {
    private static volatile AppContainer instance;

    public final AuthRepository authRepository;
    public final WordRepository wordRepository;
    public final QuizRepository quizRepository;
    public final WordSeedBootstrapper wordSeedBootstrapper;

    private AppContainer(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        authRepository = new AuthRepository(database.userDao(), new PasswordHasher());
        wordRepository = new WordRepository(database.wordDao(), database.wordSampleDao());
        quizRepository = new QuizRepository(database.wordDao(), database.quizProgressDao(), database.quizResultDao());
        wordSeedBootstrapper = new WordSeedBootstrapper(context, wordRepository);
    }

    public static AppContainer from(Context context) {
        if (instance == null) {
            synchronized (AppContainer.class) {
                if (instance == null) {
                    instance = new AppContainer(context.getApplicationContext());
                }
            }
        }
        return instance;
    }
}
