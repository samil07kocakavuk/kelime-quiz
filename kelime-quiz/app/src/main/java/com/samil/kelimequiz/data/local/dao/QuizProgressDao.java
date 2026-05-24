package com.samil.kelimequiz.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.samil.kelimequiz.data.local.entity.QuizProgressEntity;
import com.samil.kelimequiz.data.local.entity.WordEntity;

import java.util.List;

@Dao
public interface QuizProgressDao {
    @Insert
    long insert(QuizProgressEntity progress);

    @Update
    void update(QuizProgressEntity progress);

    @Query("SELECT * FROM quiz_progress WHERE userId = :userId AND wordId = :wordId LIMIT 1")
    QuizProgressEntity findByUserAndWord(int userId, int wordId);

    @Query("SELECT words.* FROM words INNER JOIN quiz_progress ON words.wordId = quiz_progress.wordId "
            + "WHERE quiz_progress.userId = :userId "
            + "AND quiz_progress.learned = 0 "
            + "AND quiz_progress.nextReviewAt <= :now "
            + "ORDER BY quiz_progress.nextReviewAt ASC "
            + "LIMIT :limit")
    List<WordEntity> listDueWords(int userId, long now, int limit);

    @Query("SELECT * FROM words WHERE userId = :userId "
            + "AND wordId NOT IN (SELECT wordId FROM quiz_progress WHERE userId = :userId) "
            + "ORDER BY RANDOM() "
            + "LIMIT :limit")
    List<WordEntity> listNewWords(int userId, int limit);

    @Query("SELECT COUNT(*) FROM quiz_progress WHERE userId = :userId AND learned = 1")
    int countLearnedWords(int userId);

    @Query("SELECT COUNT(*) FROM quiz_progress WHERE userId = :userId AND learned = 0")
    int countActiveWords(int userId);

    @Query("SELECT COUNT(*) FROM quiz_progress qp "
            + "INNER JOIN words w ON qp.wordId = w.wordId "
            + "WHERE qp.userId = :userId AND qp.level > 0 AND w.category = :category")
    int countCorrectByCategory(int userId, String category);

    @Query("SELECT AVG(qp.level) FROM quiz_progress qp "
            + "INNER JOIN words w ON qp.wordId = w.wordId "
            + "WHERE qp.userId = :userId AND w.category = :category")
    Double getAverageLevelByCategory(int userId, String category);

    @Query("SELECT AVG(level) FROM quiz_progress WHERE userId = :userId")
    Double getGlobalAverageLevel(int userId);

    @Query("SELECT COUNT(*) FROM quiz_progress WHERE userId = :userId AND level = 1")
    int countLevelOneWords(int userId);
}
