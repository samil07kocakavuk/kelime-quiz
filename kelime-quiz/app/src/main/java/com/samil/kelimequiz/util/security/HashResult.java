package com.samil.kelimequiz.util.security;

public class HashResult {
    private final String hashBase64;
    private final String saltBase64;
    private final int iterations;

    public HashResult(String hashBase64, String saltBase64, int iterations) {
        this.hashBase64 = hashBase64;
        this.saltBase64 = saltBase64;
        this.iterations = iterations;
    }

    public String getHashBase64() {
        return hashBase64;
    }

    public String getSaltBase64() {
        return saltBase64;
    }

    public int getIterations() {
        return iterations;
    }
}
