package com.samil.kelimequiz.ui.report;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.repository.ActivityReportRepository;
import com.samil.kelimequiz.ui.word.WordReportPrinter;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.SessionManager;

import java.util.List;
import java.util.Locale;

public class WeeklyReportActivity extends AppCompatActivity {
    private static final int PERIOD_DAILY = ActivityReportRepository.PERIOD_DAILY;
    private static final int PERIOD_WEEKLY = ActivityReportRepository.PERIOD_WEEKLY;
    private static final int PERIOD_MONTHLY = ActivityReportRepository.PERIOD_MONTHLY;
    private static final int PERIOD_TOTAL = ActivityReportRepository.PERIOD_TOTAL;

    private MaterialButton btnTabDaily;
    private MaterialButton btnTabWeekly;
    private MaterialButton btnTabMonthly;
    private MaterialButton btnTabTotal;
    private MaterialButton btnPrintReport;

    private View sectionDaily;
    private View sectionWeekly;
    private View sectionMonthly;
    private View sectionTotal;

    private TextView tvReportPeriodTitle;
    private TextView tvReportPeriodSubtitle;
    private TextView tvHeroSummary;

    private TextView tvDailyAnalysis;
    private TextView tvWeeklyDistributionSubtitle;
    private TextView tvWeeklyComparisonSummary;
    private TextView tvWeeklyAnalysis;
    private TextView tvMonthlyDistributionSubtitle;
    private TextView tvMonthlyComparisonSummary;
    private TextView tvMonthlyAnalysis;
    private TextView tvTotalAnalysis;
    private TextView tvTotalChartSummary;

    private LinearLayout dailyMetricsContainer;
    private LinearLayout weeklyMetricsContainer;
    private LinearLayout monthlyMetricsContainer;
    private LinearLayout totalMetricsContainer;
    private LinearLayout weeklyBucketsContainer;
    private LinearLayout monthlyBucketsContainer;
    private LinearLayout weeklyLegendContainer;
    private LinearLayout monthlyLegendContainer;
    private LinearLayout totalPercentContainer;
    private LinearLayout totalLegendContainer;

    private ActivityDonutChartView weeklyChartView;
    private ActivityDonutChartView monthlyChartView;
    private ActivityDonutChartView totalChartView;

    private ActivityReportRepository reportRepository;
    private ActivityReportRepository.ActivityReportData dailyReport;
    private ActivityReportRepository.ActivityReportData weeklyReport;
    private ActivityReportRepository.ActivityReportData monthlyReport;
    private ActivityReportRepository.ActivityReportData totalReport;

