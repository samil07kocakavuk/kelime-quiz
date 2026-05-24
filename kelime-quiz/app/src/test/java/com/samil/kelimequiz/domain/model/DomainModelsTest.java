package com.samil.kelimequiz.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class DomainModelsTest {
    @Test
    public void authResultExposesSuccessAndFailureData() {
        AuthResult success = AuthResult.success(7, "OK");
        AuthResult failure = AuthResult.fail("Nope");

        assertTrue(success.isSuccess());
        assertEquals(7, success.getUserId());
        assertEquals("OK", success.getMessage());

        assertFalse(failure.isSuccess());
        assertEquals(-1, failure.getUserId());
        assertEquals("Nope", failure.getMessage());
    }

    @Test
    public void quizSummaryAndAnswerResultExposeTheirValues() {
        QuizSummary summary = new QuizSummary(12, 5, 7);
        QuizAnswerResult answer = new QuizAnswerResult(true, 3, false, 123L);

        assertEquals(12, summary.getTotalWords());
        assertEquals(5, summary.getActiveWords());
        assertEquals(7, summary.getLearnedWords());

        assertTrue(answer.isCorrect());
        assertEquals(3, answer.getLevel());
        assertFalse(answer.isLearned());
        assertEquals(123L, answer.getNextReviewAt());
    }

    @Test
    public void categoryReportReturnsZeroWhenEmpty() {
        CategoryReport empty = new CategoryReport("Meslekler", 0, 0, 0.0);
        CategoryReport filled = new CategoryReport("Meslekler", 8, 6, 2.5);

        assertEquals("Meslekler", empty.getCategory());
        assertEquals(0, empty.getSuccessPercent());
        assertEquals(75, filled.getSuccessPercent());
    }

    @Test
    public void wordDetailsExposesStoredValues() {
        WordDetails details = new WordDetails(1, "apple", "elma", null, "Yiyecekler", "A1", Arrays.asList("a", "b"));

        assertEquals(1, details.getWordId());
        assertEquals("apple", details.getEngWord());
        assertEquals("elma", details.getTrWord());
        assertEquals("Yiyecekler", details.getCategory());
        assertEquals("A1", details.getCefrLevel());
        assertEquals(Arrays.asList("a", "b"), details.getSampleTexts());
    }
}
