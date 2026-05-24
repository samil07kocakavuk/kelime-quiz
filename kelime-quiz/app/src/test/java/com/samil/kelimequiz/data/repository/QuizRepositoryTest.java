package com.samil.kelimequiz.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.samil.kelimequiz.data.local.entity.QuizProgressEntity;
import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.domain.model.QuizAnswerResult;
import com.samil.kelimequiz.domain.model.QuizQuestion;
import com.samil.kelimequiz.domain.model.QuizSummary;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryQuizProgressDao;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryQuizResultDao;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryWordDao;

import java.util.List;

import org.junit.Test;

public class QuizRepositoryTest {
    @Test
    public void startQuizPrioritizesDueWordsAndFillsWithNewWords() {
        InMemoryWordDao wordDao = new InMemoryWordDao();
        InMemoryQuizProgressDao progressDao = new InMemoryQuizProgressDao();
        InMemoryQuizResultDao resultDao = new InMemoryQuizResultDao();
        QuizRepository repository = new QuizRepository(wordDao, progressDao, resultDao);

        WordEntity dueWord = word(1, 1, "apple", "elma", "Yiyecekler");
        WordEntity newWord = word(2, 1, "doctor", "doktor", "Meslekler");
        wordDao.put(dueWord);
        wordDao.put(newWord);
        progressDao.putWord(dueWord);
        progressDao.putWord(newWord);

        QuizProgressEntity dueProgress = new QuizProgressEntity();
        dueProgress.userId = 1;
        dueProgress.wordId = 1;
        dueProgress.level = 2;
        dueProgress.nextReviewAt = 0;
        dueProgress.learned = false;
        progressDao.putProgress(dueProgress);

        List<QuizQuestion> questions = repository.startQuiz(1, 2);
        assertEquals(2, questions.size());
        assertEquals(1, questions.get(0).getWordId());
        assertTrue(questions.get(0).getOptions().contains("elma"));
    }

    @Test
    public void answerQuestionAdvancesAndResetsProgress() {
        InMemoryWordDao wordDao = new InMemoryWordDao();
        InMemoryQuizProgressDao progressDao = new InMemoryQuizProgressDao();
        InMemoryQuizResultDao resultDao = new InMemoryQuizResultDao();
        QuizRepository repository = new QuizRepository(wordDao, progressDao, resultDao);

        WordEntity word = word(1, 1, "apple", "elma", "Yiyecekler");
        wordDao.put(word);
        progressDao.putWord(word);

        QuizAnswerResult correct = repository.answerQuestion(1, 1, "elma");
        assertTrue(correct.isCorrect());
        assertEquals(1, correct.getLevel());
        assertFalse(correct.isLearned());
        assertTrue(correct.getNextReviewAt() > 0);

        QuizAnswerResult wrong = repository.answerQuestion(1, 1, "armut");
        assertFalse(wrong.isCorrect());
        assertEquals(0, wrong.getLevel());
    }

    @Test
    public void summaryAndAverageUseStoredProgress() {
        InMemoryWordDao wordDao = new InMemoryWordDao();
        InMemoryQuizProgressDao progressDao = new InMemoryQuizProgressDao();
        InMemoryQuizResultDao resultDao = new InMemoryQuizResultDao();
        QuizRepository repository = new QuizRepository(wordDao, progressDao, resultDao);

        WordEntity first = word(1, 1, "apple", "elma", "Yiyecekler");
        WordEntity second = word(2, 1, "doctor", "doktor", "Meslekler");
        wordDao.put(first);
        wordDao.put(second);
        progressDao.putWord(first);
        progressDao.putWord(second);

        QuizProgressEntity active = progress(1, 1, 1, false);
        QuizProgressEntity learned = progress(1, 2, 6, true);
        progressDao.putProgress(active);
        progressDao.putProgress(learned);

        QuizSummary summary = repository.getSummary(1);
        assertEquals(2, summary.getTotalWords());
        assertEquals(1, summary.getActiveWords());
        assertEquals(1, summary.getLearnedWords());
        assertEquals(3.5, repository.getGlobalAverageLevel(1), 0.0);
    }

    private WordEntity word(int id, int userId, String eng, String tr, String category) {
        WordEntity word = new WordEntity();
        word.wordId = id;
        word.userId = userId;
        word.engWord = eng;
        word.trWord = tr;
        word.category = category;
        word.createdAt = 1L;
        return word;
    }

    private QuizProgressEntity progress(int userId, int wordId, int level, boolean learned) {
        QuizProgressEntity progress = new QuizProgressEntity();
        progress.userId = userId;
        progress.wordId = wordId;
        progress.level = level;
        progress.learned = learned;
        progress.nextReviewAt = 0L;
        progress.updatedAt = 1L;
        return progress;
    }
}
