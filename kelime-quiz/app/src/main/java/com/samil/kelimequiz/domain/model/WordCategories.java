package com.samil.kelimequiz.domain.model;

public final class WordCategories {
    public static final String[] ALL = {
            "Hayvanlar",
            "Meslekler",
            "Sporlar",
            "Yiyecekler",
            "Ev ve Eşyalar",
            "Okul",
            "Ulaşım",
            "Doğa",
            "Sıfatlar",
            "Fiiller",
            "Sağlık ve Vücut",
            "Günlük Yaşam"
    };

    public static final String[] FILTERS = {
            "Tümü",
            "Hayvanlar",
            "Meslekler",
            "Sporlar",
            "Yiyecekler",
            "Ev ve Eşyalar",
            "Okul",
            "Ulaşım",
            "Doğa",
            "Sıfatlar",
            "Fiiller",
            "Sağlık ve Vücut",
            "Günlük Yaşam"
    };

    public static WordLevel defaultLevelFor(String category) {
        if (category == null) {
            return WordLevel.A1;
        }

        switch (category) {
            case "Hayvanlar":
            case "Yiyecekler":
            case "Ev ve Eşyalar":
            case "Okul":
                return WordLevel.A1;
            case "Meslekler":
            case "Ulaşım":
            case "Günlük Yaşam":
                return WordLevel.A2;
            case "Doğa":
            case "Sıfatlar":
            case "Fiiller":
            case "Sağlık ve Vücut":
            case "Sporlar":
                return WordLevel.B1;
            default:
                return WordLevel.A1;
        }
    }

    private WordCategories() {
    }
}
