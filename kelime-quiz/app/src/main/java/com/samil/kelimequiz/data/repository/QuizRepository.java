package com.samil.kelimequiz.data.repository;

import com.samil.kelimequiz.data.local.dao.QuizProgressDao;
import com.samil.kelimequiz.data.local.dao.QuizResultDao;
import com.samil.kelimequiz.data.local.dao.WordDao;
import com.samil.kelimequiz.data.local.entity.QuizProgressEntity;
import com.samil.kelimequiz.data.local.entity.QuizResultEntity;
import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.domain.model.DayProgress;
import com.samil.kelimequiz.domain.model.QuizAnswerResult;
import com.samil.kelimequiz.domain.model.QuizQuestion;
import com.samil.kelimequiz.domain.model.QuizSummary;
import com.samil.kelimequiz.domain.service.SrsScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

public class QuizRepository {
    private static final int OPTION_COUNT = 4;

    private final WordDao wordDao;
    private final QuizProgressDao quizProgressDao;
    private final QuizResultDao quizResultDao;
    private final SrsScheduler srsScheduler;

    public QuizRepository(WordDao wordDao, QuizProgressDao quizProgressDao, QuizResultDao quizResultDao) {
        this.wordDao = wordDao;
        this.quizProgressDao = quizProgressDao;
        this.quizResultDao = quizResultDao;
        this.srsScheduler = new SrsScheduler();
    }

    public List<QuizQuestion> startQuiz(int userId, int questionLimit) {
        List<WordEntity> selectedWords = selectQuizWords(userId, questionLimit);
        List<QuizQuestion> questions = new ArrayList<>();
        for (WordEntity word : selectedWords) {
            questions.add(toQuestion(userId, word));
        }
        return questions;
    }

    private List<WordEntity> selectQuizWords(int userId, int questionLimit) {
        long now = System.currentTimeMillis();
        List<WordEntity> selectedWords = new ArrayList<>(quizProgressDao.listDueWords(userId, now, questionLimit));
        int remainingLimit = questionLimit - selectedWords.size();
        if (remainingLimit > 0) {
            selectedWords.addAll(quizProgressDao.listNewWords(userId, remainingLimit));
        }
        return selectedWords;
    }

    public QuizAnswerResult answerQuestion(int userId, int wordId, String selectedAnswer) {
        long now = System.currentTimeMillis();
        WordEntity word = wordDao.findByUserAndId(userId, wordId);
        if (word == null) {
            throw new IllegalArgumentException("Kelime bulunamadı.");
        }

        QuizProgressEntity progress = findOrCreateProgress(userId, wordId);
        boolean correct = word.trWord.trim().equalsIgnoreCase(selectedAnswer.trim());
        updateProgressAfterAnswer(progress, correct, now);
        saveProgress(progress);
        return new QuizAnswerResult(correct, progress.level, progress.learned, progress.nextReviewAt);
    }

    public QuizSummary getSummary(int userId) {
        return new QuizSummary(
                wordDao.countByUser(userId),
                quizProgressDao.countActiveWords(userId),
                quizProgressDao.countLearnedWords(userId)
        );
    }

    public double getGlobalAverageLevel(int userId) {
        Double avg = quizProgressDao.getGlobalAverageLevel(userId);
        return avg != null ? avg : 0.0;
    }

    public void saveQuizResult(int userId, int total, int correct) {
        QuizResultEntity result = new QuizResultEntity();
        result.userId = userId;
        result.totalQuestions = total;
        result.correctAnswers = correct;
        result.successRate = total > 0 ? (correct * 100.0) / total : 0;
        result.completedAt = System.currentTimeMillis();
        quizResultDao.insert(result);
    }

    public double getAverageSuccessRate(int userId) {
        Double avg = quizResultDao.getAverageSuccessRate(userId);
        return avg != null ? avg : 0.0;
    }

    public int countLevelOneWords(int userId) {
        return quizProgressDao.countLevelOneWords(userId);
    }

