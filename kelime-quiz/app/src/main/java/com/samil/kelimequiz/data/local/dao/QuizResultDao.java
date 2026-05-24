package com.samil.kelimequiz.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.samil.kelimequiz.data.local.entity.QuizResultEntity;

import java.util.List;

@Dao
public interface QuizResultDao {
    @Insert
    void insert(QuizResultEntity result);

    @Query("SELECT AVG(successRate) FROM quiz_results WHERE userId = :userId")
    Double getAverageSuccessRate(int userId);

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    List<QuizResultEntity> listByUser(int userId);

    @Query("SELECT * FROM quiz_results WHERE userId = :userId AND completedAt >= :since ORDER BY completedAt ASC")
    List<QuizResultEntity> getRecentResults(int userId, long since);
}
