package com.samil.kelimequiz.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.samil.kelimequiz.data.local.entity.UserEntity;
import com.samil.kelimequiz.domain.model.AuthResult;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryUserDao;
import com.samil.kelimequiz.testsupport.TestDoubles.StubPasswordHasher;
import com.samil.kelimequiz.util.security.HashResult;

import org.junit.Test;

public class AuthRepositoryTest {
    @Test
    public void registerStoresNormalizedUserAndLoginUsesHasher() {
        InMemoryUserDao userDao = new InMemoryUserDao();
        StubPasswordHasher hasher = new StubPasswordHasher(new HashResult("hash", "salt", 99), true);
        AuthRepository repository = new AuthRepository(userDao, hasher);

        AuthResult registerResult = repository.register("  Samil  ", "123456");
        assertTrue(registerResult.isSuccess());
        assertEquals("Kayıt başarılı.", registerResult.getMessage());

        UserEntity stored = userDao.findByUsername("samil");
        assertTrue(stored != null);
        assertEquals("hash", stored.passwordHash);
        assertEquals("salt", stored.passwordSalt);
        assertEquals(99, stored.passwordIterations);

        AuthResult loginResult = repository.login("samil", "123456");
        assertTrue(loginResult.isSuccess());
        assertEquals(stored.userId, loginResult.getUserId());
    }

    @Test
    public void registerRejectsDuplicateUsernames() {
        InMemoryUserDao userDao = new InMemoryUserDao();
        StubPasswordHasher hasher = new StubPasswordHasher(new HashResult("hash", "salt", 99), true);
        AuthRepository repository = new AuthRepository(userDao, hasher);

        repository.register("samil", "123456");
        AuthResult duplicate = repository.register(" SAMIL ", "123456");

        assertFalse(duplicate.isSuccess());
        assertEquals("Bu kullanıcı adı zaten kayıtlı.", duplicate.getMessage());
    }

    @Test
    public void resetPasswordAndFindUserByIdReturnStoredUser() {
        InMemoryUserDao userDao = new InMemoryUserDao();
        StubPasswordHasher hasher = new StubPasswordHasher(new HashResult("hash", "salt", 99), true);
        AuthRepository repository = new AuthRepository(userDao, hasher);

        AuthResult registerResult = repository.register("samil", "123456");
        AuthResult resetResult = repository.resetPassword("samil", "abcdef");

        assertTrue(resetResult.isSuccess());
        assertEquals(registerResult.getUserId(), resetResult.getUserId());
        assertEquals("hash", userDao.findById(registerResult.getUserId()).passwordHash);
        assertEquals(registerResult.getUserId(), repository.findUserById(registerResult.getUserId()).userId);
    }
}
