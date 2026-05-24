package com.samil.kelimequiz.domain.model;

public class AuthResult {
    private final boolean success;
    private final int userId;
    private final String message;

    private AuthResult(boolean success, int userId, String message) {
        this.success = success;
        this.userId = userId;
        this.message = message;
    }

    public static AuthResult success(int userId, String message) {
        return new AuthResult(true, userId, message);
    }

    public static AuthResult fail(String message) {
        return new AuthResult(false, -1, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }
}
