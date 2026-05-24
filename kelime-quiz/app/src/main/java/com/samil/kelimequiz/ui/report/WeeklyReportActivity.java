package com.samil.kelimequiz.ui.report;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.samil.kelimequiz.R;
import com.samil.kelimequiz.domain.model.DayProgress;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.SessionManager;

import java.util.List;

public class WeeklyReportActivity extends AppCompatActivity {

    private LinearLayout layoutBarChart;
    private TextView tvReportAnalysis;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        layoutBarChart = findViewById(R.id.layoutBarChart);
        tvReportAnalysis = findViewById(R.id.tvReportAnalysis);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        loadReportData();
    }

    private void loadReportData() {
        AppExecutors.io().execute(() -> {
            AppContainer container = AppContainer.from(this);
            List<DayProgress> weeklyProgress = container.quizRepository.getWeeklyProgress(userId);
            
            runOnUiThread(() -> {
                updateWeeklyProgress(weeklyProgress);
                updateAnalysis(weeklyProgress);
            });
        });
    }

    private void updateWeeklyProgress(List<DayProgress> progressList) {
        if (layoutBarChart == null) return;
        layoutBarChart.removeAllViews();

        for (DayProgress dp : progressList) {
            View itemView = getLayoutInflater().inflate(R.layout.item_bar_chart, layoutBarChart, false);
            View viewBar = itemView.findViewById(R.id.viewBar);
            TextView tvDayName = itemView.findViewById(R.id.tvDayName);

            tvDayName.setText(dp.day);

            float density = getResources().getDisplayMetrics().density;
            int maxHeightPx = (int) (180 * density); // Daha yüksek barlar
            int barHeight = (int) (dp.avgSuccess * maxHeightPx / 100.0);
            
            if (dp.avgSuccess > 0 && barHeight < (int)(4 * density)) {
                barHeight = (int)(4 * density);
            }

            ViewGroup.LayoutParams params = viewBar.getLayoutParams();
            params.height = barHeight;
            viewBar.setLayoutParams(params);

            layoutBarChart.addView(itemView);
        }
    }

    private void updateAnalysis(List<DayProgress> progressList) {
        double totalSuccess = 0;
        int activeDays = 0;
        for (DayProgress dp : progressList) {
            if (dp.avgSuccess > 0) {
                totalSuccess += dp.avgSuccess;
                activeDays++;
            }
        }

        String analysis;
        if (activeDays == 0) {
            analysis = "Henüz bu hafta hiç quiz çözmemişsin. Öğrendiğin kelimeleri pekiştirmek için hemen bir quiz başlat!";
        } else {
            double weeklyAvg = totalSuccess / activeDays;
            if (weeklyAvg >= 80) {
                analysis = "Harika gidiyorsun! Haftalık başarı ortalaman %" + (int)weeklyAvg + ". Bu tempoyu korumalısın.";
            } else if (weeklyAvg >= 50) {
                analysis = "İyi bir ilerleme kaydediyorsun. Başarı ortalaman %" + (int)weeklyAvg + ". Biraz daha tekrar yaparak bu oranı %80'in üzerine çıkarabilirsin.";
            } else {
                analysis = "Başarı ortalaman %" + (int)weeklyAvg + ". Kelimeleri daha sık tekrar etmen öğrenme sürecini hızlandıracaktır.";
            }
        }
        tvReportAnalysis.setText(analysis);
    }
}
