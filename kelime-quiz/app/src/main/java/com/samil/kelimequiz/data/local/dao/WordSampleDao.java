package com.samil.kelimequiz.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.samil.kelimequiz.data.local.entity.WordSampleEntity;

import java.util.List;

@Dao
public interface WordSampleDao {
    @Insert
    void insertAll(List<WordSampleEntity> samples);

    @Query("SELECT * FROM word_samples WHERE wordId = :wordId ORDER BY wordSampleId ASC")
    List<WordSampleEntity> listByWordId(int wordId);
}
