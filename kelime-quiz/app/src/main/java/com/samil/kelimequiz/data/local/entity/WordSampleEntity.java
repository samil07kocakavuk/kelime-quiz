package com.samil.kelimequiz.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "word_samples",
        foreignKeys = @ForeignKey(
                entity = WordEntity.class,
                parentColumns = "wordId",
                childColumns = "wordId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("wordId")}
)
public class WordSampleEntity {
    @PrimaryKey(autoGenerate = true)
    public int wordSampleId;

    public int wordId;
    public String sampleText;
}
