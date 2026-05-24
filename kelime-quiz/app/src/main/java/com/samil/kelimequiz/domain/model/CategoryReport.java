package com.samil.kelimequiz.domain.model;

public class CategoryReport {
    private final String category;
    private final int totalWords;
    private final int correctWords;
    private final double averageLevel;

    public CategoryReport(String category, int totalWords, int correctWords, double averageLevel) {
        this.category = category;
        this.totalWords = totalWords;
        this.correctWords = correctWords;
        this.averageLevel = averageLevel;
    }

    public String getCategory() {
        return category;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getCorrectWords() {
        return correctWords;
    }

    public double getAverageLevel() {
        return averageLevel;
    }

    public int getSuccessPercent() {
        if (totalWords == 0) {
            return 0;
        }
        return (correctWords * 100) / totalWords;
    }
}
