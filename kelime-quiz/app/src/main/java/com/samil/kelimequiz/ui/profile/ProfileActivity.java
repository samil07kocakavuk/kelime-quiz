package com.samil.kelimequiz.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.AppDatabase;
import com.samil.kelimequiz.data.local.entity.UserEntity;
import com.samil.kelimequiz.ui.auth.LoginActivity;
import com.samil.kelimequiz.ui.word.WordPoolActivity;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;
import com.samil.kelimequiz.util.QuizSettingsManager;
import com.samil.kelimequiz.util.SessionManager;
import com.samil.kelimequiz.util.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvProfileName;
    private TextView tvProfileInfo;
    private TextView tvThemeMode;
    private TextView tvQuizQuestionLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndClose();
            return;
        }

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileInfo = findViewById(R.id.tvProfileInfo);
        MaterialButton btnWordPool = findViewById(R.id.btnWordPool);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        MaterialButton btnThemeLight = findViewById(R.id.btnThemeLight);
        MaterialButton btnThemeDark = findViewById(R.id.btnThemeDark);
        MaterialButton btnDecreaseQuizLimit = findViewById(R.id.btnDecreaseQuizLimit);
        MaterialButton btnIncreaseQuizLimit = findViewById(R.id.btnIncreaseQuizLimit);
        tvThemeMode = findViewById(R.id.tvThemeMode);
        tvQuizQuestionLimit = findViewById(R.id.tvQuizQuestionLimit);

        NavigationHelper.bindTopBar(this, false);
        NavigationHelper.bindBottomBar(this);
        bindThemeSettings(btnThemeLight, btnThemeDark);
        bindQuizLimitSettings(btnDecreaseQuizLimit, btnIncreaseQuizLimit);
        btnWordPool.setOnClickListener(v -> startActivity(new Intent(this, WordPoolActivity.class)));
        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            openLoginAndClose();
        });

        loadUserInfo(sessionManager.getUserId());
    }

    private void loadUserInfo(int userId) {
        AppExecutors.io().execute(() -> {
            UserEntity user = AppContainer.from(this).authRepository.findUserById(userId);
            int wordCount = AppDatabase.getInstance(this).wordDao().countByUser(userId);
            runOnUiThread(() -> showUser(user, wordCount));
        });
    }

    private void bindThemeSettings(MaterialButton btnThemeLight, MaterialButton btnThemeDark) {
        showThemeMode();
        btnThemeLight.setOnClickListener(v -> changeTheme(ThemeManager.THEME_LIGHT));
        btnThemeDark.setOnClickListener(v -> changeTheme(ThemeManager.THEME_DARK));
    }

    private void changeTheme(int themeMode) {
        if (ThemeManager.getSavedTheme(this) == themeMode) {
            showThemeMode();
            return;
        }
        ThemeManager.saveAndApplyTheme(this, themeMode);
    }

    private void showThemeMode() {
        boolean darkTheme = ThemeManager.getSavedTheme(this) == ThemeManager.THEME_DARK;
        tvThemeMode.setText(darkTheme ? R.string.theme_mode_dark : R.string.theme_mode_light);
    }

    private void bindQuizLimitSettings(MaterialButton btnDecreaseQuizLimit, MaterialButton btnIncreaseQuizLimit) {
        QuizSettingsManager settingsManager = new QuizSettingsManager(this);
        showQuizQuestionLimit(settingsManager.getQuestionLimit());
        btnDecreaseQuizLimit.setOnClickListener(v -> showQuizQuestionLimit(settingsManager.decreaseQuestionLimit()));
        btnIncreaseQuizLimit.setOnClickListener(v -> showQuizQuestionLimit(settingsManager.increaseQuestionLimit()));
    }

    private void showQuizQuestionLimit(int questionLimit) {
        tvQuizQuestionLimit.setText(String.valueOf(questionLimit));
    }

    private void showUser(UserEntity user, int wordCount) {
        if (user == null) {
            tvProfileName.setText(R.string.profile_not_found);
            return;
        }
        tvProfileName.setText(user.username);

        String date = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr")).format(new Date(user.createdAt));
        String info = "Kayıt tarihi: " + date + "\nKelime havuzu: " + wordCount + " kelime";
        tvProfileInfo.setText(info);
    }

    private void openLoginAndClose() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
