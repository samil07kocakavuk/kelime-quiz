package com.samil.kelimequiz.domain.model;

public class QuizSummary {
    private final int totalWords;
    private final int activeWords;
    private final int learnedWords;

    public QuizSummary(int totalWords, int activeWords, int learnedWords) {
        this.totalWords = totalWords;
        this.activeWords = activeWords;
        this.learnedWords = learnedWords;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getActiveWords() {
        return activeWords;
    }

    public int getLearnedWords() {
        return learnedWords;
    }
}