    private int userId;
    private int currentPeriod = PERIOD_DAILY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }
        userId = sessionManager.getUserId();

        AppContainer container = AppContainer.from(this);
        reportRepository = container.activityReportRepository;

        bindViews();
        setupToolbar();
        setupTabs();
        setLoadingState();
        loadReports();
    }

    private void bindViews() {
        btnTabDaily = findViewById(R.id.btnTabDaily);
        btnTabWeekly = findViewById(R.id.btnTabWeekly);
        btnTabMonthly = findViewById(R.id.btnTabMonthly);
        btnTabTotal = findViewById(R.id.btnTabTotal);
        btnPrintReport = findViewById(R.id.btnPrintReport);

        sectionDaily = findViewById(R.id.sectionDaily);
        sectionWeekly = findViewById(R.id.sectionWeekly);
        sectionMonthly = findViewById(R.id.sectionMonthly);
        sectionTotal = findViewById(R.id.sectionTotal);

        tvReportPeriodTitle = findViewById(R.id.tvReportPeriodTitle);
        tvReportPeriodSubtitle = findViewById(R.id.tvReportPeriodSubtitle);
        tvHeroSummary = findViewById(R.id.tvHeroSummary);

        tvDailyAnalysis = findViewById(R.id.tvDailyAnalysis);
        tvWeeklyDistributionSubtitle = findViewById(R.id.tvWeeklyDistributionSubtitle);
        tvWeeklyComparisonSummary = findViewById(R.id.tvWeeklyComparisonSummary);
        tvWeeklyAnalysis = findViewById(R.id.tvWeeklyAnalysis);
        tvMonthlyDistributionSubtitle = findViewById(R.id.tvMonthlyDistributionSubtitle);
        tvMonthlyComparisonSummary = findViewById(R.id.tvMonthlyComparisonSummary);
        tvMonthlyAnalysis = findViewById(R.id.tvMonthlyAnalysis);
        tvTotalAnalysis = findViewById(R.id.tvTotalAnalysis);
        tvTotalChartSummary = findViewById(R.id.tvTotalChartSummary);

        dailyMetricsContainer = findViewById(R.id.dailyMetricsContainer);
        weeklyMetricsContainer = findViewById(R.id.weeklyMetricsContainer);
        monthlyMetricsContainer = findViewById(R.id.monthlyMetricsContainer);
        totalMetricsContainer = findViewById(R.id.totalMetricsContainer);
        weeklyBucketsContainer = findViewById(R.id.weeklyBucketsContainer);
        monthlyBucketsContainer = findViewById(R.id.monthlyBucketsContainer);
        weeklyLegendContainer = findViewById(R.id.weeklyLegendContainer);
        monthlyLegendContainer = findViewById(R.id.monthlyLegendContainer);
        totalPercentContainer = findViewById(R.id.totalPercentContainer);
        totalLegendContainer = findViewById(R.id.totalLegendContainer);

        weeklyChartView = findViewById(R.id.weeklyChartView);
        monthlyChartView = findViewById(R.id.monthlyChartView);
        totalChartView = findViewById(R.id.totalChartView);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupTabs() {
        btnTabDaily.setOnClickListener(v -> showPeriod(PERIOD_DAILY));
        btnTabWeekly.setOnClickListener(v -> showPeriod(PERIOD_WEEKLY));
        btnTabMonthly.setOnClickListener(v -> showPeriod(PERIOD_MONTHLY));
        btnTabTotal.setOnClickListener(v -> showPeriod(PERIOD_TOTAL));
        btnPrintReport.setOnClickListener(v -> printCurrentReport());
    }

    private void setLoadingState() {
        tvReportPeriodTitle.setText(R.string.report_loading);
        tvReportPeriodSubtitle.setText(R.string.report_loading);
        tvHeroSummary.setText(R.string.report_loading);
        tvDailyAnalysis.setText(R.string.report_loading);
        tvWeeklyDistributionSubtitle.setText(R.string.report_loading);
        tvWeeklyComparisonSummary.setText(R.string.report_loading);
        tvWeeklyAnalysis.setText(R.string.report_loading);
        tvMonthlyDistributionSubtitle.setText(R.string.report_loading);
        tvMonthlyComparisonSummary.setText(R.string.report_loading);
        tvMonthlyAnalysis.setText(R.string.report_loading);
        tvTotalAnalysis.setText(R.string.report_loading);
        tvTotalChartSummary.setText(R.string.report_loading);
    }

    private void loadReports() {
        Context appContext = getApplicationContext();
        AppExecutors.io().execute(() -> {
            ActivityReportRepository.ActivityReportData daily = reportRepository.buildDailyReport(appContext, userId);
            ActivityReportRepository.ActivityReportData weekly = reportRepository.buildWeeklyReport(appContext, userId);
            ActivityReportRepository.ActivityReportData monthly = reportRepository.buildMonthlyReport(appContext, userId);
            ActivityReportRepository.ActivityReportData total = reportRepository.buildTotalReport(appContext, userId);

            runOnUiThread(() -> {
                dailyReport = daily;
                weeklyReport = weekly;
                monthlyReport = monthly;
                totalReport = total;
                renderAllReports();
                showPeriod(currentPeriod);
            });
        });
    }

    private void renderAllReports() {
        renderDailyReport(dailyReport);
        renderWeeklyReport(weeklyReport);
        renderMonthlyReport(monthlyReport);
        renderTotalReport(totalReport);
    }

    private void renderDailyReport(ActivityReportRepository.ActivityReportData report) {
        if (report == null) return;
        tvDailyAnalysis.setText(report.note);
        setHero(report.title, report.subtitle, report.heroSummary);
        populateMetricContainer(dailyMetricsContainer, report.metrics);
    }

    private void renderWeeklyReport(ActivityReportRepository.ActivityReportData report) {
        if (report == null) return;
        tvWeeklyDistributionSubtitle.setText(report.note);
        tvWeeklyComparisonSummary.setText(report.insight);
        tvWeeklyAnalysis.setText(report.chartSummary);
        populateMetricContainer(weeklyMetricsContainer, report.metrics);
        populateBucketContainer(weeklyBucketsContainer, report.buckets);
        populateLegendContainer(weeklyLegendContainer, report.segments);
        bindChart(weeklyChartView, report);
    }

    private void renderMonthlyReport(ActivityReportRepository.ActivityReportData report) {
        if (report == null) return;
        tvMonthlyDistributionSubtitle.setText(report.note);
        tvMonthlyComparisonSummary.setText(report.insight);
        tvMonthlyAnalysis.setText(report.chartSummary);
        populateMetricContainer(monthlyMetricsContainer, report.metrics);
        populateBucketContainer(monthlyBucketsContainer, report.buckets);
        populateLegendContainer(monthlyLegendContainer, report.segments);
        bindChart(monthlyChartView, report);
    }

    private void renderTotalReport(ActivityReportRepository.ActivityReportData report) {
        if (report == null) return;
        tvTotalAnalysis.setText(report.note);
        populateMetricContainer(totalMetricsContainer, report.metrics);
        tvTotalChartSummary.setText(report.chartSummary);
        populatePercentTagContainer(totalPercentContainer, report);
        populateLegendContainer(totalLegendContainer, report.segments);
        bindChart(totalChartView, report);
    }

    private void setHero(String title, String subtitle, String summary) {
        tvReportPeriodTitle.setText(title);
        tvReportPeriodSubtitle.setText(subtitle);
        tvHeroSummary.setText(summary);
    }

    private void showPeriod(int period) {
        currentPeriod = period;

        sectionDaily.setVisibility(period == PERIOD_DAILY ? View.VISIBLE : View.GONE);
        sectionWeekly.setVisibility(period == PERIOD_WEEKLY ? View.VISIBLE : View.GONE);
        sectionMonthly.setVisibility(period == PERIOD_MONTHLY ? View.VISIBLE : View.GONE);
        sectionTotal.setVisibility(period == PERIOD_TOTAL ? View.VISIBLE : View.GONE);

        updateTabStyle(btnTabDaily, period == PERIOD_DAILY);
        updateTabStyle(btnTabWeekly, period == PERIOD_WEEKLY);
        updateTabStyle(btnTabMonthly, period == PERIOD_MONTHLY);
        updateTabStyle(btnTabTotal, period == PERIOD_TOTAL);

        ActivityReportRepository.ActivityReportData report = getReportForPeriod(period);
        if (report != null) {
            setHero(report.title, report.subtitle, report.heroSummary);
        }

        animateSection(getVisibleSection(period));
    }

    private View getVisibleSection(int period) {
        if (period == PERIOD_WEEKLY) return sectionWeekly;
        if (period == PERIOD_MONTHLY) return sectionMonthly;
        if (period == PERIOD_TOTAL) return sectionTotal;
        return sectionDaily;
    }

    private void updateTabStyle(MaterialButton button, boolean selected) {
        int background = selected ? R.color.primary : R.color.card;
        int textColor = selected ? R.color.text_on_primary : R.color.text_primary;
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, background)));
        button.setTextColor(ContextCompat.getColor(this, textColor));
    }

    private ActivityReportRepository.ActivityReportData getReportForPeriod(int period) {
        switch (period) {
            case PERIOD_WEEKLY:
                return weeklyReport;
            case PERIOD_MONTHLY:
                return monthlyReport;
            case PERIOD_TOTAL:
                return totalReport;
            default:
                return dailyReport;
        }
    }

    private void populateMetricContainer(LinearLayout container, List<ActivityReportRepository.ActivityMetric> metrics) {
        container.removeAllViews();
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        for (int i = 0; i < metrics.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(2f);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            row.addView(createMetricCell(metrics.get(i)));
            if (i + 1 < metrics.size()) {
                row.addView(createMetricCell(metrics.get(i + 1)));
            } else {
                View spacer = new View(this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, 1, 1f);
                spacerParams.leftMargin = dp(4);
                spacer.setLayoutParams(spacerParams);
                row.addView(spacer);
            }

            container.addView(row);
            animateView(row, i * 35L);
        }
    }

    private View createMetricCell(ActivityReportRepository.ActivityMetric metric) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_report_metric, null, false);
        View glow = itemView.findViewById(R.id.viewMetricGlow);
        MaterialCardView card = itemView.findViewById(R.id.cardMetric);
        View accent = itemView.findViewById(R.id.viewMetricAccent);
        View iconContainer = itemView.findViewById(R.id.ivMetricIcon).getParent() instanceof View
                ? (View) itemView.findViewById(R.id.ivMetricIcon).getParent()
                : null;
        TextView label = itemView.findViewById(R.id.tvMetricLabel);
        TextView value = itemView.findViewById(R.id.tvMetricValue);
        android.widget.ImageView icon = itemView.findViewById(R.id.ivMetricIcon);

        int accentColor = ContextCompat.getColor(this, metric.accentColorResId);
        glow.setBackgroundTintList(ColorStateList.valueOf(adjustAlpha(accentColor, 0.28f)));
        glow.setAlpha(0.78f);
        accent.setBackgroundColor(accentColor);
        icon.setImageResource(metric.iconResId);
        icon.setImageTintList(ColorStateList.valueOf(accentColor));
        label.setText(metric.label);
        value.setText(metric.value);

        if (iconContainer != null) {
            iconContainer.setBackgroundTintList(ColorStateList.valueOf(adjustAlpha(accentColor, 0.10f)));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(160), 1f);
        params.leftMargin = dp(4);
        params.rightMargin = dp(4);
        params.topMargin = dp(4);
        params.bottomMargin = dp(4);
        itemView.setLayoutParams(params);
        return itemView;
    }

    private void populateBucketContainer(LinearLayout container, List<ActivityReportRepository.ActivityBucket> buckets) {
        container.removeAllViews();
        if (buckets == null || buckets.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.report_empty_bucket);
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            empty.setTextSize(13f);
            empty.setPadding(dp(4), dp(2), dp(4), dp(2));
            container.addView(empty);
            return;
        }

        for (int i = 0; i < buckets.size(); i++) {
            ActivityReportRepository.ActivityBucket bucket = buckets.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_report_bucket, container, false);
            View glow = itemView.findViewById(R.id.viewBucketGlow);
            MaterialCardView card = itemView.findViewById(R.id.cardBucket);
            View accent = itemView.findViewById(R.id.viewBucketAccent);
            TextView title = itemView.findViewById(R.id.tvBucketTitle);
            TextView meta = itemView.findViewById(R.id.tvBucketMeta);
            TextView details = itemView.findViewById(R.id.tvBucketDetails);
            TextView percent = itemView.findViewById(R.id.tvBucketPercent);

            int accentColor = ContextCompat.getColor(this, bucket.colorResId);
            glow.setBackgroundTintList(ColorStateList.valueOf(adjustAlpha(accentColor, 0.28f)));
            glow.setAlpha(0.78f);
            accent.setBackgroundColor(accentColor);
            title.setText(bucket.title);
            meta.setText(bucket.meta);
            details.setText(bucket.details);
            percent.setText(bucket.percent);
            View iconContainer = accent.getParent() instanceof View ? (View) accent.getParent() : null;
            if (iconContainer != null) {
                iconContainer.setBackgroundTintList(ColorStateList.valueOf(adjustAlpha(accentColor, 0.10f)));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = dp(4);
            params.bottomMargin = dp(4);
            itemView.setLayoutParams(params);
            container.addView(itemView);
            animateView(itemView, i * 30L);
        }
    }

    private void populateLegendContainer(LinearLayout container, List<ActivityReportRepository.ActivitySegment> segments) {
        container.removeAllViews();
        if (segments == null || segments.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.report_chart_legend_empty);
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            empty.setTextSize(13f);
            container.addView(empty);
            return;
        }

        for (int i = 0; i < segments.size(); i++) {
            ActivityReportRepository.ActivitySegment segment = segments.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_report_legend, container, false);
            View dot = itemView.findViewById(R.id.viewLegendColor);
            TextView label = itemView.findViewById(R.id.tvLegendLabel);
            TextView value = itemView.findViewById(R.id.tvLegendValue);

            dot.setBackgroundColor(ContextCompat.getColor(this, segment.colorResId));
            label.setText(segment.label);
            value.setText(String.format(Locale.getDefault(), "%d", segment.value));
            container.addView(itemView);
            animateView(itemView, i * 25L);
        }
    }

    private void populatePercentTagContainer(LinearLayout container, ActivityReportRepository.ActivityReportData report) {
        container.removeAllViews();
        if (report == null || report.segments == null || report.segments.isEmpty() || report.totalActivity <= 0) {
            return;
        }

        for (int i = 0; i < report.segments.size(); i++) {
            ActivityReportRepository.ActivitySegment segment = report.segments.get(i);
            if (segment.value <= 0) continue;

            View itemView = LayoutInflater.from(this).inflate(R.layout.item_report_percent_tag, container, false);
            View dot = itemView.findViewById(R.id.viewTagColor);
            TextView label = itemView.findViewById(R.id.tvTagLabel);
            TextView percent = itemView.findViewById(R.id.tvTagPercent);

            dot.setBackgroundColor(ContextCompat.getColor(this, segment.colorResId));
            label.setText(segment.label);
            percent.setText(String.format(Locale.getDefault(), "%d%%", Math.round((segment.value * 100f) / report.totalActivity)));
            container.addView(itemView);
            animateView(itemView, i * 20L);
        }
    }

    private void bindChart(ActivityDonutChartView chartView, ActivityReportRepository.ActivityReportData report) {
        if (chartView == null || report == null) return;

        List<ActivityDonutChartView.Segment> segments = new java.util.ArrayList<>();
        if (report.segments != null) {
            for (ActivityReportRepository.ActivitySegment segment : report.segments) {
                segments.add(new ActivityDonutChartView.Segment(
                        segment.label,
                        segment.value,
                        ContextCompat.getColor(this, segment.colorResId)
                ));
            }
        }

        chartView.setSegments(segments);
        chartView.setCenterValue(String.format(Locale.getDefault(), "%d", report.totalActivity));
        chartView.setCenterLabel(getString(R.string.report_chart_center_activity));
        chartView.setEmptyText(getString(R.string.report_chart_empty));
    }

    private void printCurrentReport() {
        ActivityReportRepository.ActivityReportData report = totalReport;
        if (report == null) {
            return;
        }
        String html = buildReportHtml(report);
        new WordReportPrinter(this).print(html);
    }

    private String buildReportHtml(ActivityReportRepository.ActivityReportData report) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><meta charset='utf-8'>")
                .append("<style>")
                .append("body{font-family:sans-serif;padding:24px;color:#1b2430;}")
                .append("h1{color:#1b434d;margin-bottom:8px;}")
                .append("h2{margin-top:24px;color:#244b56;}")
                .append(".card{border:1px solid #d7d3de;border-radius:14px;padding:12px;margin:10px 0;}")
                .append(".muted{color:#627082;}")
                .append("</style></head><body>");
        builder.append("<h1>").append(Html.escapeHtml(report.title)).append("</h1>");
        builder.append("<p class='muted'>").append(Html.escapeHtml(report.subtitle)).append("</p>");
        builder.append("<p><strong>").append(Html.escapeHtml(report.heroSummary)).append("</strong></p>");
        builder.append("<h2>").append(Html.escapeHtml(getString(R.string.report_total_section_title))).append("</h2>");
        builder.append("<p>").append(Html.escapeHtml(report.note)).append("</p>");
        for (ActivityReportRepository.ActivityMetric metric : report.metrics) {
            builder.append("<div class='card'><strong>")
                    .append(Html.escapeHtml(metric.label))
                    .append("</strong><br>")
                    .append(Html.escapeHtml(metric.value))
                    .append("<br><span class='muted'>")
                    .append(Html.escapeHtml(metric.subtitle))
                    .append("</span></div>");
        }
        builder.append("</body></html>");
        return builder.toString();
    }

    private void animateSection(View section) {
        if (section == null) return;
        section.setAlpha(0f);
        section.setTranslationY(dp(8));
        section.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateView(View view, long delayMs) {
        view.setAlpha(0f);
        view.setTranslationY(dp(12));
        view.setScaleX(0.98f);
        view.setScaleY(0.98f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delayMs)
                .setDuration(220L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int adjustAlpha(int color, float alpha) {
        int alphaInt = Math.round(255 * alpha);
        return (color & 0x00FFFFFF) | (alphaInt << 24);
    }
}
