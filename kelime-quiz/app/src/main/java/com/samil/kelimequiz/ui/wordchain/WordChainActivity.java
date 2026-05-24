package com.samil.kelimequiz.ui.wordchain;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.AppDatabase;
import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.data.remote.LlmApiClient;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;
import com.samil.kelimequiz.util.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordChainActivity extends AppCompatActivity {
    private static final int WORD_COUNT = 5;
    private static final int MIN_STARTED_LEVEL = 1;

    private TextView tvWords;
    private TextView tvStoryText;
    private TextView tvImageStatus;
    private ImageView ivStoryImage;
    private MaterialButton btnGenerate;
    private MaterialCardView cardResult;

    private List<String> selectedWords = new ArrayList<>();
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_chain);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }
        userId = sessionManager.getUserId();

        bindViews();
        NavigationHelper.bindTopBar(this);
        NavigationHelper.bindBottomBar(this);
        pickRandomWords();
    }

    private void bindViews() {
        tvWords = findViewById(R.id.tvSelectedWords);
        tvStoryText = findViewById(R.id.tvStoryText);
        tvImageStatus = findViewById(R.id.tvImageStatus);
        ivStoryImage = findViewById(R.id.ivStoryImage);
        btnGenerate = findViewById(R.id.btnGenerateStory);
        cardResult = findViewById(R.id.cardStoryResult);

        btnGenerate.setOnClickListener(v -> generateStory());
    }

    private void pickRandomWords() {
        AppExecutors.io().execute(() -> {
            List<WordEntity> words = AppDatabase.getInstance(this)
                    .wordDao()
                    .listStartedWords(userId, MIN_STARTED_LEVEL);
            Collections.shuffle(words);
            List<String> picked = new ArrayList<>();
            for (int i = 0; i < Math.min(WORD_COUNT, words.size()); i++) {
                picked.add(words.get(i).engWord);
            }
            runOnUiThread(() -> {
                selectedWords = picked;
                if (picked.size() < WORD_COUNT) {
                    tvWords.setText(R.string.word_chain_no_words);
                    btnGenerate.setEnabled(false);
                } else {
                    tvWords.setText(getString(R.string.word_chain_words_label, String.join(", ", picked)));
                    btnGenerate.setEnabled(true);
                }
            });
        });
    }

    private void generateStory() {
        btnGenerate.setEnabled(false);
        btnGenerate.setText(R.string.word_chain_generating);
        cardResult.setVisibility(View.VISIBLE);
        tvStoryText.setText(R.string.word_chain_story_loading);
        ivStoryImage.setVisibility(View.GONE);
        tvImageStatus.setVisibility(View.GONE);

        AppExecutors.io().execute(() -> {
            try {
                String story = LlmApiClient.generateStory(selectedWords);
                runOnUiThread(() -> {
                    tvStoryText.setText(story);
                    tvImageStatus.setVisibility(View.VISIBLE);
                    tvImageStatus.setText(R.string.word_chain_image_loading);
                });

                Bitmap image = LlmApiClient.generateImage(story);
                runOnUiThread(() -> {
                    ivStoryImage.setImageBitmap(image);
                    ivStoryImage.setVisibility(View.VISIBLE);
                    tvImageStatus.setVisibility(View.GONE);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    if (tvStoryText.getText().toString().equals(getString(R.string.word_chain_story_loading))) {
                        tvStoryText.setText(getString(R.string.word_chain_story_error, e.getMessage()));
                    } else {
                        tvImageStatus.setVisibility(View.VISIBLE);
                        tvImageStatus.setText(getString(R.string.word_chain_image_error, e.getMessage()));
                    }
                });
            } finally {
                runOnUiThread(() -> {
                    btnGenerate.setEnabled(true);
                    btnGenerate.setText(R.string.word_chain_generate);
                });
            }
        });
    }
}
