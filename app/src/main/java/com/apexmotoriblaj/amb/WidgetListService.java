package com.apexmotoriblaj.amb;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WidgetListService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext(),intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID));
    }
    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private final int id;
        private final List<JSONObject> data = new ArrayList<>();
        Factory(Context ctx,int widgetId){c=ctx;id=widgetId;}
        @Override public void onCreate(){load();}
        @Override public void onDataSetChanged(){load();}
        private void load() {
            data.clear();
            String json = c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("widget_news","[]");
            Set<String> enabled = WidgetPrefs.categories(c,id);
            int max = WidgetPrefs.count(c,id);
            try {
                JSONArray arr = new JSONArray(json);
                for(int i=0;i<arr.length() && data.size()<max;i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if(o == null) continue;
                    String cat=o.optString("category","");
                    // Old cached GT items must explicitly concern GT3; retired categories are hidden.
                    if("GT".equals(cat)) {
                        if(!GT3Classifier.isGT3(o.optString("title", ""),""))continue;
                        try{o.put("category","GT3");}catch(Exception ignored){}
                        cat="GT3";
                    }
                    if(!java.util.Arrays.asList(WidgetPrefs.CATEGORIES).contains(cat))continue;
                    if(!enabled.isEmpty() && !enabled.contains(cat))continue;
                    if(o.optString("link","").startsWith("https://") && o.optString("image","").startsWith("https://")) data.add(o);
                }
            }catch(Exception ignored){}
        }
        @Override public int getCount(){return data.size();}
        @Override public RemoteViews getViewAt(int position) {
            if(position<0 || position>=data.size())return null;
            JSONObject o=data.get(position);
            RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.widget_row);
            String mode=WidgetPrefs.appearance(c,id);
            boolean dark="amoled".equals(mode)||"dark".equals(mode)||
                ("transparent".equals(mode) && WidgetPrefs.dark(c,id));
            int rowBackground="transparent".equals(mode) ? Color.TRANSPARENT :
                "amoled".equals(mode) ? Color.BLACK :
                dark ? Color.argb(38,255,255,255) : Color.argb(18,0,0,0);
            rv.setInt(R.id.widget_row,"setBackgroundColor",rowBackground);
            rv.setTextViewText(R.id.widget_category,o.optString("category","Motorsport"));
            rv.setTextViewText(R.id.widget_headline,o.optString("title",""));
            rv.setTextViewText(R.id.widget_source,o.optString("source",""));
            rv.setTextColor(R.id.widget_headline,dark?Color.WHITE:Color.rgb(28,39,50));
            rv.setTextColor(R.id.widget_source,dark?Color.rgb(202,211,220):Color.rgb(90,107,125));
            rv.setTextColor(R.id.widget_category,dark?Color.rgb(244,166,153):Color.rgb(157,55,46));
            Bitmap image=null;
            if(WidgetPrefs.images(c,id)) {
                String path=WidgetNewsSync.thumbnailFile(c,o.optString("image",""));
                if(path!=null)image=BitmapFactory.decodeFile(path);
            }
            if(image!=null) {rv.setImageViewBitmap(R.id.widget_image,image);rv.setViewVisibility(R.id.widget_image,View.VISIBLE);}
            else rv.setViewVisibility(R.id.widget_image,View.GONE);
            Intent fill=new Intent();fill.putExtra("link",o.optString("link",""));
            rv.setOnClickFillInIntent(R.id.widget_row,fill);
            return rv;
        }
        @Override public RemoteViews getLoadingView(){return null;}
        @Override public int getViewTypeCount(){return 1;}
        @Override public long getItemId(int position){return position;}
        @Override public boolean hasStableIds(){return false;}
        @Override public void onDestroy(){data.clear();}
    }
}
