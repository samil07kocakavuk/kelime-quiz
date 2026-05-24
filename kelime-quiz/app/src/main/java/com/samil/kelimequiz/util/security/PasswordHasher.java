package com.samil.kelimequiz.util.security;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
    public static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int DEFAULT_ITERATIONS = 120_000;

    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_SIZE_BYTES = 16;

    public HashResult hash(String rawPassword) {
        byte[] salt = new byte[SALT_SIZE_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, DEFAULT_ITERATIONS);
        return new HashResult(
                Base64.getEncoder().encodeToString(hash),
                Base64.getEncoder().encodeToString(salt),
                DEFAULT_ITERATIONS
        );
    }

    public boolean verify(String rawPassword, String storedHashBase64, String storedSaltBase64, int iterations) {
        byte[] expectedHash = Base64.getDecoder().decode(storedHashBase64);
        byte[] salt = Base64.getDecoder().decode(storedSaltBase64);
        byte[] candidateHash = pbkdf2(rawPassword.toCharArray(), salt, iterations);
        return constantTimeEquals(expectedHash, candidateHash);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Şifre hash üretilemedi.", e);
        }
    }

    private boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left == null || right == null || left.length != right.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < left.length; i++) {
            result |= left[i] ^ right[i];
        }
        return result == 0;
    }
}
