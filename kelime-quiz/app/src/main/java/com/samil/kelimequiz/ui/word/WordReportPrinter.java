package com.samil.kelimequiz.ui.word;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WordReportPrinter {
    private final Context context;

    public WordReportPrinter(Context context) {
        this.context = context;
    }

    public void print(String html) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                String jobName = "Kelime Quiz Raporu";
                if (printManager != null) {
                    printManager.print(
                            jobName,
                            view.createPrintDocumentAdapter(jobName),
                            new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
                    );
                }
            }
        });
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