    public List<DayProgress> getWeeklyProgress(int userId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long since = cal.getTimeInMillis();

        List<QuizResultEntity> results = quizResultDao.getRecentResults(userId, since);
        Map<String, List<Double>> grouped = new HashMap<>();

        for (QuizResultEntity res : results) {
            Calendar resCal = Calendar.getInstance();
            resCal.setTimeInMillis(res.completedAt);
            String dayKey = String.format(Locale.US, "%02d/%02d", resCal.get(Calendar.DAY_OF_MONTH), resCal.get(Calendar.MONTH) + 1);
            if (!grouped.containsKey(dayKey)) grouped.put(dayKey, new ArrayList<>());
            grouped.get(dayKey).add(res.successRate);
        }

        List<DayProgress> weekly = new ArrayList<>();
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(since);

        for (int i = 0; i < 7; i++) {
            String dayKey = String.format(Locale.US, "%02d/%02d", current.get(Calendar.DAY_OF_MONTH), current.get(Calendar.MONTH) + 1);
            double avg = 0;
            if (grouped.containsKey(dayKey)) {
                List<Double> rates = grouped.get(dayKey);
                double sum = 0;
                for (double r : rates) sum += r;
                avg = sum / rates.size();
            }
            // Kısa gün adı ekle (Örn: Pzt, Sal...)
            String dayName = getDayName(current.get(Calendar.DAY_OF_WEEK));
            weekly.add(new DayProgress(dayName, avg));
            current.add(Calendar.DAY_OF_YEAR, 1);
        }
        return weekly;
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Pzt";
            case Calendar.TUESDAY: return "Sal";
            case Calendar.WEDNESDAY: return "Çar";
            case Calendar.THURSDAY: return "Per";
            case Calendar.FRIDAY: return "Cum";
            case Calendar.SATURDAY: return "Cmt";
            case Calendar.SUNDAY: return "Paz";
            default: return "";
        }
    }

    private QuizQuestion toQuestion(int userId, WordEntity word) {
        Set<String> uniqueOptions = new LinkedHashSet<>();
        uniqueOptions.add(word.trWord);
        String excludedCategory = word.category;
        uniqueOptions.addAll(wordDao.listRandomTranslationsExcludingCategory(
                userId,
                word.wordId,
                excludedCategory,
                OPTION_COUNT - 1
        ));

        if (uniqueOptions.size() < OPTION_COUNT) {
            uniqueOptions.addAll(wordDao.listRandomTranslationsExcluding(
                    userId,
                    word.wordId,
                    OPTION_COUNT - uniqueOptions.size()
            ));
        }

        List<String> options = new ArrayList<>(uniqueOptions);
        Collections.shuffle(options);

        QuizProgressEntity progress = quizProgressDao.findByUserAndWord(userId, word.wordId);
        int level = (progress != null) ? progress.level : 0;

        return new QuizQuestion(word.wordId, word.engWord, word.trWord, word.picturePath, options, level);
    }

    private void updateProgressAfterAnswer(QuizProgressEntity progress, boolean correct, long now) {
        if (correct) {
            progress.level = Math.min(progress.level + 1, SrsScheduler.MAX_LEVEL);
            progress.learned = srsScheduler.isLearned(progress.level);
            progress.nextReviewAt = progress.learned ? 0 : srsScheduler.calculateNextReviewAt(progress.level, now);
        } else {
            progress.level = 0;
            progress.learned = false;
            progress.nextReviewAt = now;
        }
        progress.updatedAt = now;
    }

    private QuizProgressEntity findOrCreateProgress(int userId, int wordId) {
        QuizProgressEntity progress = quizProgressDao.findByUserAndWord(userId, wordId);
        if (progress != null) {
            return progress;
        }

        QuizProgressEntity newProgress = new QuizProgressEntity();
        newProgress.userId = userId;
        newProgress.wordId = wordId;
        newProgress.level = 0;
        newProgress.nextReviewAt = 0;
        newProgress.learned = false;
        newProgress.updatedAt = System.currentTimeMillis();
        return newProgress;
    }

    private void saveProgress(QuizProgressEntity progress) {
        if (progress.progressId == 0) {
            progress.progressId = (int) quizProgressDao.insert(progress);
        } else {
            quizProgressDao.update(progress);
        }
    }

}
