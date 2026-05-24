package com.samil.kelimequiz.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class WordWithLevel {
    @Embedded
    public WordEntity word;

    @ColumnInfo(name = "level")
    public int level;
}
