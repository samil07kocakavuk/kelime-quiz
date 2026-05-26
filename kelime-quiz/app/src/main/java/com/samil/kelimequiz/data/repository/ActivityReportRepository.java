package com.samil.kelimequiz.data.repository;

import android.content.Context;

import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.dao.ActivityLogDao;
import com.samil.kelimequiz.data.local.dao.QuizResultDao;
import com.samil.kelimequiz.data.local.entity.ActivityLogEntity;
import com.samil.kelimequiz.data.local.entity.QuizResultEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivityReportRepository {
    public static final int PERIOD_DAILY = 0;
    public static final int PERIOD_WEEKLY = 1;
    public static final int PERIOD_MONTHLY = 2;
    public static final int PERIOD_TOTAL = 3;

    private final QuizResultDao quizResultDao;
    private final ActivityLogDao activityLogDao;

    public ActivityReportRepository(QuizResultDao quizResultDao,
                                    ActivityLogDao activityLogDao) {
        this.quizResultDao = quizResultDao;
        this.activityLogDao = activityLogDao;
    }

    public ActivityReportData buildDailyReport(Context context, int userId) {
        return buildReport(context, userId, PERIOD_DAILY);
    }

    public ActivityReportData buildWeeklyReport(Context context, int userId) {
        return buildReport(context, userId, PERIOD_WEEKLY);
    }

    public ActivityReportData buildMonthlyReport(Context context, int userId) {
        return buildReport(context, userId, PERIOD_MONTHLY);
    }

    public ActivityReportData buildTotalReport(Context context, int userId) {
        return buildReport(context, userId, PERIOD_TOTAL);
    }

    private ActivityReportData buildReport(Context context, int userId, int period) {
        Range range = resolveRange(period);
        List<QuizResultEntity> allResults = quizResultDao.listByUser(userId);
        List<QuizResultEntity> results = filterResults(allResults, range.startAt, range.endAt);
        List<ActivityLogEntity> logs = activityLogDao.listByUserAndRange(userId, range.startAt, range.endAt);
        Map<String, Integer> logCounts = groupLogCounts(logs);
        int newLevelOneCount = logCounts.getOrDefault(ActivityLogEntity.TYPE_NEW_LEVEL_ONE, 0);
        int wordleCompletedCount = logCounts.getOrDefault(ActivityLogEntity.TYPE_WORDLE_COMPLETED, 0);
        int wordleWonCount = logCounts.getOrDefault(ActivityLogEntity.TYPE_WORDLE_WON, 0);
        int aiStoryCount = logCounts.getOrDefault(ActivityLogEntity.TYPE_AI_STORY, 0);

        int totalQuestions = 0;
        int correctAnswers = 0;
        for (QuizResultEntity result : results) {
            totalQuestions += result.totalQuestions;
            correctAnswers += result.correctAnswers;
        }
        int wrongAnswers = Math.max(0, totalQuestions - correctAnswers);
        int totalActivity = totalQuestions + wordleCompletedCount + aiStoryCount;
        double averageActivity = calculateAverageActivity(period, range, totalActivity);

        List<ActivityMetric> metrics = buildMetrics(context, totalQuestions, correctAnswers, wrongAnswers,
                newLevelOneCount, wordleCompletedCount, wordleWonCount, aiStoryCount, totalActivity, averageActivity, period);

        List<ActivityBucket> buckets = buildBuckets(context, period, results, logs, range.startAt, range.endAt, totalActivity);
        List<ActivitySegment> segments = buildSegments(buckets);

        String periodTitle = getPeriodTitle(context, period);
        String note = getPeriodNote(context, period, totalActivity, totalQuestions, correctAnswers, wrongAnswers, wordleCompletedCount, aiStoryCount);
        String subtitle = buildSubtitle(context, period, totalActivity, totalQuestions, correctAnswers, wrongAnswers, wordleCompletedCount, aiStoryCount);
        String insight = buildInsight(context, period, buckets, totalActivity);
        String chartSummary = buildChartSummary(context, period, buckets);
        String heroSummary = buildHeroSummary(context, period, buckets, totalActivity, totalQuestions, correctAnswers, wrongAnswers, wordleCompletedCount, aiStoryCount);

        return new ActivityReportData(
                period,
                periodTitle,
                subtitle,
                note,
                insight,
                heroSummary,
                chartSummary,
                totalActivity,
                metrics,
                buckets,
                segments
        );
    }

    private List<ActivityMetric> buildMetrics(Context context,
                                              int totalQuestions,
                                              int correctAnswers,
                                              int wrongAnswers,
                                              int newLevelOneCount,
                                              int wordleCompletedCount,
                                              int wordleWonCount,
                                              int aiStoryCount,
                                              int totalActivity,
                                              double averageActivity,
                                              int period) {
        List<ActivityMetric> metrics = new ArrayList<>();
        metrics.add(new ActivityMetric(R.drawable.ic_quiz_graphic,
                R.color.report_chart_1,
                context.getString(R.string.report_metric_quiz_questions),
                Integer.toString(totalQuestions),
                context.getString(R.string.report_metric_sub_quiz)));
        metrics.add(new ActivityMetric(R.drawable.ic_check_graphic,
                R.color.report_chart_3,
                context.getString(R.string.report_metric_quiz_correct),
                Integer.toString(correctAnswers),
                context.getString(R.string.report_metric_sub_correct)));
        metrics.add(new ActivityMetric(R.drawable.ic_close_graphic,
                R.color.report_chart_2,
                context.getString(R.string.report_metric_quiz_wrong),
                Integer.toString(wrongAnswers),
                context.getString(R.string.report_metric_sub_wrong)));
        metrics.add(new ActivityMetric(R.drawable.ic_target_graphic,
                R.color.report_chart_4,
                context.getString(R.string.report_metric_new_level_one),
                Integer.toString(newLevelOneCount),
                context.getString(R.string.report_metric_sub_new_level_one)));
        metrics.add(new ActivityMetric(R.drawable.ic_wordle_graphic,
                R.color.report_chart_6,
                context.getString(R.string.report_metric_wordle_completed),
                Integer.toString(wordleCompletedCount),
                context.getString(R.string.report_metric_sub_wordle)));
        metrics.add(new ActivityMetric(R.drawable.ic_wordle_graphic,
                R.color.report_chart_7,
                context.getString(R.string.report_metric_wordle_won),
                Integer.toString(wordleWonCount),
                context.getString(R.string.report_metric_sub_wordle_won)));
        metrics.add(new ActivityMetric(R.drawable.ic_lightbulb,
                R.color.report_chart_5,
                context.getString(R.string.report_metric_ai_story),
                Integer.toString(aiStoryCount),
                context.getString(R.string.report_metric_sub_ai_story)));
        boolean showAverageMetric = period == PERIOD_WEEKLY || period == PERIOD_MONTHLY;
        metrics.add(new ActivityMetric(R.drawable.ic_activity_graphic,
                R.color.report_chart_8,
                context.getString(showAverageMetric
                        ? R.string.report_metric_average_activity
                        : R.string.report_metric_total_activity),
                showAverageMetric
                        ? String.format(Locale.getDefault(), "%.1f", averageActivity)
                        : Integer.toString(totalActivity),
                context.getString(showAverageMetric
                        ? R.string.report_metric_sub_average_activity
                        : R.string.report_metric_sub_total)));
        return metrics;
    }

    private List<ActivityBucket> buildBuckets(Context context,
                                              int period,
                                              List<QuizResultEntity> results,
                                              List<ActivityLogEntity> logs,
                                              long startAt,
                                              long endAt,
                                              int totalActivity) {
        if (period != PERIOD_WEEKLY && period != PERIOD_MONTHLY) {
            return Collections.emptyList();
        }

        LinkedHashMap<String, BucketAccumulator> ordered = period == PERIOD_WEEKLY
                ? buildWeeklyBuckets(context)
                : buildMonthlyBuckets(context, startAt);

        for (QuizResultEntity result : results) {
            String key = period == PERIOD_WEEKLY ? weekDayKey(result.completedAt) : monthWeekKey(result.completedAt, startAt);
            BucketAccumulator bucket = ordered.get(key);
            if (bucket != null) {
                bucket.quizQuestions += result.totalQuestions;
                bucket.total += result.totalQuestions;
            }
        }

        for (ActivityLogEntity log : logs) {
            String key = period == PERIOD_WEEKLY ? weekDayKey(log.createdAt) : monthWeekKey(log.createdAt, startAt);
            BucketAccumulator bucket = ordered.get(key);
            if (bucket != null) {
                if (ActivityLogEntity.TYPE_WORDLE_COMPLETED.equals(log.type)) {
                    bucket.wordleCompleted++;
                    bucket.total++;
                } else if (ActivityLogEntity.TYPE_WORDLE_WON.equals(log.type)) {
                    bucket.wordleWon++;
                } else if (ActivityLogEntity.TYPE_AI_STORY.equals(log.type)) {
                    bucket.aiStory++;
                    bucket.total++;
                } else if (ActivityLogEntity.TYPE_NEW_LEVEL_ONE.equals(log.type)) {
                    bucket.newLevelOne++;
                }
            }
        }

        List<ActivityBucket> buckets = new ArrayList<>();
        int paletteIndex = 0;
        for (BucketAccumulator accumulator : ordered.values()) {
            int displayTotal = accumulator.total;
            int percent = totalActivity > 0 ? Math.round((displayTotal * 100f) / totalActivity) : 0;
            String details = "Quiz Soru " + accumulator.quizQuestions
                    + " · Wordle " + accumulator.wordleCompleted
                    + " · Kazanılan " + accumulator.wordleWon
                    + " · Hikaye " + accumulator.aiStory
                    + " · Yeni kelime " + accumulator.newLevelOne;
            buckets.add(new ActivityBucket(
                    accumulator.title,
                    context.getString(R.string.report_metric_total_activity) + " " + displayTotal,
                    details,
                    percent + "%",
                    getPaletteColor(paletteIndex++),
                    displayTotal
            ));
        }
        return buckets;
    }

    private List<ActivitySegment> buildSegments(List<ActivityBucket> buckets) {
        List<ActivitySegment> segments = new ArrayList<>();
        for (ActivityBucket bucket : buckets) {
            if (bucket.value > 0) {
                segments.add(new ActivitySegment(bucket.title, bucket.value, bucket.colorResId));
            }
        }
        return segments;
    }

    private String buildHeroSummary(Context context,
                                    int period,
                                    List<ActivityBucket> buckets,
                                    int totalActivity,
                                    int totalQuestions,
                                    int correctAnswers,
                                    int wrongAnswers,
                                    int wordleCompletedCount,
                                    int aiStoryCount) {
        if (period == PERIOD_WEEKLY) {
            int activeDays = 0;
            for (ActivityBucket bucket : buckets) {
                if (bucket.value > 0) {
                    activeDays++;
                }
            }
            return context.getString(R.string.report_weekly_insight_format, activeDays, totalActivity);
        }
        if (period == PERIOD_MONTHLY) {
            int activeWeeks = 0;
            for (ActivityBucket bucket : buckets) {
                if (bucket.value > 0) {
                    activeWeeks++;
                }
            }
            return context.getString(R.string.report_monthly_insight_format, activeWeeks, totalActivity);
        }
        if (period == PERIOD_TOTAL) {
            return context.getString(R.string.report_total_note_format, totalActivity, wordleCompletedCount, aiStoryCount);
        }
        return context.getString(R.string.report_daily_note_format, totalQuestions, correctAnswers, wrongAnswers);
    }

    private String getPeriodTitle(Context context, int period) {
        switch (period) {
            case PERIOD_WEEKLY:
                return context.getString(R.string.report_weekly_section_title);
            case PERIOD_MONTHLY:
                return context.getString(R.string.report_monthly_section_title);
            case PERIOD_TOTAL:
                return context.getString(R.string.report_total_section_title);
            default:
                return context.getString(R.string.report_daily_title);
        }
    }

    private String buildSubtitle(Context context,
                                 int period,
                                 int totalActivity,
                                 int totalQuestions,
                                 int correctAnswers,
                                 int wrongAnswers,
                                 int wordleCompletedCount,
                                 int aiStoryCount) {
        switch (period) {
            case PERIOD_WEEKLY:
                return context.getString(R.string.report_weekly_note_format, totalActivity);
            case PERIOD_MONTHLY:
                return context.getString(R.string.report_monthly_note_format, totalActivity);
            case PERIOD_TOTAL:
                return context.getString(R.string.report_total_note_format, totalActivity, wordleCompletedCount, aiStoryCount);
            default:
                return context.getString(R.string.report_daily_subtitle);
        }
    }

    private String getPeriodNote(Context context, int period, int totalActivity, int totalQuestions, int correctAnswers, int wrongAnswers, int wordleCompletedCount, int aiStoryCount) {
        switch (period) {
            case PERIOD_WEEKLY:
                return context.getString(R.string.report_weekly_note_format, totalActivity);
            case PERIOD_MONTHLY:
                return context.getString(R.string.report_monthly_note_format, totalActivity);
            case PERIOD_TOTAL:
                return context.getString(R.string.report_total_note_format, totalActivity, wordleCompletedCount, aiStoryCount);
            default:
                return context.getString(R.string.report_daily_note_format, totalQuestions, correctAnswers, wrongAnswers);
        }
    }

    private String buildInsight(Context context, int period, List<ActivityBucket> buckets, int totalActivity) {
        if (period == PERIOD_WEEKLY) {
            return context.getString(R.string.report_weekly_insight_format, countActiveBuckets(buckets), totalActivity);
        }
        if (period == PERIOD_MONTHLY) {
            return context.getString(R.string.report_monthly_insight_format, countActiveBuckets(buckets), totalActivity);
        }
        return context.getString(R.string.report_chart_center_activity) + ": " + totalActivity;
    }

    private String buildChartSummary(Context context, int period, List<ActivityBucket> buckets) {
        ActivityBucket topBucket = null;
        for (ActivityBucket bucket : buckets) {
            if (bucket.value > 0 && (topBucket == null || bucket.value > topBucket.value)) {
                topBucket = bucket;
            }
        }
        if (topBucket == null) {
            return context.getString(R.string.report_chart_empty);
        }
        if (period == PERIOD_MONTHLY) {
            return context.getString(R.string.report_chart_summary_monthly, topBucket.title);
        }
        return context.getString(R.string.report_chart_summary_weekly, topBucket.title);
    }

    private double calculateAverageActivity(int period, Range range, int totalActivity) {
        int dayCount;
        if (period == PERIOD_WEEKLY) {
            dayCount = 7;
        } else if (period == PERIOD_MONTHLY) {
            long dayDiff = TimeUnit.MILLISECONDS.toDays(range.endAt - range.startAt);
            dayCount = (int) Math.max(1L, dayDiff);
        } else {
            dayCount = 1;
        }
        return totalActivity / (double) dayCount;
    }

    private List<QuizResultEntity> filterResults(List<QuizResultEntity> allResults, long startAt, long endAt) {
        List<QuizResultEntity> results = new ArrayList<>();
        for (QuizResultEntity result : allResults) {
            if (result.completedAt >= startAt && result.completedAt < endAt) {
                results.add(result);
            }
        }
        return results;
    }

    private Map<String, Integer> groupLogCounts(List<ActivityLogEntity> logs) {
        Map<String, Integer> counts = new HashMap<>();
        for (ActivityLogEntity log : logs) {
            Integer current = counts.get(log.type);
            counts.put(log.type, current == null ? 1 : current + 1);
        }
        return counts;
    }

    private LinkedHashMap<String, BucketAccumulator> buildWeeklyBuckets(Context context) {
        LinkedHashMap<String, BucketAccumulator> buckets = new LinkedHashMap<>();
        buckets.put("MON", new BucketAccumulator("MON", context.getString(R.string.day_monday_short)));
        buckets.put("TUE", new BucketAccumulator("TUE", "Sal"));
        buckets.put("WED", new BucketAccumulator("WED", "Çar"));
        buckets.put("THU", new BucketAccumulator("THU", "Per"));
        buckets.put("FRI", new BucketAccumulator("FRI", "Cum"));
        buckets.put("SAT", new BucketAccumulator("SAT", "Cmt"));
        buckets.put("SUN", new BucketAccumulator("SUN", "Paz"));
        return buckets;
    }

    private LinkedHashMap<String, BucketAccumulator> buildMonthlyBuckets(Context context, long monthStartAt) {
        LinkedHashMap<String, BucketAccumulator> buckets = new LinkedHashMap<>();
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(monthStartAt);
        Calendar cursor = (Calendar) start.clone();
        Calendar now = Calendar.getInstance();
        int bucketIndex = 1;
        while (cursor.get(Calendar.MONTH) == start.get(Calendar.MONTH) && cursor.get(Calendar.YEAR) == start.get(Calendar.YEAR)) {
            buckets.put(String.valueOf(bucketIndex), new BucketAccumulator(String.valueOf(bucketIndex), bucketIndex + ". hafta"));
            cursor.add(Calendar.WEEK_OF_YEAR, 1);
            bucketIndex++;
            if (bucketIndex > 6) {
                break;
            }
        }
        if (buckets.isEmpty()) {
            buckets.put("1", new BucketAccumulator("1", "1. hafta"));
        }
        return buckets;
    }

    private String weekDayKey(long createdAt) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(createdAt);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return "MON";
            case Calendar.TUESDAY:
                return "TUE";
            case Calendar.WEDNESDAY:
                return "WED";
            case Calendar.THURSDAY:
                return "THU";
            case Calendar.FRIDAY:
                return "FRI";
            case Calendar.SATURDAY:
                return "SAT";
            case Calendar.SUNDAY:
            default:
                return "SUN";
        }
    }

    private String monthWeekKey(long createdAt, long monthStartAt) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(createdAt);
        Calendar monthStart = Calendar.getInstance();
        monthStart.setTimeInMillis(monthStartAt);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int week = ((dayOfMonth - 1) / 7) + 1;
        return String.valueOf(Math.max(1, week));
    }

    private Range resolveRange(int period) {
        Calendar now = Calendar.getInstance();
        clearTime(now);
        if (period == PERIOD_DAILY) {
            long start = now.getTimeInMillis();
            Calendar end = (Calendar) now.clone();
            end.add(Calendar.DAY_OF_YEAR, 1);
            return new Range(start, end.getTimeInMillis());
        }
        if (period == PERIOD_WEEKLY) {
            Calendar start = (Calendar) now.clone();
            while (start.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                start.add(Calendar.DAY_OF_YEAR, -1);
            }
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, 7);
            return new Range(start.getTimeInMillis(), end.getTimeInMillis());
        }
        if (period == PERIOD_MONTHLY) {
            Calendar start = (Calendar) now.clone();
            start.set(Calendar.DAY_OF_MONTH, 1);
            clearTime(start);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);
            return new Range(start.getTimeInMillis(), end.getTimeInMillis());
        }
        return new Range(0L, System.currentTimeMillis());
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private int countActiveBuckets(List<ActivityBucket> buckets) {
        int count = 0;
        for (ActivityBucket bucket : buckets) {
            if (bucket.value > 0) {
                count++;
            }
        }
        return count;
    }

    private int getPaletteColor(int index) {
        switch (index % 8) {
            case 0:
                return R.color.report_chart_1;
            case 1:
                return R.color.report_chart_2;
            case 2:
                return R.color.report_chart_3;
            case 3:
                return R.color.report_chart_4;
            case 4:
                return R.color.report_chart_5;
            case 5:
                return R.color.report_chart_6;
            case 6:
                return R.color.report_chart_7;
            default:
                return R.color.report_chart_8;
        }
    }

    private static class Range {
        final long startAt;
        final long endAt;

        Range(long startAt, long endAt) {
            this.startAt = startAt;
            this.endAt = endAt;
        }
    }

    private static class BucketAccumulator {
        final String key;
        final String title;
        int total;
        int quizQuestions;
        int newLevelOne;
        int wordleCompleted;
        int wordleWon;
        int aiStory;

        BucketAccumulator(String key, String title) {
            this.key = key;
            this.title = title;
        }
    }

    public static class ActivityReportData {
        public final int period;
        public final String title;
        public final String subtitle;
        public final String note;
        public final String insight;
        public final String heroSummary;
        public final String chartSummary;
        public final int totalActivity;
        public final List<ActivityMetric> metrics;
        public final List<ActivityBucket> buckets;
        public final List<ActivitySegment> segments;

        public ActivityReportData(int period,
                                  String title,
                                  String subtitle,
                                  String note,
                                  String insight,
                                  String heroSummary,
                                  String chartSummary,
                                  int totalActivity,
                                  List<ActivityMetric> metrics,
                                  List<ActivityBucket> buckets,
                                  List<ActivitySegment> segments) {
            this.period = period;
            this.title = title;
            this.subtitle = subtitle;
            this.note = note;
            this.insight = insight;
            this.heroSummary = heroSummary;
            this.chartSummary = chartSummary;
            this.totalActivity = totalActivity;
            this.metrics = metrics;
            this.buckets = buckets;
            this.segments = segments;
        }
    }

    public static class ActivityMetric {
        public final int iconResId;
        public final int accentColorResId;
        public final String label;
        public final String value;
        public final String subtitle;

        public ActivityMetric(int iconResId, int accentColorResId, String label, String value, String subtitle) {
            this.iconResId = iconResId;
            this.accentColorResId = accentColorResId;
            this.label = label;
            this.value = value;
            this.subtitle = subtitle;
        }
    }

    public static class ActivityBucket {
        public final String title;
        public final String meta;
        public final String details;
        public final String percent;
        public final int colorResId;
        public final int value;

        public ActivityBucket(String title, String meta, String details, String percent, int colorResId, int value) {
            this.title = title;
            this.meta = meta;
            this.details = details;
            this.percent = percent;
            this.colorResId = colorResId;
            this.value = value;
        }
    }

    public static class ActivitySegment {
        public final String label;
        public final int value;
        public final int colorResId;

        public ActivitySegment(String label, int value, int colorResId) {
            this.label = label;
            this.value = value;
            this.colorResId = colorResId;
        }
    }
}
