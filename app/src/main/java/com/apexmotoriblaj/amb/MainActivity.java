package com.apexmotoriblaj.amb;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;
import android.widget.Toast;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import org.json.JSONArray;
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
    private String currentAppearance="";
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
        int[] widgets=AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this,NewsWidget.class));
        if(widgets.length>0)WidgetNewsSync.refreshAsync(getApplicationContext(), false);
    }

    /** Apply WebView theme to Android system bars, including AMOLED true black. */
    private void setSystemAppearance(String theme) {
        if(theme.equals(currentAppearance))return;
        currentAppearance=theme;
        getSharedPreferences("amb_shared",MODE_PRIVATE).edit().putString("appearance",theme).apply();
        boolean amoled="amoled".equals(theme);
        boolean dark=amoled || "dark".equals(theme);
        int color=amoled ? Color.BLACK : dark ? Color.rgb(25,35,46) : Color.rgb(246,248,250);
        getWindow().setStatusBarColor(color);
        getWindow().setNavigationBarColor(color);
        getWindow().getDecorView().setSystemUiVisibility(dark ? 0 :
                (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));
        if(web!=null)web.setBackgroundColor(color);
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
            android.content.SharedPreferences prefs=getSharedPreferences("amb_shared", MODE_PRIVATE);
            if(data.equals(prefs.getString("widget_news","")))return;
            long now=System.currentTimeMillis();
            boolean prefetch=now-prefs.getLong("image_prefetch_at",0)>90000;
            prefs.edit().putString("widget_news",data).putLong("image_prefetch_at",prefetch?now:prefs.getLong("image_prefetch_at",0)).apply();
            NewsWidget.notifyData(getApplicationContext());
            if(prefetch)WidgetNewsSync.prefetchWidgetImagesAsync(getApplicationContext());
        }
        @JavascriptInterface public void setWidgetSources(String json) {
            if (json == null || json.length() > 35_000) return;
            getSharedPreferences("amb_shared", MODE_PRIVATE).edit().putString("sources", json).apply();
        }
        @JavascriptInterface public void setAppearance(String theme) {
            if(theme==null || !(theme.equals("light") || theme.equals("dark") || theme.equals("amoled")))return;
            runOnUiThread(() -> setSystemAppearance(theme));
        }
        @JavascriptInterface public void openWidgetSettings() {
            runOnUiThread(() -> startActivity(new Intent(MainActivity.this, WidgetConfigActivity.class)));
        }
        @JavascriptInterface public void openArticle(String url) {
            if(url==null||!url.startsWith("https://"))return;
            runOnUiThread(()->startActivity(new Intent(MainActivity.this,ArticleActivity.class).putExtra("link",url)));
        }
        @JavascriptInterface public void translateText(String requestId,String value) {
            if(requestId==null||requestId.length()>80||value==null||value.length()>5500)return;
            TranslateManager.translate(value,(translated,error)-> {
                String response="window.AMBTranslationResult("+JSONObject.quote(requestId)+","+
                    (translated==null?"null":JSONObject.quote(translated))+","+
                    (error==null?"null":JSONObject.quote(error))+")";
                runOnUiThread(()->{if(web!=null)web.evaluateJavascript(response,null);});
            });
        }
        @JavascriptInterface public void saveOffline(String json) {
            if(json==null||json.length()>18000)return;
            io.execute(()->{
                String link="",error=null;boolean ok=false;
                try{
                    JSONObject item=new JSONObject(json);link=item.getString("link");
                    if(!OfflineStore.exists(getApplicationContext(),link))OfflineStore.save(getApplicationContext(),item);
                    ok=OfflineStore.exists(getApplicationContext(),link);
                    if(ok&&"en".equals(item.optString("lang"))){
                        final String savedLink=link;
                        String text=OfflineStore.plainText(getApplicationContext(),savedLink);
                        if(text!=null && OfflineStore.translatedPath(getApplicationContext(),savedLink)==null){
                            TranslateManager.translateLong(text,(translation,reason)->{
                                if(reason==null&&translation!=null)try{OfflineStore.saveTranslated(getApplicationContext(),savedLink,translation);}catch(Exception ignored){}
                            });
                        }
                    }
                }catch(Exception e){error=e.getMessage()==null?"Salvataggio impossibile":e.getMessage();}
                final String target=link,reason=error;final boolean saved=ok;
                String code="window.AMBOfflineResult("+JSONObject.quote(target)+","+saved+","+(reason==null?"null":JSONObject.quote(reason))+")";
                runOnUiThread(()->{if(web!=null)web.evaluateJavascript(code,null);});
            });
        }
        @JavascriptInterface public void deleteOffline(String link){
            if(link!=null&&link.startsWith("https://"))io.execute(()->OfflineStore.delete(getApplicationContext(),link));
        }
        @JavascriptInterface public void requestNotifications(){
            runOnUiThread(()->{
                NotificationScheduler.enable(getApplicationContext());
                if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},84);
            });
        }
        @JavascriptInterface public void setCalendarData(String json){
            if(json==null||json.length()>300_000)return;
            getSharedPreferences("amb_shared",MODE_PRIVATE).edit().putString("calendar_events",json).apply();CalendarWidget.refresh(getApplicationContext());
        }
        /** Opens the device's calendar editor; no calendar write permission or silent insertion. */
        @JavascriptInterface public void addCalendarEvent(String json) {
            if(json==null||json.length()>4000)return;
            try{
                JSONObject e=new JSONObject(json);
                String title=e.optString("name","Evento motorsport");
                long start=e.optLong("date",0);
                if(start<946684800000L||title.length()>250)return;
                long end=e.optLong("end",start+3600000L);
                if(end<=start)end=start+3600000L;
                Intent intent=new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE,title)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION,e.optString("place",""))
                    .putExtra(CalendarContract.Events.DESCRIPTION,e.optString("desc",""))
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,start)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,end)
                    .putExtra(CalendarContract.Events.ALL_DAY,e.optBoolean("allDay",false));
                runOnUiThread(()->{try{startActivity(intent);}catch(Exception ex){Toast.makeText(MainActivity.this,"Nessun calendario disponibile",Toast.LENGTH_SHORT).show();}});
            }catch(Exception ignored){}
        }
        @JavascriptInterface public void downloadAuthorizedVideo(String link,String network){
            if(link==null||link.length()>3000||!("wifi".equals(network)||"all".equals(network)))return;
            try{
                Uri uri=Uri.parse(link);
                if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null)return;
                String path=uri.getPath();if(path==null||!path.toLowerCase(java.util.Locale.ROOT).endsWith(".mp4"))return;
                android.app.DownloadManager.Request request=new android.app.DownloadManager.Request(uri);
                request.setTitle("A.M.B. · Video autorizzato");
                request.setMimeType("video/mp4");
                request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setAllowedOverMetered("all".equals(network));
                request.setAllowedOverRoaming(false);
                request.setDestinationInExternalFilesDir(MainActivity.this,android.os.Environment.DIRECTORY_MOVIES,"AMB_"+System.currentTimeMillis()+".mp4");
                android.app.DownloadManager manager=(android.app.DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                if(manager!=null)manager.enqueue(request);
            }catch(Exception ignored){}
        }
        @JavascriptInterface public void enableDailyBrief(){runOnUiThread(()->{NotificationScheduler.enable(getApplicationContext());DailyBriefReceiver.schedule(getApplicationContext());});}
        @JavascriptInterface public void updateWidget() { WidgetNewsSync.refreshAsync(getApplicationContext(), true); }

    }

    @Override public void onDestroy() {
        if (web != null) { web.removeJavascriptInterface("AMBAndroid"); web.destroy(); web = null; }
        io.shutdownNow(); super.onDestroy();
    }
}
