package com.samil.kelimequiz.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "words",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("userId"),
                @Index(value = {"userId", "engWord"}, unique = true)
        }
)
public class WordEntity {
    @PrimaryKey(autoGenerate = true)
    public int wordId;

    public int userId;
    public String engWord;
    public String trWord;
    public String picturePath;
    public String category;
    public String cefrLevel;
    public long createdAt;
}
