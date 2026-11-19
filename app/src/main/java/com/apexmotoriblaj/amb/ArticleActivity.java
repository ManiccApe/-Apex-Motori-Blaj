package com.apexmotoriblaj.amb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Internal ad-filtered reader with automatic offline preference and optional EN->IT translation. */
public final class ArticleActivity extends Activity {
    private WebView browser;
    private String originalUrl;
    private boolean local;
    private String readerCache;
    private int requestToken=0;
    private Button readerButton;
    private View amberOverlay;
    private boolean warmReading=false;
    private final ExecutorService background=Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        originalUrl=getIntent().getStringExtra("link");
        if(originalUrl==null||!originalUrl.startsWith("https://")){finish();return;}
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.BLACK);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(dp(3),dp(5),dp(3),dp(5));nav.setBackgroundColor(Color.rgb(17,17,17));
        Button back=button("‹");back.setOnClickListener(v->finish());nav.addView(back,new LinearLayout.LayoutParams(dp(43),dp(47)));
        TextView name=new TextView(this);name.setText("A.M.B.");name.setTextSize(14);name.setTextColor(Color.WHITE);name.setGravity(17);
        nav.addView(name,new LinearLayout.LayoutParams(0,dp(47),1));
        readerButton=button("Lettura");readerButton.setContentDescription("Attiva tonalità calda e luminosità ridotta senza nascondere le foto");
        readerButton.setOnClickListener(v->showReader());nav.addView(readerButton,new LinearLayout.LayoutParams(dp(72),dp(47)));
        Button original=button("Orig.");original.setContentDescription("Visualizza l'articolo originale");original.setOnClickListener(v->showOriginal());nav.addView(original,new LinearLayout.LayoutParams(dp(52),dp(47)));
        Button translate=button("IT");translate.setContentDescription("Traduci in italiano");translate.setOnClickListener(v->showTranslation());nav.addView(translate,new LinearLayout.LayoutParams(dp(39),dp(47)));
        Button external=button("↗");external.setContentDescription("Sito originale");external.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(originalUrl)));}catch(Exception e){Toast.makeText(this,"Browser non disponibile",Toast.LENGTH_SHORT).show();}});
        nav.addView(external,new LinearLayout.LayoutParams(dp(43),dp(47)));root.addView(nav);
        browser=new WebView(this);browser.setBackgroundColor(Color.BLACK);
        browser.getSettings().setDomStorageEnabled(true);
        browser.getSettings().setMediaPlaybackRequiresUserGesture(true);
        browser.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        browser.getSettings().setSupportZoom(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.getSettings().setDisplayZoomControls(false);
        browser.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
                String url=request.getUrl().toString();
                return AdBlock.blocked(url)?AdBlock.empty():null;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest r){
                String u=r.getUrl().toString();
                if(u.startsWith("https://"))return false;
                try{startActivity(new Intent(Intent.ACTION_VIEW,r.getUrl()));}catch(Exception ignored){}
                return true;
            }
            @Override public void onPageFinished(WebView view,String url){
                if(url.startsWith("https://"))view.evaluateJavascript(AdBlock.cosmeticScript(),null);
            }
        });
        FrameLayout contentFrame=new FrameLayout(this);
        contentFrame.addView(browser,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        amberOverlay=new View(this);
        amberOverlay.setBackgroundColor(Color.argb(90,105,74,24));
        amberOverlay.setClickable(false);amberOverlay.setFocusable(false);
        amberOverlay.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        amberOverlay.setVisibility(View.GONE);
        contentFrame.addView(amberOverlay,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(contentFrame,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        setContentView(root);
        showOriginal();
    }
    private int dp(int d){return Math.round(d*getResources().getDisplayMetrics().density);}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(32,32,32)));b.setPadding(0,0,0,0);b.setTextSize(12);return b;}
    private void showOriginal(){
        if(browser==null)return;
        requestToken++;setWarm(false);
        String file=OfflineStore.path(this,originalUrl);
        local=file!=null;
        browser.getSettings().setAllowFileAccess(local);
        browser.getSettings().setAllowFileAccessFromFileURLs(false);
        browser.getSettings().setAllowUniversalAccessFromFileURLs(false);
        browser.getSettings().setJavaScriptEnabled(!local);
        browser.loadUrl(local?"file://"+file:originalUrl);
    }
    private void showTranslation(){
        if(browser==null)return;
        requestToken++;setWarm(false);
        String cached=OfflineStore.translatedPath(this,originalUrl);
        if(cached!=null){browser.getSettings().setJavaScriptEnabled(false);browser.getSettings().setAllowFileAccess(true);browser.loadUrl("file://"+cached);return;}
        String text=OfflineStore.plainText(this,originalUrl);
        if(text!=null){translatePlain(text,true);return;}
        // The remote document is not copied unless user saves it. Translate the visible article text on demand.
        if(browser.getUrl()!=null&&browser.getUrl().startsWith("https://")){
            browser.evaluateJavascript("(document.querySelector('article')||document.querySelector('main')||document.body).innerText.slice(0,35000)", result->{
                try{String plain=new JSONArray("["+result+"]").optString(0,"");translatePlain(plain,false);}
                catch(Exception e){Toast.makeText(this,"Testo non disponibile",Toast.LENGTH_SHORT).show();}
            });
        }else Toast.makeText(this,"Apri la pagina originale per tradurla",Toast.LENGTH_SHORT).show();
    }
    private void setReaderSelected(boolean selected){
        if(readerButton==null)return;
        readerButton.setText(selected?"✓ Lettura":"Lettura");
        readerButton.setContentDescription(selected?"Filtro caldo attivo, articolo e foto originali conservati":"Riduci la luminosità e aumenta il calore dello schermo senza modificare la pagina");
    }
    private void setWarm(boolean enabled){
        warmReading=enabled;
        setReaderSelected(enabled);
        if(amberOverlay!=null)amberOverlay.setVisibility(enabled?View.VISIBLE:View.GONE);
        // Local per-activity tint: no system-wide color or brightness changes.
    }
    private void showReader(){
        // Reading mode only changes the screen tint and apparent brightness.
        // It intentionally preserves the page, photographs, layout and scroll position.
        setWarm(!warmReading);
    }
    private void translatePlain(String text,boolean persist){
        if(text==null||text.length()<20){Toast.makeText(this,"Testo non disponibile",Toast.LENGTH_SHORT).show();return;}
        Toast.makeText(this,"Traduzione in preparazione. Il primo utilizzo scarica il modello.",Toast.LENGTH_LONG).show();
        TranslateManager.translateLong(text,(translated,error)->{
            if(error!=null){runOnUiThread(()->Toast.makeText(this,"Traduzione non disponibile: "+error,Toast.LENGTH_LONG).show());return;}
            if(persist){background.execute(()->{
                try{OfflineStore.saveTranslated(this,originalUrl,translated);}catch(Exception ignored){}
                runOnUiThread(()->{
                    String path=OfflineStore.translatedPath(this,originalUrl);
                    if(browser!=null&&path!=null){browser.getSettings().setJavaScriptEnabled(false);browser.getSettings().setAllowFileAccess(true);browser.loadUrl("file://"+path);}
                });
            });}
            else runOnUiThread(()->{
                StringBuilder body=new StringBuilder();for(String line:translated.split("\\n\\n"))body.append("<p>").append(TextUtils.htmlEncode(line)).append("</p>");
                browser.getSettings().setJavaScriptEnabled(false);
                browser.loadDataWithBaseURL(null,OfflineStore.wrapHtml("Traduzione in italiano","",body.toString(),"","A.M.B.",true),"text/html","UTF-8",null);
            });
        });
    }
    @Override public void onBackPressed(){if(browser!=null&&browser.canGoBack())browser.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(browser!=null){browser.destroy();browser=null;}background.shutdownNow();super.onDestroy();}
}
