package com.samil.kelimequiz.domain.service;

import static org.junit.Assert.assertTrue;

import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.data.local.entity.WordWithLevel;

import java.util.Arrays;

import org.junit.Test;

public class WordReportHtmlBuilderTest {
    private final WordReportHtmlBuilder builder = new WordReportHtmlBuilder();

    @Test
    public void buildCreatesCategoryAndStatusSummary() {
        String html = builder.build(Arrays.asList(
                word(1, "apple", "elma", "Yiyecekler", "A1", 6),
                word(2, "doctor", "doktor", "Meslekler", "B1", 3),
                word(3, "river", "nehir", null, null, 0),
                word(4, "quick", "hızlı", "Sıfatlar", "B1", 4)
        ));

        assertTrue(html.contains("Toplam: 4"));
        assertTrue(html.contains("Öğrenilmiş: 1"));
        assertTrue(html.contains("Devam Eden: 2"));
        assertTrue(html.contains("Başlanmamış: 1"));
        assertTrue(html.contains("A1: 2"));
        assertTrue(html.contains("B1: 2"));
        assertTrue(html.contains("Öğrenilmiş Kelimeler"));
        assertTrue(html.contains("Öğrenilmekte Olan"));
        assertTrue(html.contains("Başlanmamış"));
        assertTrue(html.contains("quick"));
    }

    private WordWithLevel word(int id, String eng, String tr, String category, String cefrLevel, int level) {
        WordEntity entity = new WordEntity();
        entity.wordId = id;
        entity.engWord = eng;
        entity.trWord = tr;
        entity.category = category;
        entity.cefrLevel = cefrLevel;

        WordWithLevel item = new WordWithLevel();
        item.word = entity;
        item.level = level;
        return item;
    }
}
