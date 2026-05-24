package com.samil.kelimequiz.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.domain.model.QuizAnswerResult;
import com.samil.kelimequiz.domain.model.QuizQuestion;
import com.samil.kelimequiz.ui.auth.LoginActivity;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;
import com.samil.kelimequiz.util.QuizSettingsManager;
import com.samil.kelimequiz.util.SessionManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {
    private static final int MAX_HINTS = 3;

    private static class QuestionResult {
        final String word;
        final int level;
        final boolean correct;
        final String correctAnswer;

        QuestionResult(String word, int level, boolean correct, String correctAnswer) {
            this.word = word;
            this.level = level;
            this.correct = correct;
            this.correctAnswer = correctAnswer;
        }
    }

    private final List<MaterialButton> optionButtons = new ArrayList<>();
    private final List<QuizQuestion> questions = new ArrayList<>();
    private final List<QuestionResult> quizResults = new ArrayList<>();

    private TextView tvQuizProgress;
    private TextView tvQuestionWord;
    private TextView tvQuizFeedback;
    private TextView tvLevelLabel;
    private TextView tvWordLevelLabel;
    private ImageView ivQuizWordImage;
    private LinearProgressIndicator lpiWordLevel;
    private MaterialButton btnNextQuestion;
    private MaterialButton btnFinishQuiz;
    private MaterialButton btnHint;

    private int userId;
    private int currentIndex;
    private int correctCount;
    private int hintsRemaining;
    private boolean answered;
    private String lastSelectedAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndClose();
            return;
        }
        userId = sessionManager.getUserId();

        bindViews();
        NavigationHelper.bindTopBar(this);
        loadQuiz();
    }

    private void bindViews() {
        tvQuizProgress = findViewById(R.id.tvQuizProgress);
        tvQuestionWord = findViewById(R.id.tvQuestionWord);
        tvQuizFeedback = findViewById(R.id.tvQuizFeedback);
        tvLevelLabel = findViewById(R.id.tvLevelLabel);
        tvWordLevelLabel = findViewById(R.id.tvWordLevelLabel);
        ivQuizWordImage = findViewById(R.id.ivQuizWordImage);
        lpiWordLevel = findViewById(R.id.lpiWordLevel);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnFinishQuiz = findViewById(R.id.btnFinishQuiz);
        btnHint = findViewById(R.id.btnHint);

        optionButtons.add(findViewById(R.id.btnOptionOne));
        optionButtons.add(findViewById(R.id.btnOptionTwo));
        optionButtons.add(findViewById(R.id.btnOptionThree));
        optionButtons.add(findViewById(R.id.btnOptionFour));

        btnNextQuestion.setOnClickListener(v -> showNextQuestion());
        btnFinishQuiz.setOnClickListener(v -> {
            if (quizResults.isEmpty()) {
                finish();
            } else {
                showQuizReport();
            }
        });
        btnHint.setOnClickListener(v -> useHint());
    }

    private void loadQuiz() {
        int questionLimit = new QuizSettingsManager(this).getQuestionLimit();
        tvQuizFeedback.setText(R.string.quiz_loading);
        AppExecutors.io().execute(() -> {
            List<QuizQuestion> loadedQuestions = AppContainer.from(this).quizRepository.startQuiz(userId, questionLimit);
            runOnUiThread(() -> showQuiz(loadedQuestions));
        });
    }

    private void showQuiz(List<QuizQuestion> loadedQuestions) {
        questions.clear();
        questions.addAll(loadedQuestions);
        currentIndex = 0;
        correctCount = 0;
        hintsRemaining = MAX_HINTS;

        if (questions.isEmpty()) {
            showEmptyQuiz();
            return;
        }
        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        answered = false;
        lastSelectedAnswer = null;
        QuizQuestion question = questions.get(currentIndex);
        tvQuizProgress.setText(getString(R.string.quiz_progress_format, currentIndex + 1, questions.size()));
        tvQuestionWord.setText(question.getQuestionText());

        if (tvWordLevelLabel != null) {
            String levelText = "A" + (question.getLevel() + 1); // Varsayılan olarak 0-5 arası seviyeyi A1-A6 gibi gösterelim veya direkt rakam
            // Kullanıcının istediği format: "A1 seviyesi ingilizce kelime"
            tvWordLevelLabel.setText(getString(R.string.word_level_label_format, levelText));
        }

        // Resim her zaman gizli başlar
        ivQuizWordImage.setVisibility(View.GONE);

        // İpucu butonu: resmi olan sorularda ve hak varsa göster
        boolean hasImage = question.getPicturePath() != null && !question.getPicturePath().trim().isEmpty();
        if (hasImage && hintsRemaining > 0) {
            btnHint.setVisibility(View.VISIBLE);
            btnHint.setText(getString(R.string.hint_action, hintsRemaining));
        } else {
            btnHint.setVisibility(View.GONE);
        }

        updateLevelStatus(question.getLevel());
        tvQuizFeedback.setText(R.string.choose_turkish_answer);
        tvQuizFeedback.setBackgroundResource(R.drawable.bg_feedback_neutral);
        tvQuizFeedback.setTextColor(getColor(R.color.text_secondary));
        btnNextQuestion.setVisibility(View.GONE);
        btnFinishQuiz.setVisibility(View.GONE);

        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.size(); i++) {
            MaterialButton button = optionButtons.get(i);
            if (i >= options.size()) {
                button.setVisibility(View.GONE);
                continue;
            }
            button.setVisibility(View.VISIBLE);
            button.setEnabled(true);
            button.setText(options.get(i));
            resetOptionStyle(button);
            button.setOnClickListener(v -> submitAnswer(question, ((MaterialButton) v).getText().toString()));
        }
    }

    private void useHint() {
        if (hintsRemaining <= 0) {
            Toast.makeText(this, R.string.hint_no_remaining, Toast.LENGTH_SHORT).show();
            return;
        }

        hintsRemaining--;
        btnHint.setText(getString(R.string.hint_action, hintsRemaining));
        if (hintsRemaining <= 0) {
            btnHint.setEnabled(false);
        }

        // Resmi göster
        QuizQuestion question = questions.get(currentIndex);
        showQuestionImage(question.getPicturePath());
    }

    private void updateLevelStatus(int level) {
        if (tvLevelLabel != null) {
            tvLevelLabel.setText(getString(R.string.level_format_label, level, 6));
        }
        if (lpiWordLevel != null) {
            int progress = (level * 100) / 6;
            lpiWordLevel.setProgressCompat(progress, true);
        }
    }

    private void submitAnswer(QuizQuestion question, String selectedAnswer) {
        if (answered) return;
        answered = true;
        lastSelectedAnswer = selectedAnswer.trim();
        tvQuizFeedback.setText(R.string.saving_answer);
        tvQuizFeedback.setBackgroundResource(R.drawable.bg_feedback_neutral);
        AppExecutors.io().execute(() -> {
            try {
                QuizAnswerResult result = AppContainer.from(this).quizRepository.answerQuestion(
                        userId, question.getWordId(), selectedAnswer);
                runOnUiThread(() -> {
                    updateLevelStatus(result.getLevel());
                    showAnswerResult(result);
                });
            } catch (RuntimeException exception) {
                runOnUiThread(this::showAnswerError);
            }
        });
    }

    private void showAnswerResult(QuizAnswerResult result) {
        QuizQuestion currentQuestion = questions.get(currentIndex);
        quizResults.add(new QuestionResult(
                currentQuestion.getQuestionText(),
                currentQuestion.getLevel(),
                result.isCorrect(),
                currentQuestion.getCorrectAnswer()
        ));

        if (result.isCorrect()) correctCount++;
        applyAnswerStyles(result.isCorrect(), currentQuestion.getCorrectAnswer());
        tvQuizFeedback.setText(buildResultMessage(result));
        tvQuizFeedback.setBackgroundResource(result.isCorrect()
                ? R.drawable.bg_feedback_success : R.drawable.bg_feedback_error);
        tvQuizFeedback.setTextColor(getColor(result.isCorrect() ? R.color.success : R.color.error));

        if (currentIndex == questions.size() - 1) {
            btnFinishQuiz.setVisibility(View.VISIBLE);
            btnFinishQuiz.setText(getString(R.string.finish_quiz_score, correctCount, questions.size()));
        } else {
            btnNextQuestion.setVisibility(View.VISIBLE);
        }
    }

    private void showQuizReport() {
        View view = getLayoutInflater().inflate(R.layout.dialog_quiz_report, null);
        TextView tvReportTitle = view.findViewById(R.id.tvReportTitle);
        TextView tvReportSubtitle = view.findViewById(R.id.tvReportSubtitle);
        TextView tvCorrectCount = view.findViewById(R.id.tvCorrectCount);
        TextView tvWrongCount = view.findViewById(R.id.tvWrongCount);
        TextView tvSuccessRate = view.findViewById(R.id.tvSuccessRate);
        TextView tvMistakeReport = view.findViewById(R.id.tvMistakeReport);
        MaterialButton btnDone = view.findViewById(R.id.btnDone);

        int totalQuestions = questions.size();
        int wrongCount = totalQuestions - correctCount;
        int successPercent = (int) (((float) correctCount / totalQuestions) * 100);

        // Save result to DB
        AppExecutors.io().execute(() -> {
            AppContainer.from(this).quizRepository.saveQuizResult(userId, totalQuestions, correctCount);
        });

        tvCorrectCount.setText(String.valueOf(correctCount));
        tvWrongCount.setText(String.valueOf(wrongCount));
        tvSuccessRate.setText(getString(R.string.quiz_success_rate_format, successPercent));

        // Başarı oranına göre mesaj ve başlık
        if (successPercent == 100) {
            tvReportTitle.setText(R.string.quiz_result_title_perfect);
            tvReportSubtitle.setText(R.string.quiz_result_subtitle_perfect);
            tvReportTitle.setTextColor(getColor(R.color.success));
        } else if (successPercent >= 70) {
            tvReportTitle.setText(R.string.quiz_result_title_great);
            tvReportSubtitle.setText(R.string.quiz_result_subtitle_great);
        } else if (successPercent >= 40) {
            tvReportTitle.setText(R.string.quiz_result_title_good);
            tvReportSubtitle.setText(R.string.quiz_result_subtitle_good);
        } else {
            tvReportTitle.setText(R.string.quiz_result_title_keep_going);
            tvReportSubtitle.setText(R.string.quiz_result_subtitle_keep_going);
        }

        // Yanlış raporu - Sadece yanlış kelimeler ve doğruları
        StringBuilder mistakeReport = new StringBuilder();
        int actualMistakes = 0;
        for (QuestionResult res : quizResults) {
            if (!res.correct) {
                actualMistakes++;
                mistakeReport.append("✖ '").append(res.word)
                        .append("' kelimesinin doğru karşılığı '")
                        .append(res.correctAnswer).append("'\n\n");
            }
        }
        
        if (actualMistakes == 0) {
            tvMistakeReport.setVisibility(View.GONE);
        } else {
            tvMistakeReport.setText(mistakeReport.toString().trim());
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnDone.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private String buildResultMessage(QuizAnswerResult result) {
        if (!result.isCorrect()) return getString(R.string.wrong_answer_feedback);
        if (result.isLearned()) return getString(R.string.learned_answer_feedback);
        
        Locale trLocale = new Locale("tr", "TR");
        String formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, trLocale).format(result.getNextReviewAt());
        
        return getString(R.string.correct_answer_feedback, result.getLevel(), formattedDate);
    }

    private void showAnswerError() {
        answered = false;
        tvQuizFeedback.setText(R.string.answer_save_error);
        tvQuizFeedback.setBackgroundResource(R.drawable.bg_feedback_error);
        tvQuizFeedback.setTextColor(getColor(R.color.error));
    }

    private void showQuestionImage(String picturePath) {
        if (picturePath == null || picturePath.trim().isEmpty()) {
            ivQuizWordImage.setVisibility(View.GONE);
            return;
        }
        ivQuizWordImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(picturePath)
                .thumbnail(0.25f)
                .fitCenter()
                .dontAnimate()
                .into(ivQuizWordImage);
    }

    private void applyAnswerStyles(boolean correct, String correctAnswer) {
        for (MaterialButton button : optionButtons) {
            String option = button.getText().toString();
            if (option.equals(correctAnswer)) {
                applyOptionStyle(button, R.drawable.bg_quiz_option_success, R.color.quiz_option_feedback_text);
            } else if (!correct && option.equals(lastSelectedAnswer)) {
                applyOptionStyle(button, R.drawable.bg_quiz_option_error, R.color.quiz_option_feedback_text);
            }
        }
    }

    private void resetOptionStyle(MaterialButton button) {
        button.setBackgroundTintList(null);
        button.setBackgroundResource(R.drawable.bg_quiz_option);
        button.setTextColor(getColor(R.color.quiz_option_text));
    }

    private void applyOptionStyle(MaterialButton button, int backgroundDrawableRes, int textColorRes) {
        button.setBackgroundTintList(null);
        button.setBackgroundResource(backgroundDrawableRes);
        button.setTextColor(getColor(textColorRes));
    }

    private void showNextQuestion() {
        currentIndex++;
        showCurrentQuestion();
    }

    private void showEmptyQuiz() {
        tvQuizProgress.setText(R.string.empty_quiz_progress);
        tvQuestionWord.setText(R.string.empty_quiz_title);
        ivQuizWordImage.setVisibility(View.GONE);
        btnHint.setVisibility(View.GONE);
        tvQuizFeedback.setText(R.string.empty_quiz_message);
        setOptionsVisible(false);
        btnNextQuestion.setVisibility(View.GONE);
        btnFinishQuiz.setVisibility(View.VISIBLE);
    }

    private void setOptionsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        for (MaterialButton button : optionButtons) {
            button.setVisibility(visibility);
        }
    }

    private void openLoginAndClose() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
