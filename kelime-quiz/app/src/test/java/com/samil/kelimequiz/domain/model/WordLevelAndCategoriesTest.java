package com.samil.kelimequiz.domain.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WordLevelAndCategoriesTest {
    @Test
    public void normalizeReturnsKnownLevelAndFallsBackToA1() {
        assertEquals(WordLevel.C2.name(), WordLevel.normalize(" c2 "));
        assertEquals(WordLevel.A1.name(), WordLevel.normalize("unknown"));
    }

    @Test
    public void fromCategoryUsesSharedCategoryMapping() {
        assertEquals(WordLevel.A2, WordLevel.fromCategory("Meslekler"));
        assertEquals(WordLevel.B1, WordCategories.defaultLevelFor("Sporlar"));
    }
}
