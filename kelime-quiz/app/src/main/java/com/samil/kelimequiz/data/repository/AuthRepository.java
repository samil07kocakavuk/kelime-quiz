package com.samil.kelimequiz.data.repository;

import com.samil.kelimequiz.data.local.dao.UserDao;
import com.samil.kelimequiz.data.local.entity.UserEntity;
import com.samil.kelimequiz.domain.model.AuthResult;
import com.samil.kelimequiz.util.security.HashResult;
import com.samil.kelimequiz.util.security.PasswordHasher;

import java.util.Calendar;
import java.util.Locale;

public class AuthRepository {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserDao userDao;
    private final PasswordHasher passwordHasher;

    public AuthRepository(UserDao userDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
    }

    public AuthResult register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        AuthResult validation = validateCredentials(normalizedUsername, password);
        if (!validation.isSuccess()) {
            return validation;
        }

        if (userDao.findByUsername(normalizedUsername) != null) {
            return AuthResult.fail("Bu kullanıcı adı zaten kayıtlı.");
        }

        HashResult hashResult = passwordHasher.hash(password);
        UserEntity user = new UserEntity();
        user.username = normalizedUsername;
        user.passwordHash = hashResult.getHashBase64();
        user.passwordSalt = hashResult.getSaltBase64();
        user.passwordIterations = hashResult.getIterations();
        user.createdAt = System.currentTimeMillis();

        int userId = (int) userDao.insert(user);
        return AuthResult.success(userId, "Kayıt başarılı.");
    }

    public AuthResult login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        AuthResult validation = validateCredentials(normalizedUsername, password);
        if (!validation.isSuccess()) {
            return validation;
        }

        UserEntity user = userDao.findByUsername(normalizedUsername);
        if (user == null) {
            return AuthResult.fail("Kullanıcı bulunamadı.");
        }

        boolean valid = passwordHasher.verify(
                password,
                user.passwordHash,
                user.passwordSalt,
                user.passwordIterations
        );
        if (!valid) {
            return AuthResult.fail("Şifre hatalı.");
        }

        checkAndUpdateStreak(user);

        return AuthResult.success(user.userId, "Giriş başarılı.");
    }

    public void checkAndUpdateStreak(UserEntity user) {
        long now = System.currentTimeMillis();
        if (user.lastLoginDate == 0) {
            user.currentStreak = 1;
            user.lastLoginDate = now;
            userDao.updateStreak(user.userId, user.currentStreak, user.lastLoginDate);
            return;
        }

        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(user.lastLoginDate);
        int lastYear = lastCal.get(Calendar.YEAR);
        int lastDay = lastCal.get(Calendar.DAY_OF_YEAR);

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(now);
        int nowYear = nowCal.get(Calendar.YEAR);
        int nowDay = nowCal.get(Calendar.DAY_OF_YEAR);

        // Bugün zaten giriş yapılmış mı?
        if (lastYear == nowYear && lastDay == nowDay) {
            return;
        }

        // Dün mü giriş yapılmış?
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.setTimeInMillis(now);
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
        int yestYear = yesterdayCal.get(Calendar.YEAR);
        int yestDay = yesterdayCal.get(Calendar.DAY_OF_YEAR);

        if (lastYear == yestYear && lastDay == yestDay) {
            user.currentStreak++;
        } else {
            // 2 gün veya daha fazla ara verilmiş, sıfırla (veya 1'den başlat)
            user.currentStreak = 1;
        }

        user.lastLoginDate = now;
        userDao.updateStreak(user.userId, user.currentStreak, user.lastLoginDate);
    }

    public AuthResult resetPassword(String username, String newPassword) {
        String normalizedUsername = normalizeUsername(username);
        AuthResult validation = validateCredentials(normalizedUsername, newPassword);
        if (!validation.isSuccess()) {
            return validation;
        }

        UserEntity user = userDao.findByUsername(normalizedUsername);
        if (user == null) {
            return AuthResult.fail("Bu kullanıcı adı ile kayıt bulunamadı.");
        }

        HashResult hashResult = passwordHasher.hash(newPassword);
        userDao.updatePassword(
                user.userId,
                hashResult.getHashBase64(),
                hashResult.getSaltBase64(),
                hashResult.getIterations()
        );
        return AuthResult.success(user.userId, "Şifre güncellendi.");
    }

    public UserEntity findUserById(int userId) {
        return userDao.findById(userId);
    }

    private AuthResult validateCredentials(String username, String password) {
        if (username.isEmpty()) {
            return AuthResult.fail("Kullanıcı adı boş bırakılamaz.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return AuthResult.fail("Şifre en az 6 karakter olmalıdır.");
        }
        return AuthResult.success(-1, "OK");
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
