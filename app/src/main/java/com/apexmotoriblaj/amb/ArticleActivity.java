package com.apexmotoriblaj.amb;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebStorage;
import android.os.Build;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import org.json.JSONArray;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Publisher-native WebView reader with user-controlled consent, ad filtering and offline fallback. */
public final class ArticleActivity extends Activity {
    private WebView browser;
    private String originalUrl;
    private boolean local;
    private int requestToken=0;
    private Button readerButton;
    private Button blockerButton;
    private volatile boolean adBlockEnabled=true;
    private View amberOverlay;
    private boolean warmReading=false;
    private final ExecutorService background=Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        originalUrl=getIntent().getStringExtra("link");
        if(originalUrl==null||!originalUrl.startsWith("https://")){finish();return;}
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        // Draw the toolbar below the status bar on Android 10+, never underneath it.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // Handle both Android 10-14 and Android 15+ enforced edge-to-edge layouts.
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.BLACK);
        root.setFitsSystemWindows(false);
        root.setOnApplyWindowInsetsListener((view,insets)->{
            int top,bottom,left,right;
            if(Build.VERSION.SDK_INT>=30){
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                top=bars.top;bottom=bars.bottom;left=bars.left;right=bars.right;
            }else{
                top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();
                left=insets.getSystemWindowInsetLeft();right=insets.getSystemWindowInsetRight();
            }
            view.setPadding(left,top,right,bottom);
            return insets;
        });
        // Two-row toolbar: no clipped controls at 320 dp or with large font scaling.
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.VERTICAL);
        nav.setPadding(dp(3),dp(4),dp(3),dp(4));nav.setBackgroundColor(Color.rgb(17,17,17));
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);
        Button back=button("‹");back.setContentDescription("Chiudi l'articolo");back.setOnClickListener(v->finish());
        header.addView(back,new LinearLayout.LayoutParams(dp(44),dp(45)));
        TextView name=new TextView(this);name.setText("A.M.B. · Articolo");name.setTextSize(14);
        name.setTextColor(Color.WHITE);name.setGravity(17);
        header.addView(name,new LinearLayout.LayoutParams(0,dp(45),1));
        Button external=button("↗");external.setContentDescription("Apri questo articolo nel browser del telefono");
        external.setOnClickListener(v->openExternal());
        header.addView(external,new LinearLayout.LayoutParams(dp(45),dp(45)));
        Button more=button("⋮");more.setContentDescription("Altre opzioni: offline, ricarica, dati dei siti");
        more.setOnClickListener(v->showMoreMenu(more));
        header.addView(more,new LinearLayout.LayoutParams(dp(44),dp(45)));
        nav.addView(header);

        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
        readerButton=button("Lettura");
        readerButton.setContentDescription("Attiva tonalità calda e luminosità ridotta senza nascondere le foto");
        readerButton.setOnClickListener(v->showReader());
        actions.addView(readerButton,new LinearLayout.LayoutParams(0,dp(45),1.3f));
        Button original=button("Originale");original.setContentDescription("Carica l'articolo online con il suo consenso cookie");
        original.setOnClickListener(v->showOriginal());
        actions.addView(original,new LinearLayout.LayoutParams(0,dp(45),1.4f));
        Button translate=button("IT");translate.setContentDescription("Traduci in italiano");
        translate.setOnClickListener(v->showTranslation());
        actions.addView(translate,new LinearLayout.LayoutParams(0,dp(45),0.65f));
        adBlockEnabled=getSharedPreferences("amb_reader",MODE_PRIVATE).getBoolean("adblock",true);
        blockerButton=button("Filtro ON");blockerButton.setOnClickListener(v->toggleAdBlock());
        actions.addView(blockerButton,new LinearLayout.LayoutParams(0,dp(45),1.35f));
        nav.addView(actions);root.addView(nav);

        browser=new WebView(this);browser.setBackgroundColor(Color.BLACK);
        // Preserve the real publisher page: its scrolling, CMP dialogs and user decision.
        WebSettings settings=browser.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false); // Keep target=_blank links inside the same reader.
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        browser.setVerticalScrollBarEnabled(true);
        browser.setHorizontalScrollBarEnabled(false);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        CookieManager cookies=CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        // Some CMPs require cross-site cookie storage to remember the user's own choice.
        // This does not click any consent buttons: the publisher asks the user directly.
        cookies.setAcceptThirdPartyCookies(browser,true);
        refreshBlockerButton();
        browser.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
                if(!adBlockEnabled || request.isForMainFrame())return null;
                String url=request.getUrl().toString();
                return AdBlock.blocked(url)?AdBlock.empty():null;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                return handleNavigation(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url){
                return handleNavigation(Uri.parse(url));
            }
            @Override public void onPageFinished(WebView view,String url){
                // DO NOT inject CSS/JS into third-party pages, hide CMP dialogs or click Reject.
                // Concealing a CMP while it locks document scrolling causes a frozen article.
                CookieManager.getInstance().flush();
            }
            @Override public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){
                if(request.isForMainFrame() && !local){
                    String cached=OfflineStore.path(ArticleActivity.this,originalUrl);
                    if(cached!=null){
                        runOnUiThread(()->{
                            if(browser==null)return;
                            Toast.makeText(ArticleActivity.this,"Sito non raggiungibile: mostro la copia offline",Toast.LENGTH_SHORT).show();
                            loadOffline(cached);
                        });
                    }
                }
            }
        });
        browser.setWebChromeClient(new WebChromeClient()); // Allow ordinary JavaScript prompts/dialogs.
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
    private boolean handleNavigation(Uri url){
        String scheme=url==null?null:url.getScheme();
        if("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme))return false;
        if("intent".equalsIgnoreCase(scheme)){
            // Do not run arbitrary intent:// payloads from an untrusted publisher page.
            Toast.makeText(this,"Apri il collegamento nel browser del telefono",Toast.LENGTH_SHORT).show();
            return true;
        }
        // mailto:, tel:, etc. remain opt-in via Android's normal chooser.
        if("mailto".equalsIgnoreCase(scheme)||"tel".equalsIgnoreCase(scheme)){
            try{startActivity(new Intent(Intent.ACTION_VIEW,url));}catch(Exception ignored){}
        }
        return true;
    }
    private void openExternal(){
        String current=browser==null?null:browser.getUrl();
        if(current==null||!current.startsWith("https://"))current=originalUrl;
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(current)));}
        catch(Exception e){Toast.makeText(this,"Browser non disponibile",Toast.LENGTH_SHORT).show();}
    }
    private void refreshBlockerButton(){
        if(blockerButton==null)return;
        blockerButton.setText(adBlockEnabled?"Filtro ON":"Filtro OFF");
        blockerButton.setContentDescription(adBlockEnabled?
            "Filtro annunci attivo; tocca per disattivarlo e ricaricare la pagina se il sito non funziona":
            "Filtro annunci disattivato; tocca per riattivarlo");
    }
    private void toggleAdBlock(){
        adBlockEnabled=!adBlockEnabled;
        getSharedPreferences("amb_reader",MODE_PRIVATE).edit().putBoolean("adblock",adBlockEnabled).apply();
        refreshBlockerButton();
        Toast.makeText(this,adBlockEnabled?"Filtro annunci attivato":"Filtro disattivato: il sito potrà caricare anche gli annunci",Toast.LENGTH_SHORT).show();
        // The intercepted requests cannot be restored without reloading the online page.
        showOriginal();
    }
    private void showMoreMenu(View anchor){
        PopupMenu popup=new PopupMenu(this,anchor);
        popup.getMenu().add(0,1,0,"Ricarica il sito");
        String cached=OfflineStore.path(this,originalUrl);
        if(cached!=null)popup.getMenu().add(0,2,1,"Apri copia offline");
        popup.getMenu().add(0,3,2,"Azzera cookie e dati dei siti…");
        popup.setOnMenuItemClickListener(item->{
            int id=item.getItemId();
            if(id==1){showOriginal();return true;}
            if(id==2){String offline=OfflineStore.path(this,originalUrl);if(offline!=null)loadOffline(offline);return true;}
            if(id==3){confirmClearSiteData();return true;}
            return false;
        });
        popup.show();
    }
    private void confirmClearSiteData(){
        new AlertDialog.Builder(this)
          .setTitle("Azzera i dati dei siti?")
          .setMessage("Elimina cookie e dati web di TUTTE le testate visitate con A.M.B. Potresti dover effettuare nuovamente l'accesso e scegliere di nuovo i cookie. I Preferiti e gli articoli salvati non vengono cancellati.")
          .setNegativeButton("Annulla",(dialog,which)->{})
          .setPositiveButton("Azzera",(dialog,which)->{
              CookieManager.getInstance().removeAllCookies(ok->{
                  CookieManager.getInstance().flush();
                  WebStorage.getInstance().deleteAllData();
                  runOnUiThread(()->{
                      if(browser!=null)browser.clearCache(true);
                      showOriginal();
                  });
              });
          }).show();
    }
    private void loadOffline(String path){
        if(browser==null||path==null)return;
        requestToken++;local=true;setWarm(false);
        browser.getSettings().setAllowFileAccess(true);
        browser.getSettings().setAllowFileAccessFromFileURLs(false);
        browser.getSettings().setAllowUniversalAccessFromFileURLs(false);
        browser.getSettings().setJavaScriptEnabled(false);
        browser.loadUrl("file://"+path);
    }
    private int dp(int d){return Math.round(d*getResources().getDisplayMetrics().density);}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(32,32,32)));b.setPadding(0,0,0,0);b.setTextSize(12);return b;}
    private void showOriginal(){
        if(browser==null)return;
        requestToken++;local=false;setWarm(false);
        browser.getSettings().setAllowFileAccess(false);
        browser.getSettings().setAllowFileAccessFromFileURLs(false);
        browser.getSettings().setAllowUniversalAccessFromFileURLs(false);
        browser.getSettings().setJavaScriptEnabled(true);
        // Original always means live publisher site, so the CMP can request consent.
        // Previously a saved offline copy could silently override the live article.
        browser.loadUrl(originalUrl);
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
    @Override protected void onPause(){CookieManager.getInstance().flush();super.onPause();}
    @Override protected void onDestroy(){if(browser!=null){browser.destroy();browser=null;}background.shutdownNow();super.onDestroy();}
}
