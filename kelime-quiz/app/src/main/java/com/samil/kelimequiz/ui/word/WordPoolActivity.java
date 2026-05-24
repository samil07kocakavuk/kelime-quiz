package com.samil.kelimequiz.ui.word;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.data.local.entity.WordWithLevel;
import com.samil.kelimequiz.domain.model.WordDetails;
import com.samil.kelimequiz.domain.model.WordCategories;
import com.samil.kelimequiz.domain.service.WordReportHtmlBuilder;
import com.samil.kelimequiz.ui.auth.LoginActivity;
import com.samil.kelimequiz.ui.main.WordCardAdapter;
import com.samil.kelimequiz.ui.profile.ProfileActivity;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;
import com.samil.kelimequiz.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WordPoolActivity extends AppCompatActivity implements WordCardAdapter.WordActionListener {
    private static final String[] SORT_OPTIONS = {
            "En Yeni",
            "Seviye (Yüksekten Alçağa)",
            "Seviye (Alçaktan Yükseğe)"
    };

    private WordCardAdapter wordAdapter;
    private TextView tvEmptyState;
    private Spinner spCategoryFilter;
    private Spinner spSortOrder;
    private SessionManager sessionManager;
    private List<WordWithLevel> allWords = new ArrayList<>();
    private final WordReportHtmlBuilder reportHtmlBuilder = new WordReportHtmlBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_pool);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndClose();
            return;
        }

        tvEmptyState = findViewById(R.id.tvEmptyState);
        spCategoryFilter = findViewById(R.id.spCategoryFilter);
        spSortOrder = findViewById(R.id.spSortOrder);
        RecyclerView rvWords = findViewById(R.id.rvWords);
        wordAdapter = new WordCardAdapter(this);
        rvWords.setAdapter(wordAdapter);

        NavigationHelper.bindTopBar(this, true, ProfileActivity.class);
        NavigationHelper.bindBottomBar(this);
        setupCategorySpinner();
        setupSortSpinner();

        findViewById(R.id.btnPrintReport).setOnClickListener(v -> printReport());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, WordCategories.FILTERS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoryFilter.setAdapter(adapter);

        spCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, SORT_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSortOrder.setAdapter(adapter);

        spSortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFilters() {
        if (allWords == null || allWords.isEmpty()) return;

        String category = WordCategories.FILTERS[spCategoryFilter.getSelectedItemPosition()];
        int sortPosition = spSortOrder.getSelectedItemPosition();

        List<WordWithLevel> filtered;
        if (category.equals("Tümü")) {
            filtered = new ArrayList<>(allWords);
        } else {
            filtered = allWords.stream()
                    .filter(w -> category.equals(w.word.category))
                    .collect(Collectors.toList());
        }

        // Sort the filtered list
        if (sortPosition == 1) { // High to Low
            filtered.sort((w1, w2) -> Integer.compare(w2.level, w1.level));
        } else if (sortPosition == 2) { // Low to High
            filtered.sort((w1, w2) -> Integer.compare(w1.level, w2.level));
        } else { // Newest First
            filtered.sort((w1, w2) -> Long.compare(w2.word.createdAt, w1.word.createdAt));
        }

        wordAdapter.setWords(filtered);
        tvEmptyState.setText(filtered.isEmpty() ? R.string.word_pool_empty : R.string.word_pool_help);
        
        // Sıralama veya filtre değişince listenin en başına kaydır
        RecyclerView rvWords = findViewById(R.id.rvWords);
        if (rvWords != null && !filtered.isEmpty()) {
            rvWords.scrollToPosition(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadWords();
        }
    }

    private void loadWords() {
        int userId = sessionManager.getUserId();
        tvEmptyState.setText(R.string.word_pool_loading);
        AppExecutors.io().execute(() -> {
            List<WordWithLevel> words = AppContainer.from(this).wordRepository.listWords(userId);
            runOnUiThread(() -> {
                allWords = words;
                applyFilters();
            });
        });
    }

    @Override
    public void onDetailRequested(WordEntity word) {
        int userId = sessionManager.getUserId();
        AppExecutors.io().execute(() -> {
            WordDetails details = AppContainer.from(this).wordRepository.getWordDetails(userId, word.wordId);
            runOnUiThread(() -> showWordDialog(details));
        });
    }

    @Override
    public void onDeleteRequested(WordEntity word) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_word_title)
                .setMessage(getString(R.string.delete_word_message, word.engWord))
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(R.string.delete_action, (dialog, which) -> deleteWord(word.wordId))
                .show();
    }

    private void showWordDialog(WordDetails details) {
        WordDetailBottomSheet bottomSheet = new WordDetailBottomSheet(details);
        bottomSheet.show(getSupportFragmentManager(), "WordDetail");
    }

    private void deleteWord(int wordId) {
        int userId = sessionManager.getUserId();
        AppExecutors.io().execute(() -> {
            AppContainer.from(this).wordRepository.deleteWord(userId, wordId);
            runOnUiThread(this::loadWords);
        });
    }

    private void openLoginAndClose() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void printReport() {
        if (allWords == null || allWords.isEmpty()) return;
        String html = reportHtmlBuilder.build(allWords);
        new WordReportPrinter(this).print(html);
    }
}
