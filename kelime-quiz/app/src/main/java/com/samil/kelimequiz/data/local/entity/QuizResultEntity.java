package com.samil.kelimequiz.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "quiz_results",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class QuizResultEntity {
    @PrimaryKey(autoGenerate = true)
    public int resultId;
    public int userId;
    public int totalQuestions;
    public int correctAnswers;
    public double successRate; // (correctAnswers / totalQuestions) * 100
    public long completedAt;
}
