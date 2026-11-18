package com.apexmotoriblaj.amb;

import android.content.Context;
import android.content.SharedPreferences;
import android.appwidget.AppWidgetManager;
import org.json.JSONArray;
import java.util.HashSet;
import java.util.Set;

final class WidgetPrefs {
    private WidgetPrefs() {}
    static final String[] CATEGORIES = {"F1","WRC","WEC","GT3","MotoGP","Auto","Personali"};
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("amb_widget", Context.MODE_PRIVATE); }
    static int count(Context c,int id) { return prefs(c).getInt(id+"_count",40); }
    static String appearance(Context c,int id) {
        return prefs(c).getString(id+"_appearance",dark(c,id)?"dark":"light");
    }
    static int opacity(Context c,int id) {
        String mode=appearance(c,id);
        if("amoled".equals(mode))return 100;
        if("transparent".equals(mode))return 0;
        return prefs(c).getInt(id+"_opacity",88);
    }
    static boolean dark(Context c,int id) { return prefs(c).getBoolean(id+"_dark",false); }
    static boolean images(Context c,int id) { return prefs(c).getBoolean(id+"_images",true); }
    static String rowSize(Context c,int id) {
        String value=prefs(c).getString(id+"_rowSize","normal");
        return "compact".equals(value) || "large".equals(value) ? value : "normal";
    }
    static Set<String> categories(Context c,int id) {
        Set<String> out = new HashSet<>();
        try { JSONArray a = new JSONArray(prefs(c).getString(id+"_cats", "[]")); for (int i=0;i<a.length();i++) out.add(a.optString(i)); }
        catch(Exception ignored){}
        out.retainAll(new HashSet<>(java.util.Arrays.asList(CATEGORIES)));
        return out;
    }
    static void remove(Context c,int id) { prefs(c).edit().remove(id+"_count").remove(id+"_opacity").remove(id+"_dark").remove(id+"_images").remove(id+"_cats").remove(id+"_appearance").remove(id+"_rowSize").apply(); }
}
