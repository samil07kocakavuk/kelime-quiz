package com.samil.kelimequiz.util.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordHasherTest {
    @Test
    public void verifyReturnsTrueForMatchingPassword() {
        PasswordHasher hasher = new PasswordHasher();
        HashResult hashResult = hasher.hash("123456");

        boolean valid = hasher.verify(
                "123456",
                hashResult.getHashBase64(),
                hashResult.getSaltBase64(),
                hashResult.getIterations()
        );

        assertTrue(valid);
    }

    @Test
    public void verifyReturnsFalseForDifferentPassword() {
        PasswordHasher hasher = new PasswordHasher();
        HashResult hashResult = hasher.hash("123456");

        boolean valid = hasher.verify(
                "abcdef",
                hashResult.getHashBase64(),
                hashResult.getSaltBase64(),
                hashResult.getIterations()
        );

        assertFalse(valid);
    }
}
