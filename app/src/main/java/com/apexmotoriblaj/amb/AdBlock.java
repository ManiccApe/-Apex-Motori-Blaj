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
        "adservice.google.com","googletagmanager.com","doubleverify.com","adsafeprotected.com",
        "indexww.com","sovrn.com","sharethrough.com","triplelift.com","gumgum.com",
        "optad360.com","e-planning.net","mediavine.com","journeyads.com",
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
        // Runs on page load and while CMP banners are inserted. It never chooses "Accept all".
        // Intentionally limits hiding to known ad/CMP overlays; publisher content is kept.
        return "(function(){if(window.__amb251Filter)return;window.__amb251Filter=true;"+
          "var css='ins.adsbygoogle,[id^=google_ads],.ad-container,.advertisement,.sponsored-ad,[data-ad-slot],"+
          ".outbrain-container,.taboola-widget,.ad-banner,.advertising-banner,.sticky-ad,.ad-unit,"+
          "#onetrust-banner-sdk,.onetrust-pc-dark-filter,.qc-cmp2-container,.fc-consent-root,"+
          "[id^=sp_message_container_],.osano-cm-window,.didomi-popup-container,.cc-window';"+
          "var style=document.createElement('style');style.textContent=css+'{display:none!important;visibility:hidden!important}';"+
          "(document.head||document.documentElement).appendChild(style);"+
          "function reject(){var buttons=document.querySelectorAll('button,[role=button],input[type=button]');"+
          "for(var i=0;i<buttons.length;i++){var b=buttons[i],label=((b.textContent||b.value||'')+' '+(b.getAttribute('aria-label')||'')).trim().toLowerCase();"+
          "if(/^(reject|reject all|reject optional|refuse|refuse all|rifiuta|rifiuta tutto|rifiuta tutti|nega|nega tutto|non accetto|continua senza accettare|continue without accepting|only necessary|solo necessari|essenziali soltanto|use necessary cookies only)$/.test(label)){"+
          "try{b.click();}catch(e){}break;}}}"+
          "reject();var t=0;var o=new MutationObserver(function(){if(++t<80)reject();else o.disconnect()});"+
          "o.observe(document.documentElement,{childList:true,subtree:true});"+
          "setTimeout(function(){o.disconnect()},12000);"+
          "})()";
    }
}
