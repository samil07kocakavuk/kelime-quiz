package com.samil.kelimequiz.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.samil.kelimequiz.R;
import com.samil.kelimequiz.data.local.entity.ActivityLogEntity;
import com.samil.kelimequiz.data.local.entity.QuizResultEntity;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryActivityLogDao;
import com.samil.kelimequiz.testsupport.TestDoubles.InMemoryQuizResultDao;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Locale;

public class ActivityReportRepositoryTest {
    private InMemoryQuizResultDao quizResultDao;
    private InMemoryActivityLogDao activityLogDao;
    private ActivityReportRepository repository;
    private ReportTextResolver strings;

    @Before
    public void setUp() {
        quizResultDao = new InMemoryQuizResultDao();
        activityLogDao = new InMemoryActivityLogDao();
        repository = new ActivityReportRepository(quizResultDao, activityLogDao);
        strings = new ReportTextResolver();
    }

    @Test
    public void buildDailyReportUsesTodayDataAndKeepsChartsEmpty() {
        int userId = 7;
        long today = todayAt(10);

        quizResultDao.insert(quizResult(userId, 8, 5, today));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_COMPLETED, today + 60_000L));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_AI_STORY, today + 120_000L));

        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, ActivityReportRepository.PERIOD_DAILY);

        assertEquals("Daily Activity", report.title);
        assertEquals(10, report.totalActivity);
        assertTrue(report.note.contains("8 quiz"));
        assertTrue(report.heroSummary.contains("5"));
        assertTrue(report.chartSummary.contains("No chart data"));
        assertTrue(report.buckets.isEmpty());
        assertTrue(report.segments.isEmpty());
        assertEquals(8, report.metrics.size());
    }

    @Test
    public void buildWeeklyReportAggregatesBucketsAndSummary() {
        int userId = 11;
        long monday = currentWeekMondayAt(10);
        long tuesday = monday + dayMs();

        quizResultDao.insert(quizResult(userId, 6, 4, monday));
        quizResultDao.insert(quizResult(userId, 2, 1, tuesday));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_COMPLETED, monday + 60_000L));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_WON, monday + 120_000L));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_AI_STORY, tuesday + 60_000L));

        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, ActivityReportRepository.PERIOD_WEEKLY);

        assertEquals("Weekly Summary", report.title);
        assertEquals(10, report.totalActivity);
        assertTrue(report.subtitle.contains("activities"));
        assertTrue(report.note.contains("week"));
        assertTrue(report.insight.contains("2"));
        assertTrue(report.chartSummary.contains("day"));
        assertTrue(report.buckets.size() >= 2);
        assertTrue(report.segments.size() >= 2);
    }

    @Test
    public void buildMonthlyReportAggregatesWeeksAndSummary() {
        int userId = 13;
        long monthStart = currentMonthStartAt(10);
        long weekOne = monthStart + dayMs();
        long weekTwo = monthStart + (8L * dayMs());

        quizResultDao.insert(quizResult(userId, 5, 3, weekOne));
        quizResultDao.insert(quizResult(userId, 7, 6, weekTwo));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_COMPLETED, weekOne + 60_000L));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_AI_STORY, weekTwo + 60_000L));

        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, ActivityReportRepository.PERIOD_MONTHLY);

        assertEquals("Monthly Summary", report.title);
        assertEquals(14, report.totalActivity);
        assertTrue(report.subtitle.contains("activities"));
        assertTrue(report.note.contains("month"));
        assertTrue(report.insight.contains("weeks"));
        assertTrue(report.chartSummary.contains("week"));
        assertTrue(report.buckets.size() >= 2);
        assertTrue(report.segments.size() >= 2);
    }

    @Test
    public void buildTotalReportUsesTotalSegmentsAndTopLabel() {
        int userId = 17;
        long past = currentMonthStartAt(10);

        quizResultDao.insert(quizResult(userId, 9, 7, past));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_COMPLETED, past + 60_000L));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_AI_STORY, past + 120_000L));

        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, ActivityReportRepository.PERIOD_TOTAL);

        assertEquals("Total Summary", report.title);
        assertEquals(11, report.totalActivity);
        assertTrue(report.note.contains("Total 11 activities"));
        assertTrue(report.heroSummary.contains("Wordle"));
        assertTrue(report.chartSummary.contains("Top share"));
        assertTrue(report.segments.size() >= 2);
        assertTrue(report.buckets.isEmpty());
    }

    @Test
    public void totalReportWithoutSegmentsUsesEmptySummary() {
        int userId = 23;
        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, ActivityReportRepository.PERIOD_TOTAL);

        assertEquals("Total Summary", report.title);
        assertEquals("No chart data.", report.chartSummary);
        assertTrue(report.segments.isEmpty());
    }

    @Test
    public void weeklyReportCoversAllWeekdaysAndPaletteTail() throws Exception {
        int userId = 29;
        long monday = currentWeekMondayAt(10);

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            long dayAt = monday + (dayIndex * dayMs());
            quizResultDao.insert(quizResult(userId, dayIndex + 1, dayIndex, dayAt));
            activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_COMPLETED, dayAt + 60_000L));
        }
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_NEW_LEVEL_ONE, monday + 2 * dayMs() + 120_000L));

        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, ActivityReportRepository.PERIOD_WEEKLY);

        assertEquals("Weekly Summary", report.title);
        assertTrue(report.buckets.size() >= 7);
        assertTrue(report.segments.size() >= 7);
        assertTrue(report.chartSummary.contains("day"));
        assertEquals(R.color.report_chart_8, invokePaletteColor(7));
    }

    @Test
    public void invalidPeriodFallsBackToDailyBehavior() {
        int userId = 31;
        long now = todayAt(10);

        quizResultDao.insert(quizResult(userId, 3, 2, now));
        activityLogDao.insert(activityLog(userId, ActivityLogEntity.TYPE_WORDLE_COMPLETED, now + 60_000L));

        ActivityReportRepository.ActivityReportData report = repository.buildReport(strings, userId, 999);

        assertEquals("Daily Activity", report.title);
        assertTrue(report.note.contains("quiz"));
        assertTrue(report.heroSummary.contains("quiz"));
        assertEquals("No chart data.", report.chartSummary);
        assertTrue(report.buckets.isEmpty());
        assertTrue(report.segments.isEmpty());
    }

    private QuizResultEntity quizResult(int userId, int totalQuestions, int correctAnswers, long completedAt) {
        QuizResultEntity result = new QuizResultEntity();
        result.userId = userId;
        result.totalQuestions = totalQuestions;
        result.correctAnswers = correctAnswers;
        result.successRate = totalQuestions == 0 ? 0 : (correctAnswers * 100.0) / totalQuestions;
        result.completedAt = completedAt;
        return result;
    }

    private ActivityLogEntity activityLog(int userId, String type, long createdAt) {
        ActivityLogEntity log = new ActivityLogEntity();
        log.userId = userId;
        log.type = type;
        log.createdAt = createdAt;
        return log;
    }

    private long todayAt(int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long currentWeekMondayAt(int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return cal.getTimeInMillis();
    }

    private long currentMonthStartAt(int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long dayMs() {
        return 24L * 60L * 60L * 1000L;
    }

    private int invokePaletteColor(int index) throws Exception {
        Method method = ActivityReportRepository.class.getDeclaredMethod("getPaletteColor", int.class);
        method.setAccessible(true);
        return (Integer) method.invoke(repository, index);
    }

    private static final class ReportTextResolver implements ActivityReportRepository.TextResolver {
        @Override
        public String getString(int resId, Object... formatArgs) {
            String template;
            if (resId == R.string.report_daily_title) {
                template = "Daily Activity";
            } else if (resId == R.string.report_weekly_section_title) {
                template = "Weekly Summary";
            } else if (resId == R.string.report_monthly_section_title) {
                template = "Monthly Summary";
            } else if (resId == R.string.report_total_section_title) {
                template = "Total Summary";
            } else if (resId == R.string.report_daily_subtitle) {
                template = "Today quiz, Wordle, story and word progress.";
            } else if (resId == R.string.report_daily_note_format) {
                template = "Today you solved %1$d quiz questions, got %2$d right and %3$d wrong.";
            } else if (resId == R.string.report_weekly_note_format) {
                template = "This week %1$d activities were recorded.";
            } else if (resId == R.string.report_monthly_note_format) {
                template = "This month %1$d activities were recorded.";
            } else if (resId == R.string.report_total_note_format) {
                template = "Total %1$d activities, %2$d Wordle and %3$d AI story.";
            } else if (resId == R.string.report_weekly_insight_format) {
                template = "This week you were active %1$d days, total %2$d activities.";
            } else if (resId == R.string.report_monthly_insight_format) {
                template = "This month you were active %1$d weeks, total %2$d activities.";
            } else if (resId == R.string.report_chart_center_activity) {
                template = "total activity";
            } else if (resId == R.string.report_chart_empty) {
                template = "No chart data.";
            } else if (resId == R.string.report_chart_summary_weekly) {
                template = "This week most active day was %1$s.";
            } else if (resId == R.string.report_chart_summary_monthly) {
                template = "This month most active week was %1$s.";
            } else if (resId == R.string.report_total_chart_summary) {
                template = "Top share was %1$s.";
            } else if (resId == R.string.report_metric_total_activity) {
                template = "Total Activity";
            } else if (resId == R.string.report_metric_quiz_questions) {
                template = "Quiz Questions";
            } else if (resId == R.string.report_metric_wordle_completed) {
                template = "Wordle Completed";
            } else if (resId == R.string.report_metric_ai_story) {
                template = "AI Story";
            } else if (resId == R.string.report_metric_average_activity) {
                template = "Average Activity";
            } else if (resId == R.string.report_metric_quiz_correct) {
                template = "Correct";
            } else if (resId == R.string.report_metric_quiz_wrong) {
                template = "Wrong";
            } else if (resId == R.string.report_metric_new_level_one) {
                template = "New Level One";
            } else if (resId == R.string.report_metric_wordle_won) {
                template = "Wordle Won";
            } else if (resId == R.string.report_metric_sub_quiz) {
                template = "Total quiz questions";
            } else if (resId == R.string.report_metric_sub_correct) {
                template = "Correct answers";
            } else if (resId == R.string.report_metric_sub_wrong) {
                template = "Wrong answers";
            } else if (resId == R.string.report_metric_sub_new_level_one) {
                template = "Level one words";
            } else if (resId == R.string.report_metric_sub_wordle) {
                template = "Wordle games";
            } else if (resId == R.string.report_metric_sub_wordle_won) {
                template = "Won Wordle";
            } else if (resId == R.string.report_metric_sub_ai_story) {
                template = "Story generation";
            } else if (resId == R.string.report_metric_sub_total) {
                template = "Total activity";
            } else if (resId == R.string.report_metric_sub_average_activity) {
                template = "Daily average";
            } else if (resId == R.string.day_monday_short) {
                template = "Mon";
            } else {
                template = "res-" + resId;
            }
            if (formatArgs == null || formatArgs.length == 0) {
                return template;
            }
            return String.format(Locale.ROOT, template, formatArgs);
        }
    }
}
