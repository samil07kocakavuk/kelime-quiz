package com.samil.kelimequiz.domain.service;

import com.samil.kelimequiz.data.local.entity.WordWithLevel;
import com.samil.kelimequiz.domain.model.WordLevel;

import java.util.ArrayList;
import java.util.List;

public class WordReportHtmlBuilder {
    public String build(List<WordWithLevel> words) {
        List<WordWithLevel> learnedList = new ArrayList<>();
        List<WordWithLevel> inProgressList = new ArrayList<>();
        List<WordWithLevel> notStartedList = new ArrayList<>();

        for (WordWithLevel word : words) {
            if (word.level >= 6) {
                learnedList.add(word);
            } else if (word.level == 0) {
                notStartedList.add(word);
            } else {
                inProgressList.add(word);
            }
        }
        inProgressList.sort((a, b) -> Integer.compare(b.level, a.level));

        int a1Count = countByCefr(words, WordLevel.A1.name());
        int a2Count = countByCefr(words, WordLevel.A2.name());
        int b1Count = countByCefr(words, WordLevel.B1.name());
        int b2Count = countByCefr(words, WordLevel.B2.name());
        int c1Count = countByCefr(words, WordLevel.C1.name());
        int c2Count = countByCefr(words, WordLevel.C2.name());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='UTF-8'><style>")
                .append("body{font-family:sans-serif;padding:24px}")
                .append("h1{color:#333;font-size:20px}")
                .append("h2{color:#555;font-size:16px;margin-top:20px;border-bottom:1px solid #ddd;padding-bottom:4px}")
                .append(".badge{display:inline-block;padding:4px 10px;border-radius:999px;margin-right:6px;margin-bottom:6px;background:#f1f5f9;color:#0f172a;font-size:12px;font-weight:bold}")
                .append("table{width:100%;border-collapse:collapse;margin-top:8px}")
                .append("th,td{border:1px solid #ddd;padding:6px 10px;text-align:left;font-size:13px}")
                .append("th{background:#f5f5f5}")
                .append("</style></head><body>")
                .append("<h1>Kelime Quiz - Kişisel Analiz Raporu</h1>")
                .append("<p>Toplam: ").append(words.size())
                .append(" | Öğrenilmiş: ").append(learnedList.size())
                .append(" | Devam Eden: ").append(inProgressList.size())
                .append(" | Başlanmamış: ").append(notStartedList.size()).append("</p>")
                .append("<p>")
                .append("<span class='badge'>A1: ").append(a1Count).append("</span>")
                .append("<span class='badge'>A2: ").append(a2Count).append("</span>")
                .append("<span class='badge'>B1: ").append(b1Count).append("</span>")
                .append("<span class='badge'>B2: ").append(b2Count).append("</span>")
                .append("<span class='badge'>C1: ").append(c1Count).append("</span>")
                .append("<span class='badge'>C2: ").append(c2Count).append("</span>")
                .append("</p>");

        if (!learnedList.isEmpty()) {
            sb.append("<h2>✓ Öğrenilmiş Kelimeler (").append(learnedList.size()).append(")</h2>");
            appendTable(sb, learnedList);
        }
        if (!inProgressList.isEmpty()) {
            sb.append("<h2>↻ Öğrenilmekte Olan (").append(inProgressList.size()).append(")</h2>");
            appendTable(sb, inProgressList);
        }
        if (!notStartedList.isEmpty()) {
            sb.append("<h2>○ Başlanmamış (").append(notStartedList.size()).append(")</h2>");
            appendTable(sb, notStartedList);
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private int countByCefr(List<WordWithLevel> words, String cefrLevel) {
        int count = 0;
        for (WordWithLevel word : words) {
            if (cefrLevel.equalsIgnoreCase(word.word.cefrLevel == null ? WordLevel.A1.name() : word.word.cefrLevel)) {
                count++;
            }
        }
        return count;
    }

    private void appendTable(StringBuilder sb, List<WordWithLevel> words) {
        sb.append("<table><tr><th>İngilizce</th><th>Türkçe</th><th>Kategori</th><th>CEFR</th><th>Quiz Seviyesi</th></tr>");
        for (WordWithLevel word : words) {
            sb.append("<tr><td>").append(escapeHtml(word.word.engWord)).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.trWord)).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.category != null ? word.word.category : "-")).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.cefrLevel != null ? word.word.cefrLevel : "-")).append("</td>")
                    .append("<td>").append(word.level).append("/6</td></tr>");
        }
        sb.append("</table>");
    }

    private String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }
}
