package com.samil.kelimequiz.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.samil.kelimequiz.data.local.entity.ActivityLogEntity;

import java.util.List;

@Dao
public interface ActivityLogDao {
    @Insert
    long insert(ActivityLogEntity log);

    @Query("SELECT * FROM activity_logs WHERE userId = :userId AND createdAt >= :startAt AND createdAt < :endAt ORDER BY createdAt ASC")
    List<ActivityLogEntity> listByUserAndRange(int userId, long startAt, long endAt);

    @Query("SELECT COUNT(*) FROM activity_logs WHERE userId = :userId AND type = :type AND createdAt >= :startAt AND createdAt < :endAt")
    int countByTypeAndRange(int userId, String type, long startAt, long endAt);
}
