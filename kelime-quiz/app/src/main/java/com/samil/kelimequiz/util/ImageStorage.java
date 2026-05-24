package com.samil.kelimequiz.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class ImageStorage {
    private static final String IMAGE_DIR = "word_images";
    private static final String SEED_IMAGE_DIR = "seed_images";

    private ImageStorage() {
    }

    public static String copyToAppStorage(Context context, Uri sourceUri) {
        try {
            File imageDirectory = new File(context.getFilesDir(), IMAGE_DIR);
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                throw new IllegalStateException("Görsel klasörü oluşturulamadı.");
            }

            File targetFile = new File(imageDirectory, "word_" + System.currentTimeMillis() + ".jpg");
            try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
                 OutputStream outputStream = new FileOutputStream(targetFile)) {
                if (inputStream == null) {
                    throw new IllegalStateException("Seçilen görsel okunamadı.");
                }
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("Görsel kaydedilemedi.", e);
        }
    }

    public static String copySeedAssetToAppStorage(Context context, String assetPath) {
        String normalizedAssetPath = normalizeAssetPath(assetPath);
        if (normalizedAssetPath == null) {
            return null;
        }

        try {
            File imageDirectory = new File(context.getFilesDir(), SEED_IMAGE_DIR);
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                throw new IllegalStateException("Tohum görsel klasörü oluşturulamadı.");
            }

            String fileName = new File(normalizedAssetPath).getName();
            File targetFile = new File(imageDirectory, fileName);
            try (InputStream inputStream = context.getAssets().open(normalizedAssetPath);
                 OutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("Tohum görseli kopyalanamadı.", e);
        }
    }

    public static void clearSeedImageCache(Context context) {
        deleteRecursively(new File(context.getFilesDir(), SEED_IMAGE_DIR));
    }

    private static String normalizeAssetPath(String assetPath) {
        if (assetPath == null) {
            return null;
        }
        String trimmed = assetPath.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        while (trimmed.startsWith("assets/")) {
            trimmed = trimmed.substring("assets/".length());
        }
        return trimmed;
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        if (!file.delete()) {
            throw new IllegalStateException("Tohum görsel önbelleği silinemedi: " + file.getAbsolutePath());
        }
    }
}
