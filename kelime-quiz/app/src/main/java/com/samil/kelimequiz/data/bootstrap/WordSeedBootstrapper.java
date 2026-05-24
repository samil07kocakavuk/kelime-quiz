package com.samil.kelimequiz.data.bootstrap;

import android.content.Context;
import android.content.SharedPreferences;

import com.samil.kelimequiz.data.repository.WordRepository;
import com.samil.kelimequiz.domain.model.WordLevel;
import com.samil.kelimequiz.util.ImageStorage;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WordSeedBootstrapper {
    private static final String PREF_NAME = "word_seed_bootstrap";
    private static final String KEY_SEED_VERSION_PREFIX = "seed_version_";
    private static final int CURRENT_SEED_VERSION = 4;

    private final Context context;
    private final WordRepository wordRepository;

    public WordSeedBootstrapper(Context context, WordRepository wordRepository) {
        this.context = context.getApplicationContext();
        this.wordRepository = wordRepository;
    }

    public int ensureSeedWords(int userId) {
        if (isSeedAlreadyImported(userId)) {
            return 0;
        }

        ImageStorage.clearSeedImageCache(context);
        int importedCount = 0;
        for (SeedWord seedWord : loadSeedWords()) {
            String resolvedPicturePath = resolvePicturePath(seedWord.picturePath);
            wordRepository.syncSeedWord(
                    userId,
                    seedWord.engWord,
                    seedWord.trWord,
                    resolvedPicturePath,
                    seedWord.samplesText,
                    seedWord.category,
                    seedWord.cefrLevel
            );
            importedCount++;
        }
        markSeedImported(userId);
        return importedCount;
    }

    private boolean isSeedAlreadyImported(int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(seedVersionKey(userId), 0) >= CURRENT_SEED_VERSION;
    }

    private void markSeedImported(int userId) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(seedVersionKey(userId), CURRENT_SEED_VERSION)
                .apply();
    }

    private String seedVersionKey(int userId) {
        return KEY_SEED_VERSION_PREFIX + userId;
    }

    private List<SeedWord> loadSeedWords() {
        try (InputStream inputStream = context.getAssets().open("seed_words_100.json");
             Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
            String json = scanner.hasNext() ? scanner.next() : "[]";
            return parseSeedWords(json);
        } catch (IOException | JSONException e) {
            throw new IllegalStateException("Hazir kelime havuzu yuklenemedi.", e);
        }
    }

    private String resolvePicturePath(String picturePath) {
        String copiedPath = ImageStorage.copySeedAssetToAppStorage(context, picturePath);
        return copiedPath != null ? copiedPath : picturePath;
    }

    private List<SeedWord> parseSeedWords(String json) throws JSONException {
        JSONArray items = new JSONArray(json);
        List<SeedWord> words = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String category = item.optString("category", "Günlük Yaşam");
            words.add(new SeedWord(
                    item.optString("engWord", ""),
                    item.optString("trWord", ""),
                    item.optString("picturePath", null),
                    item.optString("samplesText", ""),
                    category,
                    item.optString("cefrLevel", WordLevel.fromCategory(category).name())
            ));
        }
        return words;
    }

    private static final class SeedWord {
        private final String engWord;
        private final String trWord;
        private final String picturePath;
        private final String samplesText;
        private final String category;
        private final String cefrLevel;

        private SeedWord(String engWord, String trWord, String picturePath, String samplesText, String category, String cefrLevel) {
            this.engWord = engWord;
            this.trWord = trWord;
            this.picturePath = picturePath;
            this.samplesText = samplesText;
            this.category = category;
            this.cefrLevel = cefrLevel;
        }
    }
}
