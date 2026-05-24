package com.samil.kelimequiz.domain.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

public class SrsSchedulerTest {
    private final SrsScheduler scheduler = new SrsScheduler();

    @Test
    public void calculateNextReviewAtUsesExpectedIntervals() {
        Calendar base = Calendar.getInstance();
        base.set(2026, Calendar.MAY, 9, 10, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        assertEquals(add(base, Calendar.DAY_OF_YEAR, 1), scheduler.calculateNextReviewAt(1, base.getTimeInMillis()));
        assertEquals(add(base, Calendar.WEEK_OF_YEAR, 1), scheduler.calculateNextReviewAt(2, base.getTimeInMillis()));
        assertEquals(add(base, Calendar.MONTH, 1), scheduler.calculateNextReviewAt(3, base.getTimeInMillis()));
        assertEquals(add(base, Calendar.MONTH, 3), scheduler.calculateNextReviewAt(4, base.getTimeInMillis()));
        assertEquals(add(base, Calendar.MONTH, 6), scheduler.calculateNextReviewAt(5, base.getTimeInMillis()));
        assertEquals(add(base, Calendar.YEAR, 1), scheduler.calculateNextReviewAt(6, base.getTimeInMillis()));
    }

    @Test
    public void isLearnedReturnsTrueForFinalLevelOnly() {
        assertFalse(scheduler.isLearned(5));
        assertTrue(scheduler.isLearned(6));
    }

    private long add(Calendar base, int field, int amount) {
        Calendar clone = (Calendar) base.clone();
        clone.add(field, amount);
        return clone.getTimeInMillis();
    }
}
