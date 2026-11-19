package com.apexmotoriblaj.amb;

import android.content.Context;
import android.text.TextUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Locale;

/** Personal-use article cache. Never shares or republishes publisher content. */
final class OfflineStore {
    private OfflineStore(){}
    private static String hex(String s)throws Exception{
        byte[] bytes=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder b=new StringBuilder();for(int i=0;i<16;i++)b.append(String.format(Locale.ROOT,"%02x",bytes[i]));
        return b.toString();
    }
    static File folder(Context c,String url)throws Exception{
        if(url==null || !url.startsWith("https://"))throw new IllegalArgumentException("Link HTTPS richiesto");
        return new File(new File(c.getFilesDir(),"amb_offline"),hex(url));
    }
    static boolean exists(Context c,String url){
        try{return new File(folder(c,url),"article.html").isFile();}catch(Exception e){return false;}
    }
    static String path(Context c,String url){
        try{File file=new File(folder(c,url),"article.html");return file.isFile()?file.getAbsolutePath():null;}catch(Exception e){return null;}
    }
    static String translatedPath(Context c,String url){
        try{File f=new File(folder(c,url),"translated.html");return f.isFile()?f.getAbsolutePath():null;}catch(Exception e){return null;}
    }
    private static void writeText(File f,String text) throws IOException {
        try(OutputStream out=new FileOutputStream(f)){out.write(text.getBytes(StandardCharsets.UTF_8));}
    }
    private static String readText(File file) throws IOException {
        try(InputStream in=new FileInputStream(file);ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
            byte[] block=new byte[8192];int n;
            while((n=in.read(block))!=-1){if(buffer.size()+n>5_000_000)throw new IOException("Too large");buffer.write(block,0,n);}
            return buffer.toString("UTF-8");
        }
    }
    static String plainText(Context c,String url){
        try{return readText(new File(folder(c,url),"body.txt"));}catch(Exception e){return null;}
    }
    static void delete(Context c,String url){
        try{File dir=folder(c,url);File[] files=dir.listFiles();if(files!=null)for(File file:files)file.delete();dir.delete();}catch(Exception ignored){}
    }
    private static String photo(Context c,String address,File dir){
        if(address==null||!address.startsWith("https://"))return "";
        HttpURLConnection con=null;
        try{
            con=(HttpURLConnection)new URL(address).openConnection();
            con.setConnectTimeout(7000);con.setReadTimeout(9000);
            con.setRequestProperty("User-Agent","Mozilla/5.0 Android A.M.B. Reader");
            if(con.getResponseCode()!=200 || !"https".equalsIgnoreCase(con.getURL().getProtocol()))return "";
            String mime=con.getContentType();if(mime==null||!mime.toLowerCase(Locale.ROOT).startsWith("image/"))return "";
            String ext=mime.contains("png")?"png":mime.contains("webp")?"webp":"jpg";
            String filename="photo."+ext;
            File f=new File(dir,filename);long total=0;
            try(InputStream in=con.getInputStream();OutputStream out=new FileOutputStream(f)){
                byte[] bytes=new byte[8192];int n;
                while((n=in.read(bytes))!=-1){total+=n;if(total>7_000_000)throw new IOException("Image too big");out.write(bytes,0,n);}
            }catch(Exception e){f.delete();return "";}
            return filename;
        }catch(Exception ignored){return "";}
        finally{if(con!=null)con.disconnect();}
    }
    static String wrapHtml(String title,String description,String body,String image,String source,boolean translated){
        String text="<!doctype html><html lang='"+(translated?"it":"en")+"'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"+
           "<style>:root{color-scheme:dark}*{box-sizing:border-box}body{margin:0;background:#000;color:#f5f5f5;font:16px/1.65 system-ui,Roboto,sans-serif;padding:24px max(17px,calc((100vw - 800px)/2));}"+
           "h1{line-height:1.2;font-size:1.55rem}h2,h3{line-height:1.3}p,li{overflow-wrap:anywhere}img{width:100%;height:auto;border-radius:12px}a{color:#e58e82}small{color:#aaa}blockquote{border-left:3px solid #9f4e43;padding-left:12px}header{margin-bottom:20px}.summary{color:#c6c6c6}</style>"+
           "<header><small>"+TextUtils.htmlEncode(source)+" · A.M.B. Offline</small><h1>"+TextUtils.htmlEncode(title)+"</h1>"+
           (image.isEmpty()?"":"<img src='"+image+"' alt='Foto articolo'>")+
           (description.isEmpty()?"":"<p class='summary'>"+TextUtils.htmlEncode(description)+"</p>")+"</header><main>"+body+"</main></html>";
        return text;
    }
    static void save(Context c,JSONObject data)throws Exception{
        String url=data.optString("link"),title=data.optString("title"),desc=data.optString("desc",""),source=data.optString("source","Sito web");
        File dir=folder(c,url);if(!dir.exists()&&!dir.mkdirs())throw new IOException("Impossibile creare la cartella offline");
        // Individual publishers may not expose complete articles. Never claim a complete offline copy when blocked.
        Document doc=Jsoup.parse(WidgetNewsSync.readHttps(url,5_000_000),url);
        doc.select("script,style,iframe,nav,footer,aside,form,noscript,svg,.advertisement,.advert,.ad-container,.cookie-banner,[aria-label=advertisement]").remove();
        Element body=null;
        for(String css:new String[]{"[itemprop=articleBody]","article .article-body","article .entry-content",".post-content","article","main"}){
            Element match=doc.selectFirst(css);
            if(match!=null&&match.text().length()>Math.max(160,desc.length())){body=match;break;}
        }
        if(body==null)body=doc.body();
        if(body==null)throw new IOException("Articolo non disponibile");
        String content=body.text().trim();
        if(content.length()<120)throw new IOException("Il sito non rende disponibile il testo completo");
        // Remove all remote images and embeds from the body: no background data usage offline.
        String bodyHtml=Jsoup.clean(body.html(),Safelist.relaxed().removeTags("img").addTags("h1","h2","h3","h4","blockquote"));
        String originalPhoto=data.optString("image","");
        if(originalPhoto.isEmpty()&&source.contains("TmRexh"))originalPhoto=WidgetNewsSync.TMREXH_BRAND_ICON;
        String image=photo(c,originalPhoto,dir);
        String page=wrapHtml(title,desc,bodyHtml,image,source,false);
        writeText(new File(dir,"article.html"),page);
        writeText(new File(dir,"body.txt"),content);
        writeText(new File(dir,"metadata.json"),data.toString());
    }
    static void saveTranslated(Context c,String url,String translatedText)throws Exception{
        File dir=folder(c,url),meta=new File(dir,"metadata.json");if(!meta.exists())return;
        JSONObject d=new JSONObject(readText(meta));
        String[] parts=translatedText.split("\\n\\n");
        StringBuilder content=new StringBuilder();for(String p:parts)if(!p.trim().isEmpty())content.append("<p>").append(TextUtils.htmlEncode(p)).append("</p>");
        String photo="";for(String ext:new String[]{"jpg","png","webp"})if(new File(dir,"photo."+ext).isFile()){photo="photo."+ext;break;}
        String html=wrapHtml(d.optString("title"),d.optString("desc"),content.toString(),photo,d.optString("source"),true);
        writeText(new File(dir,"translated.html"),html);
    }
}
