package com.samil.kelimequiz.domain.model;

public class DayProgress {
    public String day;
    public double avgSuccess;

    public DayProgress(String day, double avgSuccess) {
        this.day = day;
        this.avgSuccess = avgSuccess;
    }
}
