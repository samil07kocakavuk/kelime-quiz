package com.samil.kelimequiz.domain.model;

import java.util.Locale;

public enum WordLevel {
    A1,
    A2,
    B1,
    B2,
    C1,
    C2;

    public static WordLevel fromCategory(String category) {
        return WordCategories.defaultLevelFor(category);
    }

    public static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return A1.name();
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("A1".equals(normalized) || "A2".equals(normalized) || "B1".equals(normalized)
                || "B2".equals(normalized) || "C1".equals(normalized) || "C2".equals(normalized)) {
            return normalized;
        }
        return A1.name();
    }
}
