package com.samil.kelimequiz.testsupport;

import com.samil.kelimequiz.data.local.dao.QuizProgressDao;
import com.samil.kelimequiz.data.local.dao.UserDao;
import com.samil.kelimequiz.data.local.dao.WordDao;
import com.samil.kelimequiz.data.local.dao.WordSampleDao;
import com.samil.kelimequiz.data.local.dao.QuizResultDao;
import com.samil.kelimequiz.data.local.entity.QuizProgressEntity;
import com.samil.kelimequiz.data.local.entity.QuizResultEntity;
import com.samil.kelimequiz.data.local.entity.UserEntity;
import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.data.local.entity.WordSampleEntity;
import com.samil.kelimequiz.data.local.entity.WordWithLevel;
import com.samil.kelimequiz.util.security.HashResult;
import com.samil.kelimequiz.util.security.PasswordHasher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TestDoubles {
    private TestDoubles() {
    }

    public static final class InMemoryUserDao implements UserDao {
        private final Map<Integer, UserEntity> byId = new HashMap<>();
        private final Map<String, UserEntity> byUsername = new HashMap<>();
        private int nextId = 1;

        @Override
        public long insert(UserEntity user) {
            user.userId = nextId++;
            byId.put(user.userId, copy(user));
            byUsername.put(normalize(user.username), copy(user));
            return user.userId;
        }

        @Override
        public UserEntity findByUsername(String username) {
            UserEntity user = byUsername.get(normalize(username));
            return user == null ? null : copy(user);
        }

        @Override
        public void updatePassword(int userId, String hash, String salt, int iterations) {
            UserEntity user = byId.get(userId);
            if (user != null) {
                user.passwordHash = hash;
                user.passwordSalt = salt;
                user.passwordIterations = iterations;
                byUsername.put(normalize(user.username), copy(user));
            }
        }

        @Override
        public UserEntity findById(int userId) {
            UserEntity user = byId.get(userId);
            return user == null ? null : copy(user);
        }

        @Override
        public void updateStreak(int userId, int streak, long lastLogin) {
            UserEntity user = byId.get(userId);
            if (user != null) {
                user.currentStreak = streak;
                user.lastLoginDate = lastLogin;
                byUsername.put(normalize(user.username), copy(user));
            }
        }

        public void put(UserEntity user) {
            byId.put(user.userId, copy(user));
            byUsername.put(normalize(user.username), copy(user));
        }

        private static String normalize(String username) {
            return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        }

        private static UserEntity copy(UserEntity source) {
            UserEntity copy = new UserEntity();
            copy.userId = source.userId;
            copy.username = source.username;
            copy.passwordHash = source.passwordHash;
            copy.passwordSalt = source.passwordSalt;
            copy.passwordIterations = source.passwordIterations;
            copy.createdAt = source.createdAt;
            copy.currentStreak = source.currentStreak;
            copy.lastLoginDate = source.lastLoginDate;
            return copy;
        }
    }

    public static final class InMemoryWordDao implements WordDao {
        private final Map<Integer, WordEntity> words = new HashMap<>();
        private final Map<Integer, QuizProgressEntity> progressByWordId = new HashMap<>();
        private int nextId = 1;

        @Override
        public long insert(WordEntity word) {
            if (word.wordId == 0) {
                word.wordId = nextId++;
            }
            words.put(word.wordId, copy(word));
            return word.wordId;
        }

        @Override
        public void update(WordEntity word) {
            words.put(word.wordId, copy(word));
        }

        @Override
        public void delete(WordEntity word) {
            words.remove(word.wordId);
        }

        @Override
        public List<WordWithLevel> listByUser(int userId) {
            List<WordWithLevel> result = new ArrayList<>();
            for (WordEntity word : words.values()) {
                if (word.userId == userId) {
                    WordWithLevel item = new WordWithLevel();
                    item.word = copy(word);
                    item.level = 0;
                    result.add(item);
                }
            }
            return result;
        }

        @Override
        public int countByUser(int userId) {
            int count = 0;
            for (WordEntity word : words.values()) {
                if (word.userId == userId) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public WordEntity findByUserAndId(int userId, int wordId) {
            WordEntity word = words.get(wordId);
            return word != null && word.userId == userId ? copy(word) : null;
        }

        @Override
        public WordEntity findByUserAndEnglishWord(int userId, String engWord) {
            for (WordEntity word : words.values()) {
                if (word.userId == userId && word.engWord != null && word.engWord.equalsIgnoreCase(engWord)) {
                    return copy(word);
                }
            }
            return null;
        }

        @Override
        public List<String> listRandomTranslationsExcluding(int userId, int excludedWordId, int limit) {
            List<String> translations = new ArrayList<>();
            for (WordEntity word : words.values()) {
                if (word.userId == userId && word.wordId != excludedWordId) {
                    translations.add(word.trWord);
                }
            }
            return new ArrayList<>(translations.subList(0, Math.min(limit, translations.size())));
        }

        @Override
        public List<String> listRandomTranslationsExcludingCategory(int userId, int excludedWordId, String excludedCategory, int limit) {
            List<String> translations = new ArrayList<>();
            for (WordEntity word : words.values()) {
                if (word.userId == userId
                        && word.wordId != excludedWordId
                        && word.category != null
                        && !word.category.equals(excludedCategory)) {
                    translations.add(word.trWord);
                }
            }
            return new ArrayList<>(translations.subList(0, Math.min(limit, translations.size())));
        }

        @Override
        public List<String> listCategories(int userId) {
            return Collections.emptyList();
        }

        @Override
        public int countByCategory(int userId, String category) {
            int count = 0;
            for (WordEntity word : words.values()) {
                if (word.userId == userId && category != null && category.equals(word.category)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public String getRandomWordForWordle(int userId, int minLen, int maxLen) {
            for (WordEntity word : words.values()) {
                if (word.userId == userId && word.engWord != null && word.engWord.length() >= minLen && word.engWord.length() <= maxLen) {
                    return word.engWord;
                }
            }
            return null;
        }

        @Override
        public String getRandomStartedWordForWordle(int userId, int minLen, int maxLen, int minLevel) {
            for (WordEntity word : words.values()) {
                QuizProgressEntity progress = progressByWordId.get(word.wordId);
                if (word.userId == userId
                        && word.engWord != null
                        && word.engWord.length() >= minLen
                        && word.engWord.length() <= maxLen
                        && progress != null
                        && progress.userId == userId
                        && progress.level >= minLevel) {
                    return word.engWord;
                }
            }
            return null;
        }

        @Override
        public WordEntity getRandomWord(int userId) {
            for (WordEntity word : words.values()) {
                if (word.userId == userId) {
                    return copy(word);
                }
            }
            return null;
        }

        @Override
        public List<WordEntity> listByUserSimple(int userId) {
            List<WordEntity> result = new ArrayList<>();
            for (WordEntity word : words.values()) {
                if (word.userId == userId) {
                    result.add(copy(word));
                }
            }
            return result;
        }

        @Override
        public List<WordEntity> listStartedWords(int userId, int minLevel) {
            List<WordEntity> result = new ArrayList<>();
            for (WordEntity word : words.values()) {
                QuizProgressEntity progress = progressByWordId.get(word.wordId);
                if (word.userId == userId
                        && progress != null
                        && progress.userId == userId
                        && progress.level >= minLevel) {
                    result.add(copy(word));
                }
            }
            return result;
        }

        public void putProgress(QuizProgressEntity progress) {
            progressByWordId.put(progress.wordId, InMemoryQuizProgressDao.copy(progress));
        }

        public void put(WordEntity word) {
            words.put(word.wordId, copy(word));
        }

        public List<WordEntity> all() {
            List<WordEntity> result = new ArrayList<>();
            for (WordEntity word : words.values()) {
                result.add(copy(word));
            }
            return result;
        }

        private static WordEntity copy(WordEntity source) {
            WordEntity copy = new WordEntity();
            copy.wordId = source.wordId;
            copy.userId = source.userId;
            copy.engWord = source.engWord;
            copy.trWord = source.trWord;
            copy.picturePath = source.picturePath;
            copy.category = source.category;
            copy.cefrLevel = source.cefrLevel;
            copy.createdAt = source.createdAt;
            return copy;
        }
    }

    public static final class InMemoryWordSampleDao implements WordSampleDao {
        private final Map<Integer, List<WordSampleEntity>> samples = new HashMap<>();

        @Override
        public void insertAll(List<WordSampleEntity> samples) {
            for (WordSampleEntity sample : samples) {
                this.samples.computeIfAbsent(sample.wordId, key -> new ArrayList<>()).add(copy(sample));
            }
        }

        @Override
        public List<WordSampleEntity> listByWordId(int wordId) {
            List<WordSampleEntity> list = samples.get(wordId);
            if (list == null) {
                return new ArrayList<>();
            }
            List<WordSampleEntity> result = new ArrayList<>();
            for (WordSampleEntity sample : list) {
                result.add(copy(sample));
            }
            return result;
        }

        private static WordSampleEntity copy(WordSampleEntity source) {
            WordSampleEntity copy = new WordSampleEntity();
            copy.wordSampleId = source.wordSampleId;
            copy.wordId = source.wordId;
            copy.sampleText = source.sampleText;
            return copy;
        }
    }

    public static final class InMemoryQuizProgressDao implements QuizProgressDao {
        private final Map<Integer, QuizProgressEntity> progressByWordId = new HashMap<>();
        private final Map<Integer, WordEntity> wordsById = new HashMap<>();
        private int nextId = 1;

        public void putWord(WordEntity word) {
            wordsById.put(word.wordId, copy(word));
        }

        public void putProgress(QuizProgressEntity progress) {
            progressByWordId.put(progress.wordId, copy(progress));
        }

        @Override
        public long insert(QuizProgressEntity progress) {
            if (progress.progressId == 0) {
                progress.progressId = nextId++;
            }
            progressByWordId.put(progress.wordId, copy(progress));
            return progress.progressId;
        }

        @Override
        public void update(QuizProgressEntity progress) {
            progressByWordId.put(progress.wordId, copy(progress));
        }

        @Override
        public QuizProgressEntity findByUserAndWord(int userId, int wordId) {
            QuizProgressEntity progress = progressByWordId.get(wordId);
            return progress != null && progress.userId == userId ? copy(progress) : null;
        }

        @Override
        public List<WordEntity> listDueWords(int userId, long now, int limit) {
            List<WordEntity> result = new ArrayList<>();
            for (QuizProgressEntity progress : progressByWordId.values()) {
                if (progress.userId == userId && !progress.learned && progress.nextReviewAt <= now) {
                    WordEntity word = wordsById.get(progress.wordId);
                    if (word != null) {
                        result.add(copy(word));
                    }
                }
            }
            return new ArrayList<>(result.subList(0, Math.min(limit, result.size())));
        }

        @Override
        public List<WordEntity> listNewWords(int userId, int limit) {
            List<WordEntity> result = new ArrayList<>();
            for (WordEntity word : wordsById.values()) {
                QuizProgressEntity progress = progressByWordId.get(word.wordId);
                if (word.userId == userId && progress == null) {
                    result.add(copy(word));
                }
            }
            return new ArrayList<>(result.subList(0, Math.min(limit, result.size())));
        }

        @Override
        public int countLearnedWords(int userId) {
            int count = 0;
            for (QuizProgressEntity progress : progressByWordId.values()) {
                if (progress.userId == userId && progress.learned) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public int countActiveWords(int userId) {
            int count = 0;
            for (QuizProgressEntity progress : progressByWordId.values()) {
                if (progress.userId == userId && !progress.learned) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public int countCorrectByCategory(int userId, String category) {
            if (category == null) {
                return 0;
            }
            int count = 0;
            for (QuizProgressEntity progress : progressByWordId.values()) {
                WordEntity word = wordsById.get(progress.wordId);
                if (progress.userId == userId && progress.level > 0 && word != null && category.equals(word.category)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public Double getAverageLevelByCategory(int userId, String category) {
            if (category == null) {
                return null;
            }
            int count = 0;
            int total = 0;
            for (QuizProgressEntity progress : progressByWordId.values()) {
                WordEntity word = wordsById.get(progress.wordId);
                if (progress.userId == userId && word != null && category.equals(word.category)) {
                    total += progress.level;
                    count++;
                }
            }
            return count == 0 ? null : (double) total / count;
        }

        @Override
        public Double getGlobalAverageLevel(int userId) {
            int count = 0;
            int total = 0;
            for (QuizProgressEntity progress : progressByWordId.values()) {
                if (progress.userId == userId) {
                    total += progress.level;
                    count++;
                }
            }
            return count == 0 ? null : (double) total / count;
        }

        @Override
        public int countLevelOneWords(int userId) {
            int count = 0;
            for (QuizProgressEntity progress : progressByWordId.values()) {
                if (progress.userId == userId && progress.level == 1) {
                    count++;
                }
            }
            return count;
        }

        private static QuizProgressEntity copy(QuizProgressEntity source) {
            QuizProgressEntity copy = new QuizProgressEntity();
            copy.progressId = source.progressId;
            copy.userId = source.userId;
            copy.wordId = source.wordId;
            copy.level = source.level;
            copy.nextReviewAt = source.nextReviewAt;
            copy.learned = source.learned;
            copy.updatedAt = source.updatedAt;
            return copy;
        }

        private static WordEntity copy(WordEntity source) {
            WordEntity copy = new WordEntity();
            copy.wordId = source.wordId;
            copy.userId = source.userId;
            copy.engWord = source.engWord;
            copy.trWord = source.trWord;
            copy.picturePath = source.picturePath;
            copy.category = source.category;
            copy.cefrLevel = source.cefrLevel;
            copy.createdAt = source.createdAt;
            return copy;
        }
    }

    public static final class InMemoryQuizResultDao implements QuizResultDao {
        private final List<QuizResultEntity> results = new ArrayList<>();

        @Override
        public void insert(QuizResultEntity result) {
            results.add(copy(result));
        }

        @Override
        public Double getAverageSuccessRate(int userId) {
            double total = 0;
            int count = 0;
            for (QuizResultEntity res : results) {
                if (res.userId == userId) {
                    total += res.successRate;
                    count++;
                }
            }
            return count == 0 ? null : total / count;
        }

        @Override
        public List<QuizResultEntity> listByUser(int userId) {
            List<QuizResultEntity> userResults = new ArrayList<>();
            for (QuizResultEntity res : results) {
                if (res.userId == userId) {
                    userResults.add(copy(res));
                }
            }
            return userResults;
        }

        @Override
        public List<QuizResultEntity> getRecentResults(int userId, long since) {
            List<QuizResultEntity> userResults = new ArrayList<>();
            for (QuizResultEntity res : results) {
                if (res.userId == userId && res.completedAt >= since) {
                    userResults.add(copy(res));
                }
            }
            return userResults;
        }

        private static QuizResultEntity copy(QuizResultEntity source) {
            QuizResultEntity copy = new QuizResultEntity();
            copy.resultId = source.resultId;
            copy.userId = source.userId;
            copy.totalQuestions = source.totalQuestions;
            copy.correctAnswers = source.correctAnswers;
            copy.successRate = source.successRate;
            copy.completedAt = source.completedAt;
            return copy;
        }
    }

    public static final class StubPasswordHasher extends PasswordHasher {
        private final HashResult hashResult;
        private final boolean verifyResult;

        public StubPasswordHasher(HashResult hashResult, boolean verifyResult) {
            this.hashResult = hashResult;
            this.verifyResult = verifyResult;
        }

        @Override
        public HashResult hash(String rawPassword) {
            return hashResult;
        }

        @Override
        public boolean verify(String rawPassword, String storedHashBase64, String storedSaltBase64, int iterations) {
            return verifyResult;
        }
    }
}
