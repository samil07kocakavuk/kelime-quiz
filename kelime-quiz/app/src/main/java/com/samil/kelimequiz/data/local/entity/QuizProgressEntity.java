package com.samil.kelimequiz.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "quiz_progress",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = WordEntity.class,
                        parentColumns = "wordId",
                        childColumns = "wordId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("userId"),
                @Index("wordId"),
                @Index(value = {"userId", "wordId"}, unique = true)
        }
)
public class QuizProgressEntity {
    @PrimaryKey(autoGenerate = true)
    public int progressId;

    public int userId;
    public int wordId;
    public int level;
    public long nextReviewAt;
    public boolean learned;
    public long updatedAt;
}
