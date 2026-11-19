package com.apexmotoriblaj.amb;

import android.webkit.WebResourceResponse;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/** Small native domain-rule ad/tracker blocker by ManicApe. No traffic interception or analytics. */
final class AdBlock {
    private static final Set<String> BLOCK = new HashSet<>(Arrays.asList(
        "doubleclick.net","googlesyndication.com","googleadservices.com","googletagservices.com",
        "adnxs.com","criteo.com","criteo.net","taboola.com","outbrain.com","pubmatic.com",
        "rubiconproject.com","openx.net","advertising.com","adsrvr.org","smartadserver.com",
        "scorecardresearch.com","quantserve.com","casalemedia.com","adform.net","adform.com",
        "amazon-adsystem.com","yieldmo.com","bidswitch.net","moatads.com","zedo.com",
        "media.net","teads.tv","lijit.com","revcontent.com","mgid.com","adroll.com",
        "google-analytics.com","connect.facebook.net","pixel.facebook.com","hotjar.com",
        "analytics.tiktok.com","bat.bing.com","demdex.net"));
    static boolean blocked(String url){
        try{
            URI u=new URI(url);String host=u.getHost();if(host==null)return false;
            host=host.toLowerCase(Locale.ROOT);
            for(String d:BLOCK)if(host.equals(d)||host.endsWith("."+d))return true;
        }catch(Exception ignored){}
        return false;
    }
    static WebResourceResponse empty(){
        return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
    }
    static String cosmeticScript(){
        return "(function(){var style=document.createElement('style');style.textContent="+
        "'ins.adsbygoogle,[id^=google_ads],.ad-container,.advertisement,.sponsored-ad,"+
        ".outbrain-container,.taboola-widget,[data-ad-slot],[data-ad-client]{display:none!important}';"+
        "document.head.appendChild(style)})()";
    }
}
