package com.samil.kelimequiz.domain.service;

import java.util.Calendar;

public class SrsScheduler {
    public static final int MAX_LEVEL = 6;

    public long calculateNextReviewAt(int level, long answeredAt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(answeredAt);
        switch (level) {
            case 1:
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case 2:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case 3:
                calendar.add(Calendar.MONTH, 1);
                break;
            case 4:
                calendar.add(Calendar.MONTH, 3);
                break;
            case 5:
                calendar.add(Calendar.MONTH, 6);
                break;
            default:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
        return calendar.getTimeInMillis();
    }

    public boolean isLearned(int level) {
        return level >= MAX_LEVEL;
    }
}
