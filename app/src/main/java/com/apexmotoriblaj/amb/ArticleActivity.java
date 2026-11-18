package com.apexmotoriblaj.amb;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

/** Opens article links in the external browser; never embeds video playback. */
public final class ArticleActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        String url = getIntent().getStringExtra("link");
        if (url != null && (url.startsWith("https://") || url.startsWith("http://"))) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { }
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
