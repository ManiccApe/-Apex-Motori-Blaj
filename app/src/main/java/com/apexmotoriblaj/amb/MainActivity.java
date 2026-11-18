package com.apexmotoriblaj.amb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** A.M.B. Android shell. No third-party dependency or backend required. */
public final class MainActivity extends Activity {
    private WebView web;
    private final ExecutorService io = Executors.newFixedThreadPool(4);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(246,248,250));
        getWindow().setNavigationBarColor(Color.rgb(246,248,250));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        web = new WebView(this);
        WebSettings cfg = web.getSettings();
        cfg.setJavaScriptEnabled(true);
        cfg.setDomStorageEnabled(true);
        cfg.setAllowFileAccess(true);
        cfg.setAllowFileAccessFromFileURLs(false);
        cfg.setAllowUniversalAccessFromFileURLs(false);
        cfg.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        cfg.setMediaPlaybackRequiresUserGesture(true);
        cfg.setDefaultTextEncodingName("UTF-8");
        web.setBackgroundColor(Color.rgb(246,248,250));
        web.addJavascriptInterface(new Bridge(), "AMBAndroid");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            private boolean allow(String s) {
                if (s.startsWith("file:///android_asset/")) return false;
                if (s.startsWith("https://") || s.startsWith("http://")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(s))); } catch (Exception ignored) { }
                }
                return true;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) { return allow(req.getUrl().toString()); }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return allow(url); }
        });
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        WidgetNewsSync.refreshAsync(getApplicationContext(), false);
    }

    private final class Bridge {
        @JavascriptInterface public void fetch(String requestId, String url) {
            if (requestId == null || requestId.length() > 100 || url == null) return;
            io.execute(() -> {
                String data = null, error = null;
                try { data = WidgetNewsSync.readHttps(url, 3_000_000); }
                catch (Exception e) { error = (e.getMessage() == null ? "Fonte non disponibile" : e.getMessage()); }
                final String result = "window.AMBBridgeResult(" + JSONObject.quote(requestId) + "," +
                    (data == null ? "null" : JSONObject.quote(data)) + "," +
                    (error == null ? "null" : JSONObject.quote(error)) + ")";
                runOnUiThread(() -> { if (web != null) web.evaluateJavascript(result, null); });
            });
        }
        @JavascriptInterface public void setWidgetData(String data) {
            if (data == null || data.length() > 450_000) return;
            getSharedPreferences("amb_shared", MODE_PRIVATE).edit().putString("widget_news", data).apply();
            NewsWidget.notifyData(getApplicationContext());
        }
        @JavascriptInterface public void setWidgetSources(String json) {
            if (json == null || json.length() > 35_000) return;
            getSharedPreferences("amb_shared", MODE_PRIVATE).edit().putString("sources", json).apply();
        }
        @JavascriptInterface public void openWidgetSettings() {
            runOnUiThread(() -> startActivity(new Intent(MainActivity.this, WidgetConfigActivity.class)));
        }
        @JavascriptInterface public void updateWidget() { WidgetNewsSync.refreshAsync(getApplicationContext(), true); }
    }

    @Override public void onDestroy() {
        if (web != null) { web.removeJavascriptInterface("AMBAndroid"); web.destroy(); web = null; }
        io.shutdownNow(); super.onDestroy();
    }
}
