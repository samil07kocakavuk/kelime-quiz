package com.samil.kelimequiz.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.data.local.entity.WordWithLevel;

import java.util.List;

@Dao
public interface WordDao {
    @Insert
    long insert(WordEntity word);

    @Update
    void update(WordEntity word);

    @Delete
    void delete(WordEntity word);

    @Query("SELECT words.wordId, words.userId, words.engWord, words.trWord, words.picturePath, words.category, words.createdAt, " +
            "words.cefrLevel, COALESCE(quiz_progress.level, 0) AS level FROM words " +
            "LEFT JOIN quiz_progress ON words.wordId = quiz_progress.wordId AND quiz_progress.userId = :userId " +
            "WHERE words.userId = :userId ORDER BY words.createdAt DESC")
    List<WordWithLevel> listByUser(int userId);

    @Query("SELECT COUNT(*) FROM words WHERE userId = :userId")
    int countByUser(int userId);

    @Query("SELECT * FROM words WHERE userId = :userId AND wordId = :wordId LIMIT 1")
    WordEntity findByUserAndId(int userId, int wordId);

    @Query("SELECT * FROM words WHERE userId = :userId AND LOWER(engWord) = LOWER(:engWord) LIMIT 1")
    WordEntity findByUserAndEnglishWord(int userId, String engWord);

    @Query("SELECT trWord FROM words WHERE userId = :userId AND wordId != :excludedWordId ORDER BY RANDOM() LIMIT :limit")
    List<String> listRandomTranslationsExcluding(int userId, int excludedWordId, int limit);

    @Query("SELECT trWord FROM words WHERE userId = :userId AND wordId != :excludedWordId AND category IS NOT NULL AND category != :excludedCategory ORDER BY RANDOM() LIMIT :limit")
    List<String> listRandomTranslationsExcludingCategory(int userId, int excludedWordId, String excludedCategory, int limit);

    @Query("SELECT DISTINCT category FROM words WHERE userId = :userId AND category IS NOT NULL ORDER BY category")
    List<String> listCategories(int userId);

    @Query("SELECT COUNT(*) FROM words WHERE userId = :userId AND category = :category")
    int countByCategory(int userId, String category);

    @Query("SELECT engWord FROM words WHERE userId = :userId AND LENGTH(engWord) BETWEEN :minLen AND :maxLen ORDER BY RANDOM() LIMIT 1")
    String getRandomWordForWordle(int userId, int minLen, int maxLen);

    @Query("SELECT words.engWord FROM words " +
            "INNER JOIN quiz_progress ON words.wordId = quiz_progress.wordId AND quiz_progress.userId = :userId " +
            "WHERE words.userId = :userId " +
            "AND LENGTH(words.engWord) BETWEEN :minLen AND :maxLen " +
            "AND quiz_progress.level >= :minLevel " +
            "ORDER BY RANDOM() LIMIT 1")
    String getRandomStartedWordForWordle(int userId, int minLen, int maxLen, int minLevel);

    @Query("SELECT * FROM words WHERE userId = :userId ORDER BY RANDOM() LIMIT 1")
    WordEntity getRandomWord(int userId);

    @Query("SELECT * FROM words WHERE userId = :userId ORDER BY RANDOM()")
    List<WordEntity> listByUserSimple(int userId);

    @Query("SELECT words.* FROM words " +
            "INNER JOIN quiz_progress ON words.wordId = quiz_progress.wordId AND quiz_progress.userId = :userId " +
            "WHERE words.userId = :userId " +
            "AND quiz_progress.level >= :minLevel " +
            "ORDER BY RANDOM()")
    List<WordEntity> listStartedWords(int userId, int minLevel);
}
