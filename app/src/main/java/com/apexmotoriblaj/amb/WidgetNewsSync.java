package com.apexmotoriblaj.amb;

import android.content.Context;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Direct RSS/Atom aggregation from user-configured sources for Android widgets. */
public final class WidgetNewsSync {
    static final String TMREXH_BRAND_ICON="https://www.google.com/s2/favicons?domain=tmrexh.it&sz=256";
    private static final ExecutorService WORK = Executors.newSingleThreadExecutor();
    private static final ExecutorService IMAGE_WORK = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static final AtomicBoolean IMAGES_BUSY = new AtomicBoolean(false);
    private static final AtomicBoolean IMAGES_DIRTY = new AtomicBoolean(false);
    private static final String DEFAULT = "["+
        "{\"name\":\"Motorsport Italia F1\",\"category\":\"F1\",\"url\":\"https://it.motorsport.com/rss/f1/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia WRC\",\"category\":\"WRC\",\"url\":\"https://it.motorsport.com/rss/wrc/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia WEC\",\"category\":\"WEC\",\"url\":\"https://it.motorsport.com/rss/wec/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia GT3\",\"category\":\"GT3\",\"url\":\"https://it.motorsport.com/rss/endurance-gt/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia MotoGP\",\"category\":\"MotoGP\",\"url\":\"https://it.motorsport.com/rss/motogp/news/\",\"enabled\":true},"+
        "{\"name\":\"Motor1 Italia\",\"category\":\"Auto\",\"url\":\"https://it.motor1.com/rss/news/all/\",\"enabled\":true},"+
        "{\"name\":\"Automoto.it\",\"category\":\"Auto\",\"url\":\"https://www.automoto.it/rss/news.xml\",\"enabled\":true},"+
        "{\"name\":\"TmRexh · Articoli tecnici\",\"category\":\"Auto\",\"url\":\"https://www.tmrexh.it/category/articoli-tecnici/feed/\",\"enabled\":true}]";

