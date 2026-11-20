package com.apexmotoriblaj.amb;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.FrameLayout;

/** YouTube's official iframe (including platform ads); no feed scraping or ad suppression in player. */
public final class VideoPlayerActivity extends Activity {
    public static final String EXTRA_VIDEO_ID="video_id";
    private WebView player;
    private FrameLayout fullscreen;
    private View fullscreenView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        final String id=getIntent().getStringExtra(EXTRA_VIDEO_ID);
        if(id==null||!id.matches("[A-Za-z0-9_-]{11}")){finish();return;}
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        LinearLayout column=new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setBackgroundColor(Color.BLACK);
        LinearLayout controls=new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        Button back=new Button(this);
        back.setText("✕ Chiudi");
        back.setOnClickListener(v->finish());
        controls.addView(back,new LinearLayout.LayoutParams(0,60,1));
        Button external=new Button(this);
        external.setText("Apri su YouTube ↗");
        external.setOnClickListener(v->{
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/watch?v="+id)));}
            catch(Exception ignored){}
        });
        controls.addView(external,new LinearLayout.LayoutParams(0,60,1));
        column.addView(controls,new LinearLayout.LayoutParams(-1,-2));
        player=new WebView(this);
        player.setBackgroundColor(Color.BLACK);
        WebSettings settings=player.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setAllowFileAccess(false);
        player.setWebViewClient(new WebViewClient());
        player.setWebChromeClient(new WebChromeClient(){
            @Override public void onShowCustomView(View view,CustomViewCallback callback){
                if(fullscreenView!=null){callback.onCustomViewHidden();return;}
                fullscreenView=view;fullscreenCallback=callback;
                fullscreen=new FrameLayout(VideoPlayerActivity.this);
                fullscreen.setBackgroundColor(Color.BLACK);
                fullscreen.addView(view,new FrameLayout.LayoutParams(-1,-1));
                addContentView(fullscreen,new ViewGroup.LayoutParams(-1,-1));
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            @Override public void onHideCustomView(){closeFullscreen();}
        });
        column.addView(player,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(column);
        // HTTPS base supplies a non-file origin/referrer required by YouTube embeds.
        // This dedicated WebView never changes the original app's file:// storage/preferences.
        String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"+
            "<style>html,body{margin:0;background:#000;height:100%;overflow:hidden}iframe{width:100%;height:100%;border:0}</style></head><body>"+
            "<iframe src='https://www.youtube.com/embed/"+id+"?playsinline=1&rel=0' " +
            "title='YouTube' allow='accelerometer; autoplay; encrypted-media; picture-in-picture; fullscreen' allowfullscreen></iframe></body></html>";
        player.loadDataWithBaseURL("https://appassets.androidplatform.net/",html,"text/html","UTF-8",null);
    }
    private void closeFullscreen(){
        if(fullscreenView==null)return;
        if(fullscreen!=null){fullscreen.removeAllViews();((ViewGroup)fullscreen.getParent()).removeView(fullscreen);fullscreen=null;}
        fullscreenView=null;
        if(fullscreenCallback!=null){fullscreenCallback.onCustomViewHidden();fullscreenCallback=null;}
        getWindow().getDecorView().setSystemUiVisibility(0);
    }
    @Override public void onBackPressed(){if(fullscreenView!=null)closeFullscreen();else super.onBackPressed();}
    @Override protected void onDestroy(){closeFullscreen();if(player!=null){player.stopLoading();player.destroy();player=null;}super.onDestroy();}
}
