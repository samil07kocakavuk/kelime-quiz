package com.samil.kelimequiz.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.samil.kelimequiz.domain.model.WordDetails;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryWordDao;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryWordSampleDao;
import com.samil.kelimequiz.domain.model.WordLevel;

import java.util.Arrays;

import org.junit.Test;

public class WordRepositoryTest {
    @Test
    public void addWordPersistsSamplesAndRejectsDuplicates() {
        InMemoryWordDao wordDao = new InMemoryWordDao();
        InMemoryWordSampleDao sampleDao = new InMemoryWordSampleDao();
        WordRepository repository = new WordRepository(wordDao, sampleDao);

        boolean inserted = repository.addWord(1, "Apple", "Elma", "/tmp/a.png", "one\n two ", "Yiyecekler", WordLevel.A1.name());
        assertTrue(inserted);
        assertFalse(repository.addWord(1, "apple", "ElmA", null, null, "Yiyecekler", WordLevel.A1.name()));
        assertEquals(1, wordDao.countByUser(1));
        assertEquals(2, sampleDao.listByWordId(1).size());
    }

    @Test
    public void getWordDetailsReturnsSavedSamples() {
        InMemoryWordDao wordDao = new InMemoryWordDao();
        InMemoryWordSampleDao sampleDao = new InMemoryWordSampleDao();
        WordRepository repository = new WordRepository(wordDao, sampleDao);

        repository.addWord(1, "Doctor", "Doktor", null, "a\nb", "Meslekler", WordLevel.A2.name());
        WordDetails details = repository.getWordDetails(1, 1);

        assertEquals("Doctor", details.getEngWord());
        assertEquals("Doktor", details.getTrWord());
        assertEquals(Arrays.asList("a", "b"), details.getSampleTexts());
    }

    @Test
    public void deleteWordRemovesEntryAndListWordsReturnsRemainingItems() {
        InMemoryWordDao wordDao = new InMemoryWordDao();
        InMemoryWordSampleDao sampleDao = new InMemoryWordSampleDao();
        WordRepository repository = new WordRepository(wordDao, sampleDao);

        repository.addWord(1, "Apple", "Elma", null, null, "Yiyecekler", WordLevel.A1.name());
        repository.addWord(1, "Doctor", "Doktor", null, null, "Meslekler", WordLevel.A2.name());
        repository.deleteWord(1, 1);

        assertEquals(1, repository.listWords(1).size());
        assertEquals("Doctor", repository.listWords(1).get(0).word.engWord);
    }
}
