package com.samil.kelimequiz.domain.service;

import com.samil.kelimequiz.data.local.entity.WordWithLevel;
import com.samil.kelimequiz.domain.model.WordLevel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WordReportHtmlBuilder {
    public String build(List<WordWithLevel> words) {
        List<WordWithLevel> sortedWords = words == null ? Collections.emptyList() : words;
        sortedWords = new java.util.ArrayList<>(sortedWords);
        sortedWords.sort(Comparator
                .comparingInt((WordWithLevel word) -> word == null ? 0 : word.level)
                .reversed()
                .thenComparing(word -> safeText(word == null ? null : word.word == null ? null : word.word.cefrLevel))
                .thenComparing(word -> safeText(word == null || word.word == null ? null : word.word.engWord)));

        int learnedCount = 0;
        int inProgressCount = 0;
        int notStartedCount = 0;
        for (WordWithLevel word : sortedWords) {
            if (word == null) {
                continue;
            }
            if (word.level >= 6) {
                learnedCount++;
            } else if (word.level == 0) {
                notStartedCount++;
            } else {
                inProgressCount++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='UTF-8'><style>")
                .append("body{font-family:Arial,sans-serif;padding:24px;background:#ffffff;color:#0f172a}")
                .append("h1{color:#0f172a;font-size:24px;margin:0 0 8px}")
                .append("p.meta{color:#475569;font-size:13px;line-height:1.6;margin:0 0 14px}")
                .append(".badge{display:inline-block;padding:5px 11px;border-radius:999px;margin-right:6px;margin-bottom:6px;background:#eef2ff;color:#1e293b;font-size:12px;font-weight:bold}")
                .append(".summary{margin:14px 0 20px}")
                .append(".summary-item{display:inline-block;margin-right:18px;font-size:13px;color:#334155}")
                .append("table{width:100%;border-collapse:collapse;margin-top:8px}")
                .append("th,td{border:1px solid #dbe2ea;padding:8px 10px;text-align:left;font-size:13px;vertical-align:top}")
                .append("th{background:#f8fafc;color:#0f172a}")
                .append("tbody tr:nth-child(even){background:#f8fafc}")
                .append(".level{font-weight:bold}")
                .append("</style></head><body>")
                .append("<h1>Kelime Havuzu Analiz Raporu</h1>")
                .append("<p class='meta'>Tüm kelimeler öğrenilme seviyesine göre yüksekten düşüğe sıralanır. Aynı seviyedekilerde önce dil seviyesi, sonra İngilizce kelime adı dikkate alınır.</p>")
                .append("<div class='summary'>")
                .append("<span class='summary-item'>Toplam: ").append(sortedWords.size()).append("</span>")
                .append("<span class='summary-item'>Öğrenilmiş: ").append(learnedCount).append("</span>")
                .append("<span class='summary-item'>Devam Eden: ").append(inProgressCount).append("</span>")
                .append("<span class='summary-item'>Başlanmamış: ").append(notStartedCount).append("</span>")
                .append("</div>")
                .append("<p>")
                .append("<span class='badge'>A1: ").append(countByCefr(sortedWords, WordLevel.A1.name())).append("</span>")
                .append("<span class='badge'>A2: ").append(countByCefr(sortedWords, WordLevel.A2.name())).append("</span>")
                .append("<span class='badge'>B1: ").append(countByCefr(sortedWords, WordLevel.B1.name())).append("</span>")
                .append("<span class='badge'>B2: ").append(countByCefr(sortedWords, WordLevel.B2.name())).append("</span>")
                .append("<span class='badge'>C1: ").append(countByCefr(sortedWords, WordLevel.C1.name())).append("</span>")
                .append("<span class='badge'>C2: ").append(countByCefr(sortedWords, WordLevel.C2.name())).append("</span>")
                .append("</p>");

        if (sortedWords.isEmpty()) {
            sb.append("<p>Gösterilecek kelime bulunmuyor.</p>");
        } else {
            appendTable(sb, sortedWords);
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private int countByCefr(List<WordWithLevel> words, String cefrLevel) {
        int count = 0;
        for (WordWithLevel word : words) {
            if (word != null
                    && word.word != null
                    && cefrLevel.equalsIgnoreCase(word.word.cefrLevel == null ? WordLevel.A1.name() : word.word.cefrLevel)) {
                count++;
            }
        }
        return count;
    }

    private void appendTable(StringBuilder sb, List<WordWithLevel> words) {
        sb.append("<table><thead><tr><th>#</th><th>İngilizce</th><th>Türkçe</th><th>Kategori</th><th>Dil Seviyesi</th><th>Öğrenilme Seviyesi</th></tr></thead><tbody>");
        int index = 1;
        for (WordWithLevel word : words) {
            if (word == null || word.word == null) {
                continue;
            }

            sb.append("<tr><td>").append(index++).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.engWord)).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.trWord)).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.category != null ? word.word.category : "-")).append("</td>")
                    .append("<td>").append(escapeHtml(word.word.cefrLevel != null ? word.word.cefrLevel : WordLevel.A1.name())).append("</td>")
                    .append("<td class='level'>").append(word.level).append("/6</td></tr>");
        }
        sb.append("</tbody></table>");
    }

    private String safeText(String value) {
        return value == null ? "" : value;
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
