package com.samil.kelimequiz.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.entity.WordEntity;
import com.samil.kelimequiz.data.local.entity.WordWithLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordCardAdapter extends RecyclerView.Adapter<WordCardAdapter.WordViewHolder> {
    public interface WordActionListener {
        void onDetailRequested(WordEntity word);
        void onDeleteRequested(WordEntity word);
    }

    private final List<WordWithLevel> words = new ArrayList<>();
    private final Set<Integer> revealedWordIds = new HashSet<>();
    private final WordActionListener actionListener;

    public WordCardAdapter(WordActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setWords(List<WordWithLevel> newWords) {
        List<WordWithLevel> oldWords = new ArrayList<>(words);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new WordDiffCallback(oldWords, newWords));
        words.clear();
        words.addAll(newWords);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_card, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        WordWithLevel wordWithLevel = words.get(position);
        holder.bind(wordWithLevel, revealedWordIds.contains(wordWithLevel.word.wordId), actionListener);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEnglishWord;
        private final TextView tvTurkishWord;
        private final TextView tvLevelLabel;
        private final LinearProgressIndicator lpiWordLevel;
        private final ImageButton btnToggleMeaning;
        private final MaterialButton btnDetail;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglishWord = itemView.findViewById(R.id.tvEnglishWord);
            tvTurkishWord = itemView.findViewById(R.id.tvTurkishWord);
            tvLevelLabel = itemView.findViewById(R.id.tvLevelLabel);
            lpiWordLevel = itemView.findViewById(R.id.lpiWordLevel);
            btnToggleMeaning = itemView.findViewById(R.id.btnToggleMeaning);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }

        public void bind(WordWithLevel wordWithLevel, boolean isRevealed, WordActionListener listener) {
            WordEntity word = wordWithLevel.word;
            tvEnglishWord.setText(word.engWord);
            tvTurkishWord.setText(word.trWord);
            tvTurkishWord.setVisibility(isRevealed ? View.VISIBLE : View.GONE);
            
            btnToggleMeaning.setImageResource(isRevealed ? R.drawable.ic_eye : R.drawable.ic_eye_off);
            
            updateLevelStatus(wordWithLevel);

            btnToggleMeaning.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                if (revealedWordIds.contains(word.wordId)) {
                    revealedWordIds.remove(word.wordId);
                } else {
                    revealedWordIds.add(word.wordId);
                }
                notifyItemChanged(position);
            });

            btnDetail.setOnClickListener(v -> listener.onDetailRequested(word));
            
            itemView.setOnLongClickListener(v -> {
                listener.onDeleteRequested(word);
                return true;
            });
        }

        private void updateLevelStatus(WordWithLevel wordWithLevel) {
            if (tvLevelLabel != null) {
                String category = wordWithLevel.word.category == null
                        ? itemView.getContext().getString(R.string.category_hint)
                        : wordWithLevel.word.category;
                String cefrLevel = wordWithLevel.word.cefrLevel == null ? "A1" : wordWithLevel.word.cefrLevel;
                tvLevelLabel.setText(itemView.getContext().getString(R.string.word_meta_format, cefrLevel, category));
            }
            if (lpiWordLevel != null) {
                int level = wordWithLevel.level;
                int progress = (level * 100) / 6;
                lpiWordLevel.setProgressCompat(progress, true);
            }
        }
    }

    private static class WordDiffCallback extends DiffUtil.Callback {
        private final List<WordWithLevel> oldWords;
        private final List<WordWithLevel> newWords;

        WordDiffCallback(List<WordWithLevel> oldWords, List<WordWithLevel> newWords) {
            this.oldWords = oldWords;
            this.newWords = newWords;
        }

        @Override
        public int getOldListSize() {
            return oldWords.size();
        }

        @Override
        public int getNewListSize() {
            return newWords.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldWords.get(oldItemPosition).word.wordId == newWords.get(newItemPosition).word.wordId;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            WordWithLevel oldItem = oldWords.get(oldItemPosition);
            WordWithLevel newItem = newWords.get(newItemPosition);
            return oldItem.word.engWord.equals(newItem.word.engWord)
                    && oldItem.word.trWord.equals(newItem.word.trWord)
                    && oldItem.level == newItem.level
                    && ((oldItem.word.cefrLevel == null && newItem.word.cefrLevel == null)
                    || (oldItem.word.cefrLevel != null && oldItem.word.cefrLevel.equals(newItem.word.cefrLevel)));
        }
    }
}