    private WidgetNewsSync(){}
    public static void refreshAsync(Context context,boolean force) {
        Context c=context.getApplicationContext();
        long last=c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getLong("widget_updated",0);
        if(!force && System.currentTimeMillis()-last<30*60*1000)return;
        if(!BUSY.compareAndSet(false,true))return;
        WORK.execute(() -> {
            try { refresh(c); }catch(Exception e){ Log.w("AMB_Widget","Widget refresh failed",e); }
            finally { BUSY.set(false); }
        });
    }
    public static String readHttps(String address,int maxBytes) throws Exception {
        if(address==null || address.length()>1800 || !address.startsWith("https://"))throw new IllegalArgumentException("È richiesto un URL HTTPS valido");
        URL url = new URL(address);
        if(url.getHost()==null || url.getHost().isEmpty())throw new IllegalArgumentException("Host mancante");
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        try {
            conn.setConnectTimeout(8000);conn.setReadTimeout(10000);
            boolean youtube="youtube.com".equalsIgnoreCase(url.getHost()) || "www.youtube.com".equalsIgnoreCase(url.getHost());
            conn.setRequestProperty("User-Agent",youtube?"Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36":"ApexMotoriBlaj/2.6.1 (Android RSS Reader)");
            if(youtube)conn.setRequestProperty("Accept-Language","it-IT,it;q=0.9,en;q=0.7");
            conn.setRequestProperty("Accept","application/rss+xml, application/atom+xml, text/calendar, application/xml, text/xml, */*");
            conn.setInstanceFollowRedirects(true);
            if(conn.getResponseCode()<200 || conn.getResponseCode()>299)throw new Exception("HTTP "+conn.getResponseCode());
            // Disallow unexpected downgrade redirects.
            if(!"https".equalsIgnoreCase(conn.getURL().getProtocol()))throw new Exception("Redirect non sicuro");
            try(InputStream in=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] b=new byte[8192];int n;
                while((n=in.read(b))!=-1) {
                    if(out.size()+n>maxBytes)throw new Exception("Risposta troppo grande");
                    out.write(b,0,n);
                }
                return out.toString("UTF-8");
            }
        }finally{conn.disconnect();}
    }
    private static String get(Element e,String... tags) {
        for(String tag:tags) {
            NodeList list=e.getElementsByTagName(tag);
            if(list.getLength()>0) {
                String v=list.item(0).getTextContent();
                if(v!=null && !v.trim().isEmpty())return v.trim();
            }
        }
        return "";
    }
    /** Only image links belonging to an item; rejects favicons, videos and tracking pixels. */
    private static String validPhoto(String value){
        if(value==null || !value.startsWith("https://"))return "";
        try{
            String path=new URL(value).getPath().toLowerCase(java.util.Locale.ROOT);
            if(path.matches(".*\\.(svg|gif|ico|mp4|webm|mp3|m3u8|pdf)$"))return "";
            if(path.matches(".*\\/(pixel|spacer|tracking|favicon)([._/-].*|$)"))return "";
        }catch(Exception ignored){return "";}
        return value.replace("&amp;","&");
    }
    private static String image(Element el) {
        for(String name:new String[]{"media:content","media:thumbnail","enclosure","thumbnail","image"}){
            NodeList list=el.getElementsByTagName(name);
            for(int i=0;i<list.getLength();i++) {
                Element e=(Element)list.item(i);
                String type=e.getAttribute("type").toLowerCase(java.util.Locale.ROOT);
                if(type.startsWith("video/")||type.startsWith("audio/"))continue;
                try{
                    int w=Integer.parseInt(e.getAttribute("width"));if(w>0 && w<48)continue;
                }catch(NumberFormatException ignored){}
                String url=e.getAttribute("url");
                if(url.isEmpty())url=e.getAttribute("src");
                if(url.isEmpty() && "image".equals(name))url=get(e,"url");
                url=validPhoto(url);
                if(!url.isEmpty())return url;
            }
        }
        for(String tag:new String[]{"content:encoded","description","summary","content"}){
            String html=get(el,tag);if(html.isEmpty())continue;
            Matcher m=Pattern.compile("<img\\b[^>]*(?:data-src|data-original|src)\\s*=\\s*[\\\"'](https://[^\\\"']+)",Pattern.CASE_INSENSITIVE).matcher(html);
            while(m.find()){
                String url=validPhoto(m.group(1));if(!url.isEmpty())return url;
            }
        }
        return "";
    }
    private static long epoch(String date) {
        try{return ZonedDateTime.parse(date,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();}
        catch(Exception ignored){}
        try{return java.time.Instant.parse(date).toEpochMilli();}catch(Exception ignored){}
        return 0;
    }
    private static List<JSONObject> parse(String xml,JSONObject source) throws Exception {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
        factory.setXIncludeAware(false);factory.setExpandEntityReferences(false);
        Document doc=factory.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        NodeList list=doc.getElementsByTagName("item");boolean atom=false;
        if(list.getLength()==0){list=doc.getElementsByTagName("entry");atom=true;}
        List<JSONObject> result=new ArrayList<>();
        for(int i=0;i<Math.min(60,list.getLength());i++) {
            Element item=(Element)list.item(i);
            String title=get(item,"title"),link=get(item,"link");
            if(atom) {
                NodeList links=item.getElementsByTagName("link");
                for(int j=0;j<links.getLength();j++) {
                    Element le=(Element)links.item(j);
                    if(!le.hasAttribute("rel") || "alternate".equals(le.getAttribute("rel"))) {link=le.getAttribute("href");break;}
                }
            }
            if(!link.startsWith("https://") || title.isEmpty())continue;
            boolean defaultGT3="gt3".equals(source.optString("id")) ||
                source.optString("name", "").startsWith("Motorsport Italia GT3");
            if(defaultGT3 && !GT3Classifier.isGT3(title,get(item,"description","content:encoded","summary")))continue;
            JSONObject row=new JSONObject();
            row.put("title",title);row.put("link",link);
            row.put("source",source.optString("name","Motorsport"));
            row.put("category",source.optString("category","Personali"));
            String photo=image(item);
            boolean technical=source.optString("url","").contains("tmrexh.it/category/articoli-tecnici/feed/");
            if(photo.isEmpty()&&!technical)continue; // exclusive TmRexh exception
            if(photo.isEmpty()&&technical)photo=TMREXH_BRAND_ICON;
            row.put("image",photo);
            row.put("date",epoch(get(item,"pubDate","dc:date","published","updated")));
            result.add(row);
        }
        return result;
    }
    static void refreshForJob(Context c) throws Exception {refresh(c);}
    static void refresh(Context c) throws Exception {
        String sources=c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("sources",DEFAULT);
        JSONArray entries;
        try{entries=new JSONArray(sources);}catch(Exception e){entries=new JSONArray(DEFAULT);}
        List<JSONObject> items=new ArrayList<>();Set<String> seen=new HashSet<>();
        int successes=0;
        // Independent requests in a small bounded pool: widget remains usable with 20+ sites.
        java.util.concurrent.ExecutorService feeds=Executors.newFixedThreadPool(4);
        List<java.util.concurrent.Future<List<JSONObject>>> pending=new ArrayList<>();
        try {
            for(int i=0;i<entries.length() && i<75;i++) {
                JSONObject source=entries.optJSONObject(i);
                if(source==null || !source.optBoolean("enabled",true))continue;
                String url=source.optString("url","");
                if(!url.startsWith("https://"))continue;
                pending.add(feeds.submit(()->parse(readHttps(url,2_000_000),source)));
            }
            for(java.util.concurrent.Future<List<JSONObject>> future:pending) {
                try {
                    List<JSONObject> rows=future.get(24,java.util.concurrent.TimeUnit.SECONDS);
                    successes++;
                    for(JSONObject row:rows)if(seen.add(row.optString("link")))items.add(row);
                }catch(Exception e){future.cancel(true);Log.w("AMB_Widget","RSS unavailable during widget update",e);}
            }
        }finally{feeds.shutdownNow();}
        if(successes==0)return; // Never overwrite a good cache on network failure.
        // Removed championship categories must not come back from old saved widget sources.
        items.removeIf(row -> {
            String cat=row.optString("category", "");
            return !(cat.equals("F1")||cat.equals("WRC")||cat.equals("WEC")||cat.equals("GT3")||cat.equals("MotoGP")||cat.equals("Auto")||cat.equals("Personali"));
        });
        items.sort((a,b)->Long.compare(b.optLong("date",0),a.optLong("date",0)));
        JSONArray out=new JSONArray();
        for(int i=0;i<Math.min(300,items.size());i++)out.put(items.get(i));
        c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).edit()
            .putString("widget_news",out.toString())
            .putLong("widget_updated",System.currentTimeMillis()).apply();
        NewsWidget.notifyData(c);
        // Download thumbnails for the rows actually displayed in each widget.
        prefetchWidgetImagesAsync(c);
    }

    /** Called both after WebView transfers news and when a widget is created/refreshed.
     * Never download images on the UI thread or inside RemoteViewsFactory.getViewAt(). */
    public static void prefetchWidgetImagesAsync(Context context) {
        final Context app=context.getApplicationContext();
        IMAGES_DIRTY.set(true);
        if(!IMAGES_BUSY.compareAndSet(false,true))return;
        IMAGE_WORK.execute(() -> {
            try{
                do {
                    IMAGES_DIRTY.set(false);
                    prefetchWidgetImages(app);
                } while (IMAGES_DIRTY.get());
            }catch(Exception e){Log.w("AMB_Widget","Thumbnail sync failed",e);}
            finally{
                IMAGES_BUSY.set(false);
                // A new data set may have arrived just as the worker was completing.
                if(IMAGES_DIRTY.get())prefetchWidgetImagesAsync(app);
            }
        });
    }

    private static void prefetchWidgetImages(Context c) throws Exception {
        int[] widgetIds=AppWidgetManager.getInstance(c).getAppWidgetIds(new ComponentName(c,NewsWidget.class));
        if(widgetIds==null || widgetIds.length==0)return;
        JSONArray feed=new JSONArray(c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("widget_news","[]"));
        LinkedHashSet<String> urls=new LinkedHashSet<>();
        for(int widgetId:widgetIds){
            if(!WidgetPrefs.images(c,widgetId))continue;
            Set<String> allowed=WidgetPrefs.categories(c,widgetId);
            int desired=WidgetPrefs.count(c,widgetId),selected=0;
            for(int i=0;i<feed.length() && selected<desired;i++){
                JSONObject item=feed.optJSONObject(i);
                if(item==null)continue;
                String category=item.optString("category","");
                if("GT".equals(category))category="GT3";
                if(!allowed.isEmpty() && !allowed.contains(category))continue;
                String url=item.optString("image","");
                if(!item.optString("link","").startsWith("https://") || !url.startsWith("https://"))continue;
                urls.add(url);selected++;
                if(urls.size()>=180)break;
            }
        }
        if(urls.isEmpty())return;
        ExecutorService pool=Executors.newFixedThreadPool(4);
        try{
            List<java.util.concurrent.Future<?>> jobs=new ArrayList<>();
            for(String url:urls){
                jobs.add(pool.submit(() -> {
                    try{saveThumbnail(c,url);}catch(Exception e){Log.d("AMB_Widget","Image unavailable: "+e.getMessage());}
                }));
            }
            // Notify the launcher in batches, so visible thumbnails appear progressively.
            int processed=0;
            for(java.util.concurrent.Future<?> job:jobs){
                try{job.get(16,java.util.concurrent.TimeUnit.SECONDS);}
                catch(Exception e){job.cancel(true);}
                processed++;
                if(processed%8==0)NewsWidget.notifyData(c);
            }
            NewsWidget.notifyData(c);
        }finally{pool.shutdownNow();}
    }

    private static String thumbnailName(String url) throws Exception {
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8));
        StringBuilder b=new StringBuilder();for(int i=0;i<8;i++)b.append(String.format("%02x",hash[i]));
        return b+".jpg";
    }
    static String thumbnailFile(Context c,String url) {
        if(!url.startsWith("https://"))return null;
        try {File f=new File(c.getCacheDir(),"ambthumb_"+thumbnailName(url));return f.exists()?f.getAbsolutePath():null;}
        catch(Exception e){return null;}
    }
    private static void saveThumbnail(Context c,String url) throws Exception {
        if(url==null || !url.startsWith("https://"))return;
        File file=new File(c.getCacheDir(),"ambthumb_"+thumbnailName(url));
        if(file.exists() && file.length()>100)return;
        HttpURLConnection con=(HttpURLConnection)new URL(url).openConnection();
        try{
            con.setConnectTimeout(7000);con.setReadTimeout(9000);
            con.setInstanceFollowRedirects(true);
            con.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36");
            con.setRequestProperty("Accept","image/jpeg,image/png,image/webp,image/*;q=0.8,*/*;q=0.5");
            if(con.getResponseCode()!=200 || !"https".equalsIgnoreCase(con.getURL().getProtocol()))return;
            final int MAX_IMAGE=5_000_000;
            if(con.getContentLengthLong()>MAX_IMAGE)return;
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try(InputStream in=con.getInputStream()){
                byte[] b=new byte[8192];int n;
                while((n=in.read(b))!=-1){
                    if(out.size()+n>MAX_IMAGE)return;
                    out.write(b,0,n);
                }
            }
            byte[] data=out.toByteArray();
            BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
            BitmapFactory.decodeByteArray(data,0,data.length,bounds);
            if(bounds.outWidth<48 || bounds.outHeight<48)return;
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=1;
            while(bounds.outWidth/options.inSampleSize>420 || bounds.outHeight/options.inSampleSize>420)
                options.inSampleSize*=2;
            Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,options);
            if(bitmap==null)return;
            // 360x320 supports high-density launchers while keeping image memory bounded.
            Bitmap cropped=Bitmap.createScaledBitmap(bitmap,360,320,true);
            try(FileOutputStream f=new FileOutputStream(file)){
                if(!cropped.compress(Bitmap.CompressFormat.JPEG,90,f))file.delete();
            }finally{
                if(cropped!=bitmap)cropped.recycle();
                bitmap.recycle();
            }
        }finally{con.disconnect();}
    }
}
