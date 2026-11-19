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
        "adservice.google.com","doubleverify.com","adsafeprotected.com",
        "indexww.com","sovrn.com","sharethrough.com","triplelift.com","gumgum.com",
        "optad360.com","e-planning.net","mediavine.com","journeyads.com",
        "demdex.net"));
    /** Only obvious ad hosts, never consent managers or pages themselves. */
    static boolean blocked(String url){
        try{
            URI u=new URI(url);String host=u.getHost();if(host==null)return false;
            host=host.toLowerCase(Locale.ROOT);
            String path=(u.getPath()==null?"":u.getPath()).toLowerCase(Locale.ROOT);
            // Consent platforms and privacy endpoints must remain accessible.
            for(String marker:new String[]{"consent","privacy","cookie","gdpr","onetrust","didomi", "sourcepoint", "trustarc", "cmp", "fundingchoices"}){
                if(host.contains(marker)||path.contains(marker))return false;
            }
            for(String d:BLOCK)if(host.equals(d)||host.endsWith("."+d))return true;
        }catch(Exception ignored){}
        return false;
    }
    static WebResourceResponse empty(){
        return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
    }
}
