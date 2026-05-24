package com.samil.kelimequiz.domain.model;

public class QuizAnswerResult {
    private final boolean correct;
    private final int level;
    private final boolean learned;
    private final long nextReviewAt;

    public QuizAnswerResult(boolean correct, int level, boolean learned, long nextReviewAt) {
        this.correct = correct;
        this.level = level;
        this.learned = learned;
        this.nextReviewAt = nextReviewAt;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getLevel() {
        return level;
    }

    public boolean isLearned() {
        return learned;
    }

    public long getNextReviewAt() {
        return nextReviewAt;
    }
}
