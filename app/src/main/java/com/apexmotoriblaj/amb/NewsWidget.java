package com.apexmotoriblaj.amb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.os.Build;
import android.widget.RemoteViews;
import android.view.View;

/** Native, scrollable, per-widget configurable news widget for API 29+. */
public final class NewsWidget extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.apexmotoriblaj.amb.ACTION_WIDGET_REFRESH";

    static void notifyData(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, NewsWidget.class));
        if (ids.length == 0) return;
        for (int id : ids) updateOne(context, manager, id);
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }
    static void updateOne(Context context, AppWidgetManager mgr, int id) {
        try { render(context,mgr,id); }
        catch (RuntimeException | LinkageError error) {
            Log.e("AMB-NewsWidget","News rendering failed; using safe layout",error);
            try {
                RemoteViews safe=new RemoteViews(context.getPackageName(),R.layout.widget_fallback_news);
                safe.setOnClickPendingIntent(R.id.widget_fallback_root,
                    PendingIntent.getActivity(context,id,new Intent(context,MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
                mgr.updateAppWidget(id,safe);
            } catch(RuntimeException ignored) { Log.e("AMB-NewsWidget","Fallback failed",ignored); }
        }
    }
    private static void render(Context context, AppWidgetManager mgr, int id) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_shell);
        String mode=WidgetPrefs.appearance(context,id);
        boolean dark = "amoled".equals(mode) || "dark".equals(mode) ||
                ("transparent".equals(mode) && WidgetPrefs.dark(context,id));
        int opacity = WidgetPrefs.opacity(context,id);
        int bg = "amoled".equals(mode) ? Color.BLACK : "transparent".equals(mode) ? Color.TRANSPARENT :
                Color.argb(Math.max(0,Math.min(100,opacity))*255/100,
                        dark?15:248,dark?23:250,dark?33:253);
        int fg = dark ? Color.WHITE : Color.rgb(28,39,50);
        if("amoled".equals(mode))
            rv.setInt(R.id.widget_root,"setBackgroundResource",R.drawable.widget_bg_amoled);
        else
            rv.setInt(R.id.widget_root,"setBackgroundColor",bg);
        rv.setTextColor(R.id.widget_title,fg);
        rv.setTextColor(R.id.widget_refresh,fg);
        rv.setTextColor(R.id.widget_settings,fg);
        rv.setTextColor(R.id.widget_empty,fg);
        Intent service = new Intent(context,WidgetListService.class);
        service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        service.setData(Uri.parse("amb://widget/list/"+id));
        rv.setRemoteAdapter(R.id.widget_list,service);
        rv.setEmptyView(R.id.widget_list,R.id.widget_empty);

        Intent main = new Intent(context,MainActivity.class);
        PendingIntent open = PendingIntent.getActivity(context,id,main,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_title,open);
        Intent refresh = new Intent(context,NewsWidget.class).setAction(ACTION_REFRESH);
        refresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        PendingIntent refreshPI = PendingIntent.getBroadcast(context,id,refresh,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_refresh,refreshPI);
        Intent configure = new Intent(context,WidgetConfigActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        PendingIntent cfgPI = PendingIntent.getActivity(context,id,configure,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_settings,cfgPI);
        Intent click = new Intent(context,ArticleActivity.class);
        PendingIntent clickPI = PendingIntent.getActivity(context,id+10_000,click,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        rv.setPendingIntentTemplate(R.id.widget_list,clickPI);
        mgr.updateAppWidget(id,rv);
    }
    @Override public void onUpdate(Context c,AppWidgetManager mgr,int[] ids) {
        for(int id:ids) updateOne(c,mgr,id);
        try {
            WidgetNewsSync.refreshAsync(c.getApplicationContext(),false);
            WidgetNewsSync.prefetchWidgetImagesAsync(c.getApplicationContext());
        }catch(RuntimeException error){Log.e("AMB-NewsWidget","Refresh request failed",error);}
    }
    @Override public void onReceive(Context c,Intent intent) {
        super.onReceive(c,intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            WidgetNewsSync.refreshAsync(c.getApplicationContext(),true);
            WidgetNewsSync.prefetchWidgetImagesAsync(c.getApplicationContext());
        }
    }
    @Override public void onRestored(Context c,int[] oldIds,int[] newIds){
        onUpdate(c,AppWidgetManager.getInstance(c),newIds);
    }
    @Override public void onDeleted(Context c,int[] ids) { for(int id:ids) WidgetPrefs.remove(c,id); }
}
