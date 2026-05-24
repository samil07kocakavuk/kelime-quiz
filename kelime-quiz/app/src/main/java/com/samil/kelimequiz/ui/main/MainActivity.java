package com.samil.kelimequiz.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.entity.UserEntity;
import com.samil.kelimequiz.domain.model.QuizSummary;
import com.samil.kelimequiz.ui.auth.LoginActivity;
import com.samil.kelimequiz.ui.quiz.QuizActivity;
import com.samil.kelimequiz.ui.report.WeeklyReportActivity;
import com.samil.kelimequiz.ui.wordchain.WordChainActivity;
import com.samil.kelimequiz.ui.wordle.WordleActivity;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;
import com.samil.kelimequiz.util.SessionManager;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MaterialButton btnStartQuiz;
    private TextView tvUsernameMain;
    private TextView tvAverageLevelValue;
    private TextView tvGreeting;
    private TextView tvStreakValue;
    private TextView tvSuccessRateValue;
    private TextView tvWordCountValue;
    private TextView tvDailyWord;
    private TextView tvDailyWordMeaning;
    private TextView tvDailyWordSample;
    private int userId;
    private int totalWordCount;
    private boolean wordCountLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndClose();
            return;
        }

        userId = sessionManager.getUserId();

        // Stale session kontrolü (DB reset sonrası)
        AppExecutors.io().execute(() -> {
            AppContainer container = AppContainer.from(this);
            UserEntity user = container.authRepository.findUserById(userId);
            if (user == null) {
                runOnUiThread(() -> {
                    sessionManager.clear();
                    openLoginAndClose();
                });
                return;
            }

            runOnUiThread(() -> {
                NavigationHelper.bindBottomBar(this);
                btnStartQuiz = findViewById(R.id.btnStartQuiz);
                tvUsernameMain = findViewById(R.id.tvUsernameMain);
                tvAverageLevelValue = findViewById(R.id.tvAverageLevelValue);
                tvGreeting = findViewById(R.id.tvGreeting);
                tvStreakValue = findViewById(R.id.tvStreakValue);
                tvSuccessRateValue = findViewById(R.id.tvSuccessRateValue);
                tvWordCountValue = findViewById(R.id.tvWordCountValue);
                tvDailyWord = findViewById(R.id.tvDailyWord);
                tvDailyWordMeaning = findViewById(R.id.tvDailyWordMeaning);
                tvDailyWordSample = findViewById(R.id.tvDailyWordSample);
                
                tvUsernameMain.setText(user.username);
                updateGreeting();
                
                btnStartQuiz.setEnabled(false);
                btnStartQuiz.setOnClickListener(v -> openQuizIfWordsExist());

                MaterialButton btnDailyWordle = findViewById(R.id.btnDailyWordle);
                btnDailyWordle.setOnClickListener(v -> startActivity(new Intent(this, WordleActivity.class)));

                MaterialButton btnWordChain = findViewById(R.id.btnWordChain);
                btnWordChain.setOnClickListener(v -> startActivity(new Intent(this, WordChainActivity.class)));

                MaterialButton btnWeeklyReport = findViewById(R.id.btnWeeklyReport);
                btnWeeklyReport.setOnClickListener(v -> startActivity(new Intent(this, WeeklyReportActivity.class)));

                loadUserData();
            });
        });
    }

    private void updateGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) greeting = "Günaydın,";
        else if (hour >= 12 && hour < 18) greeting = "Tünaydın,";
        else if (hour >= 18 && hour < 22) greeting = "İyi Akşamlar,";
        else greeting = "İyi Geceler,";
        
        if (tvGreeting != null) tvGreeting.setText(greeting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnStartQuiz != null) {
            loadUserData();
            updateGreeting();
        }
    }

    private void loadUserData() {
        wordCountLoaded = false;
        btnStartQuiz.setEnabled(false);
        AppExecutors.io().execute(() -> {
            AppContainer container = AppContainer.from(this);
            UserEntity user = container.authRepository.findUserById(userId);
            if (user != null) {
                container.authRepository.checkAndUpdateStreak(user);
                // Güncellenmiş veriyi tekrar çek
                user = container.authRepository.findUserById(userId);
            }
            
            QuizSummary summary = container.quizRepository.getSummary(userId);
            double avgLevel = container.quizRepository.getGlobalAverageLevel(userId);
            int levelOneCount = container.quizRepository.countLevelOneWords(userId);
            double averageSuccessRate = container.quizRepository.getAverageSuccessRate(userId);
            
            final UserEntity finalUser = user;
            
            runOnUiThread(() -> {
                showWordCount(summary);
                updateAverageLevel(avgLevel);
                loadDailyWord();
                if (tvStreakValue != null && finalUser != null) {
                    tvStreakValue.setText(getString(R.string.day_count_format, finalUser.currentStreak));
                }
                if (tvSuccessRateValue != null) tvSuccessRateValue.setText(String.format(Locale.US, "%%%d", (int)averageSuccessRate));
                if (tvWordCountValue != null) tvWordCountValue.setText(String.valueOf(levelOneCount));
            });
        });
    }

    private void loadDailyWord() {
        AppExecutors.io().execute(() -> {
            android.content.SharedPreferences prefs = getSharedPreferences("daily_word_prefs", android.content.Context.MODE_PRIVATE);
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
            String keyWord = "word_" + today + "_" + userId;
            String keyMeaning = "meaning_" + today + "_" + userId;
            String keySample = "sample_" + today + "_" + userId;

            String savedWord = prefs.getString(keyWord, null);
            String savedMeaning = prefs.getString(keyMeaning, null);
            String savedSample = prefs.getString(keySample, null);

            if (savedWord != null && savedMeaning != null) {
                // Kelime var ama cümle bilgisi hiç yoksa (null ise), veritabanından çekip güncelle
                if (savedSample == null) {
                    com.samil.kelimequiz.data.local.AppDatabase db = com.samil.kelimequiz.data.local.AppDatabase.getInstance(this);
                    com.samil.kelimequiz.data.local.entity.WordEntity word = db.wordDao().findByUserAndEnglishWord(userId, savedWord);
                    if (word != null) {
                        List<com.samil.kelimequiz.data.local.entity.WordSampleEntity> samples = db.wordSampleDao().listByWordId(word.wordId);
                        String sampleText = (samples != null && !samples.isEmpty()) ? samples.get(0).sampleText : "";
                        prefs.edit().putString(keySample, sampleText).apply();
                        savedSample = sampleText;
                    } else {
                        savedSample = ""; // Kelime bulunamadıysa boş bırak
                    }
                }

                final String finalSample = savedSample;
                runOnUiThread(() -> {
                    if (tvDailyWord != null) tvDailyWord.setText(savedWord);
                    if (tvDailyWordMeaning != null) tvDailyWordMeaning.setText(savedMeaning);
                    if (tvDailyWordSample != null) {
                        if (finalSample != null && !finalSample.isEmpty()) {
                            tvDailyWordSample.setText(finalSample);
                            tvDailyWordSample.setVisibility(View.VISIBLE);
                        } else {
                            tvDailyWordSample.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                com.samil.kelimequiz.data.local.AppDatabase db = com.samil.kelimequiz.data.local.AppDatabase.getInstance(this);
                com.samil.kelimequiz.data.local.entity.WordEntity word = db.wordDao().getRandomWord(userId);
                if (word != null) {
                    List<com.samil.kelimequiz.data.local.entity.WordSampleEntity> samples = db.wordSampleDao().listByWordId(word.wordId);
                    String sampleText = (samples != null && !samples.isEmpty()) ? samples.get(0).sampleText : "";

                    prefs.edit()
                            .putString(keyWord, word.engWord)
                            .putString(keyMeaning, word.trWord)
                            .putString(keySample, sampleText)
                            .apply();
                            
                    runOnUiThread(() -> {
                        if (tvDailyWord != null) tvDailyWord.setText(word.engWord);
                        if (tvDailyWordMeaning != null) tvDailyWordMeaning.setText(word.trWord);
                        if (tvDailyWordSample != null) {
                            if (!sampleText.isEmpty()) {
                                tvDailyWordSample.setText(sampleText);
                                tvDailyWordSample.setVisibility(View.VISIBLE);
                            } else {
                                tvDailyWordSample.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateAverageLevel(double avgLevel) {
        if (tvAverageLevelValue != null) {
            tvAverageLevelValue.setText(String.format(Locale.US, "%.1f", avgLevel));
        }
    }

    private void showWordCount(QuizSummary summary) {
        wordCountLoaded = true;
        totalWordCount = summary.getTotalWords();
        btnStartQuiz.setText(totalWordCount == 0 ? R.string.add_word_first_action : R.string.start_quiz_action);
        btnStartQuiz.setEnabled(true);
    }

    private void openQuizIfWordsExist() {
        if (!wordCountLoaded) {
            Toast.makeText(this, R.string.quiz_summary_wait, Toast.LENGTH_SHORT).show();
            return;
        }
        if (totalWordCount == 0) {
            Toast.makeText(this, R.string.quiz_requires_words, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, QuizActivity.class));
    }

    private void openLoginAndClose() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
