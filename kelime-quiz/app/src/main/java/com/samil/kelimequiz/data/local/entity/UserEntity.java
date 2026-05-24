package com.samil.kelimequiz.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"username"}, unique = true)})
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int userId;

    public String username;
    public String passwordHash;
    public String passwordSalt;
    public int passwordIterations;
    public long createdAt;
    public int currentStreak;
    public long lastLoginDate;
}
