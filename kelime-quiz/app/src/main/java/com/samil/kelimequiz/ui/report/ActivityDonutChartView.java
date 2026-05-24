package com.samil.kelimequiz.ui.report;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.samil.kelimequiz.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityDonutChartView extends View {
    public static class Segment {
        public final String label;
        public final int value;
        @ColorInt
        public final int color;

        public Segment(String label, int value, @ColorInt int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private final List<Segment> segments = new ArrayList<>();

    private String centerValue = "0";
    private String centerLabel = "";
    private String emptyText = "";

    public ActivityDonutChartView(Context context) {
        super(context);
        init();
    }

    public ActivityDonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActivityDonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(dp(26));
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.card_stroke));

        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.ROUND);
        segmentPaint.setStrokeWidth(dp(26));

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(28));
        textPaint.setFakeBoldText(true);

        subTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextSize(sp(12));
    }

    public void setSegments(List<Segment> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
        invalidate();
    }

    public void setCenterValue(String centerValue) {
        this.centerValue = centerValue != null ? centerValue : "0";
        invalidate();
    }

    public void setCenterLabel(String centerLabel) {
        this.centerLabel = centerLabel != null ? centerLabel : "";
        invalidate();
    }

    public void setEmptyText(String emptyText) {
        this.emptyText = emptyText != null ? emptyText : "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float inset = dp(24);
        arcBounds.set(
                (width - size) / 2f + inset,
                (height - size) / 2f + inset,
                (width + size) / 2f - inset,
                (height + size) / 2f - inset
        );

        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);

        int total = 0;
        for (Segment segment : segments) {
            total += Math.max(0, segment.value);
        }

        if (total > 0) {
            float startAngle = -90f;
            for (Segment segment : segments) {
                if (segment.value <= 0) continue;
                float sweepAngle = (segment.value * 360f) / total;
                segmentPaint.setColor(segment.color);
                canvas.drawArc(arcBounds, startAngle, sweepAngle, false, segmentPaint);
                startAngle += sweepAngle;
            }
        }

        float centerX = width / 2f;
        float centerY = height / 2f;
        float valueBaseline = centerY - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(centerValue, centerX, valueBaseline, textPaint);

        float labelBaseline = centerY + dp(22);
        canvas.drawText(total > 0 ? centerLabel : emptyText, centerX, labelBaseline, subTextPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
