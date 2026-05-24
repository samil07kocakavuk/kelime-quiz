package com.samil.kelimequiz.data.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LlmApiClient {
    private static final String TEXT_URL = "https://text.pollinations.ai/";
    private static final String IMAGE_URL = "https://image.pollinations.ai/prompt/";

    private LlmApiClient() {
    }

    public static String generateStory(List<String> words) throws IOException {
        String wordList = String.join(", ", words);
        String systemPrompt = "Sen bir yardimci egitim asistanisin. Sana verilen 5 Ingilizce kelimeyi kullanarak kisa bir Turkce hikaye yazmalisin (3-4 cumle). Ingilizce kelimeleri hikaye icinde aynen koru, cevirme. Sadece hikayeyi yaz.";
        String userPrompt = "Kelimeler: " + wordList;

        IOException lastException = null;
        for (int i = 0; i < 3; i++) {
            try {
                HttpURLConnection connection = openConnection(buildTextUrl(userPrompt, systemPrompt), 15000, 30000);
                try {
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder storyBuilder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                storyBuilder.append(line).append('\n');
                            }
                            String result = storyBuilder.toString().trim();
                            if (!result.isEmpty()) {
                                return result;
                            }
                        }
                        lastException = new IOException("Hikaye metni bos geldi.");
                    } else {
                        lastException = new IOException("Beklenmeyen HTTP kodu: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                lastException = e;
                if (i < 2) {
                    sleepQuietly(1000L * (i + 1));
                }
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("Hikaye olusturulamadi (API Hatasi)");
    }

    public static Bitmap generateImage(String storyDescription) throws IOException {
        String prompt = "colorful children book illustration, no text, " + storyDescription;

        IOException lastException = null;
        for (int i = 0; i < 2; i++) {
            try {
                HttpURLConnection connection = openConnection(buildImageUrl(prompt), 20000, 45000);
                try {
                    connection.setInstanceFollowRedirects(true);
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (InputStream inputStream = connection.getInputStream()) {
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                return bitmap;
                            }
                        }
                        lastException = new IOException("Gorsel cozulenemedi.");
                    } else {
                        lastException = new IOException("Beklenmeyen HTTP kodu: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                lastException = e;
                if (i < 1) {
                    sleepQuietly(1500L);
                }
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("Gorsel olusturulamadi (API Hatasi)");
    }

    private static URL buildTextUrl(String userPrompt, String systemPrompt) throws IOException {
        Uri uri = Uri.parse(TEXT_URL).buildUpon()
                .appendPath(userPrompt)
                .appendQueryParameter("system", systemPrompt)
                .appendQueryParameter("model", "openai")
                .build();
        return new URL(uri.toString());
    }

    private static URL buildImageUrl(String prompt) throws IOException {
        Uri uri = Uri.parse(IMAGE_URL).buildUpon()
                .appendPath(prompt)
                .appendQueryParameter("width", "512")
                .appendQueryParameter("height", "512")
                .appendQueryParameter("nologo", "true")
                .appendQueryParameter("model", "flux")
                .build();
        return new URL(uri.toString());
    }

    private static HttpURLConnection openConnection(URL url, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        return connection;
    }

    private static void sleepQuietly(long millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("İstek kesildi.", e);
        }
    }
}
