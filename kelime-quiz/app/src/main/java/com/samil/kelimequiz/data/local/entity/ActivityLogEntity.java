package com.samil.kelimequiz.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "activity_logs",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("userId"),
                @Index("type"),
                @Index("createdAt")
        }
)
public class ActivityLogEntity {
    public static final String TYPE_QUIZ_COMPLETED = "quiz_completed";
    public static final String TYPE_NEW_LEVEL_ONE = "new_level_one";
    public static final String TYPE_WORDLE_COMPLETED = "wordle_completed";
    public static final String TYPE_WORDLE_WON = "wordle_won";
    public static final String TYPE_AI_STORY = "ai_story";

    @PrimaryKey(autoGenerate = true)
    public int activityLogId;

    public int userId;
    public String type;
    public long createdAt;
}
