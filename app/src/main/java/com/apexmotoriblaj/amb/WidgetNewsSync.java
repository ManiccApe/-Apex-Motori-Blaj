package com.apexmotoriblaj.amb;

import android.content.Context;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Direct RSS/Atom aggregation from user-configured sources for Android widgets. */
public final class WidgetNewsSync {
    private static final ExecutorService WORK = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static final String DEFAULT = "["+
        "{\"name\":\"Motorsport Italia F1\",\"category\":\"F1\",\"url\":\"https://it.motorsport.com/rss/f1/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia WRC\",\"category\":\"WRC\",\"url\":\"https://it.motorsport.com/rss/wrc/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia WEC\",\"category\":\"WEC\",\"url\":\"https://it.motorsport.com/rss/wec/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia GT3\",\"category\":\"GT3\",\"url\":\"https://it.motorsport.com/rss/endurance-gt/news/\",\"enabled\":true},"+
        "{\"name\":\"Motorsport Italia MotoGP\",\"category\":\"MotoGP\",\"url\":\"https://it.motorsport.com/rss/motogp/news/\",\"enabled\":true}]";

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
            conn.setRequestProperty("User-Agent","ApexMotoriBlaj/2.0 (Android RSS Reader)");
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
    private static String image(Element el) {
        for(String name:new String[]{"media:thumbnail","media:content","enclosure","thumbnail"}) {
            NodeList list=el.getElementsByTagName(name);
            for(int i=0;i<list.getLength();i++) {
                Element e=(Element)list.item(i);
                String url=e.getAttribute("url");
                if(url.startsWith("https://") && !url.matches("(?i).*\\.(mp4|webm|mp3)(\\?.*)?$"))return url;
            }
        }
        String desc=get(el,"description","content:encoded","summary");
        Matcher m=Pattern.compile("<img[^>]+src=[\\\"'](https://[^\\\"']+)",Pattern.CASE_INSENSITIVE).matcher(desc);
        return m.find()?m.group(1):"";
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
            row.put("image",image(item));
            row.put("date",epoch(get(item,"pubDate","dc:date","published","updated")));
            result.add(row);
        }
        return result;
    }
    private static void refresh(Context c) throws Exception {
        String sources=c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("sources",DEFAULT);
        JSONArray entries;
        try{entries=new JSONArray(sources);}catch(Exception e){entries=new JSONArray(DEFAULT);}
        List<JSONObject> items=new ArrayList<>();Set<String> seen=new HashSet<>();
        int successes=0;
        for(int i=0;i<entries.length() && i<18;i++) {
            JSONObject source=entries.optJSONObject(i);
            if(source==null || !source.optBoolean("enabled",true))continue;
            String url=source.optString("url","");
            if(!url.startsWith("https://"))continue;
            try {
                List<JSONObject> rows=parse(readHttps(url,2_000_000),source);
                successes++;
                for(JSONObject row:rows)if(seen.add(row.optString("link")))items.add(row);
            }catch(Exception e){Log.w("AMB_Widget","RSS unavailable: "+source.optString("name"),e);}
        }
        if(successes==0)return; // Never overwrite a good cache on network failure.
        // Removed championship categories must not come back from old saved widget sources.
        items.removeIf(row -> {
            String cat=row.optString("category", "");
            return !(cat.equals("F1")||cat.equals("WRC")||cat.equals("WEC")||cat.equals("GT3")||cat.equals("MotoGP")||cat.equals("Personali"));
        });
        items.sort((a,b)->Long.compare(b.optLong("date",0),a.optLong("date",0)));
        JSONArray out=new JSONArray();
        for(int i=0;i<Math.min(150,items.size());i++)out.put(items.get(i));
        c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).edit()
            .putString("widget_news",out.toString())
            .putLong("widget_updated",System.currentTimeMillis()).apply();
        NewsWidget.notifyData(c);
        for(int i=0;i<Math.min(16,items.size());i++) {
            String url=items.get(i).optString("image","");
            try{saveThumbnail(c,url);}catch(Exception ignored){}
        }
        NewsWidget.notifyData(c);
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
        if(!url.startsWith("https://"))return;
        File file=new File(c.getCacheDir(),"ambthumb_"+thumbnailName(url));
        if(file.exists() && file.length()>50)return;
        URL u=new URL(url);
        HttpURLConnection con=(HttpURLConnection)u.openConnection();
        try {
            con.setConnectTimeout(4000);con.setReadTimeout(4000);
            if(con.getResponseCode()!=200 || con.getContentLengthLong()>400_000)return;
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try(InputStream in=con.getInputStream()){
                byte[] b=new byte[4096];int n;
                while((n=in.read(b))!=-1){if(out.size()+n>400_000)return;out.write(b,0,n);}
            }
            BitmapFactory.Options opt=new BitmapFactory.Options();opt.inSampleSize=4;
            Bitmap bm=BitmapFactory.decodeByteArray(out.toByteArray(),0,out.size(),opt);
            if(bm==null)return;
            Bitmap small=Bitmap.createScaledBitmap(bm,105,95,true);
            try(FileOutputStream f=new FileOutputStream(file)){small.compress(Bitmap.CompressFormat.JPEG,75,f);}
            if(small!=bm)small.recycle();bm.recycle();
        }finally{con.disconnect();}
    }
}
