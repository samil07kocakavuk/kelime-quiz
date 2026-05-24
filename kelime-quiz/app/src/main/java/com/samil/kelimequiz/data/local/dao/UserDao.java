package com.samil.kelimequiz.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.samil.kelimequiz.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity findByUsername(String username);

    @Query("UPDATE users SET passwordHash = :hash, passwordSalt = :salt, passwordIterations = :iterations WHERE userId = :userId")
    void updatePassword(int userId, String hash, String salt, int iterations);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity findById(int userId);

    @Query("UPDATE users SET currentStreak = :streak, lastLoginDate = :lastLogin WHERE userId = :userId")
    void updateStreak(int userId, int streak, long lastLogin);
}
