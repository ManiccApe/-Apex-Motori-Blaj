package com.apexmotoriblaj.amb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Transparent Samsung-like month grid with day labels from verified imported .ics events. */
public final class CalendarWidget extends AppWidgetProvider {
    private static final String PREV="com.apexmotoriblaj.amb.CALENDAR_PREV";
    private static final String NEXT="com.apexmotoriblaj.amb.CALENDAR_NEXT";
    private static final String PREF="amb_calendar_widget";
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle options){update(c,m,id);}
    @Override public void onDeleted(Context c,int[] ids){android.content.SharedPreferences.Editor e=c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit();for(int id:ids)e.remove("offset_"+id);e.apply();}
    @Override public void onReceive(Context c,Intent intent){
        super.onReceive(c,intent);
        String action=intent.getAction();
        if(!PREV.equals(action)&&!NEXT.equals(action))return;
        int id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);if(id<0)return;
        android.content.SharedPreferences prefs=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        int offset=prefs.getInt("offset_"+id,0)+(NEXT.equals(action)?1:-1);
        offset=Math.max(-12,Math.min(12,offset));
        prefs.edit().putInt("offset_"+id,offset).apply();
        update(c,AppWidgetManager.getInstance(c),id);
    }
    static void refresh(Context c){
        AppWidgetManager m=AppWidgetManager.getInstance(c);
        ComponentName n=new ComponentName(c,CalendarWidget.class);
        for(int id:m.getAppWidgetIds(n))update(c,m,id);
    }
    private static int rid(Context c,String name){return c.getResources().getIdentifier(name,"id",c.getPackageName());}
    private static PendingIntent shift(Context c,int id,String action){
        Intent i=new Intent(c,CalendarWidget.class).setAction(action).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        int request=id*3+(PREV.equals(action)?1:2);
        return PendingIntent.getBroadcast(c,request,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    private static PendingIntent openDay(Context c,int id,int slot,Calendar date){
        Intent i=new Intent(c,MainActivity.class);
        i.putExtra("amb_calendar_date",new SimpleDateFormat("yyyy-MM-dd",Locale.ITALIAN).format(date.getTime()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c,id*50+slot,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void update(Context c,AppWidgetManager manager,int id){
        Calendar today=Calendar.getInstance(),first=(Calendar)today.clone();
        first.set(Calendar.DAY_OF_MONTH,1);
        int offset=c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getInt("offset_"+id,0);
        first.add(Calendar.MONTH,offset);
        RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.widget_calendar_261);
        rv.setTextViewText(R.id.calendar261_title,"A.M.B. · "+new SimpleDateFormat("MMMM yyyy",Locale.ITALIAN).format(first.getTime()));
        rv.setOnClickPendingIntent(R.id.calendar261_prev,shift(c,id,PREV));
        rv.setOnClickPendingIntent(R.id.calendar261_next,shift(c,id,NEXT));
        String[] headers={"LUN","MAR","MER","GIO","VEN","SAB","DOM"};
        for(int col=0;col<7;col++){
            int v=rid(c,"cal261_dayname_"+col);
            rv.setTextViewText(v,headers[col]);rv.setTextColor(v,col==6?0xFFFF6571:Color.WHITE);
        }
        Map<Integer,List<JSONObject>> events=new HashMap<>();int count=0;
        try{
            JSONArray arr=new JSONArray(c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("calendar_events","[]"));
            for(int i=0;i<arr.length();i++){
                JSONObject e=arr.optJSONObject(i);if(e==null)continue;
                long timestamp=e.optLong("date",0);if(timestamp<=0)continue;
                Calendar d=Calendar.getInstance();d.setTimeInMillis(timestamp);
                if(d.get(Calendar.YEAR)!=first.get(Calendar.YEAR)||d.get(Calendar.MONTH)!=first.get(Calendar.MONTH))continue;
                count++;
                int day=d.get(Calendar.DAY_OF_MONTH);
                if(!events.containsKey(day))events.put(day,new ArrayList<>());
                events.get(day).add(e);
            }
        }catch(Exception ignored){}
        rv.setTextViewText(R.id.calendar261_count,count+" eventi");
        Calendar current=(Calendar)first.clone();
        int leading=(current.get(Calendar.DAY_OF_WEEK)+5)%7;
        current.add(Calendar.DAY_OF_MONTH,-leading);
        for(int row=0;row<6;row++)for(int col=0;col<7;col++){
            int slot=row*7+col;
            int root=rid(c,"cal261_cell_"+row+"_"+col);
            int num=rid(c,"cal261_num_"+row+"_"+col);
            int label=rid(c,"cal261_event_"+row+"_"+col);
            boolean inMonth=current.get(Calendar.MONTH)==first.get(Calendar.MONTH)&&current.get(Calendar.YEAR)==first.get(Calendar.YEAR);
            boolean now=current.get(Calendar.YEAR)==today.get(Calendar.YEAR)&&current.get(Calendar.MONTH)==today.get(Calendar.MONTH)&&current.get(Calendar.DAY_OF_MONTH)==today.get(Calendar.DAY_OF_MONTH);
            rv.setTextViewText(num,Integer.toString(current.get(Calendar.DAY_OF_MONTH)));
            rv.setTextColor(num,now?0xFFFFFFFF:inMonth?(col==6?0xFFFF6A74:0xFFFFFFFF):0x66FFFFFF);
            rv.setInt(root,"setBackgroundResource",now?R.drawable.widget_calendar_cell_today_261:R.drawable.widget_calendar_bg_261);
            List<JSONObject> dayEvents=inMonth?events.get(current.get(Calendar.DAY_OF_MONTH)):null;
            if(dayEvents!=null&&!dayEvents.isEmpty()){
                JSONObject firstEvent=dayEvents.get(0);
                String eventName=firstEvent.optString("name","Evento");
                // Two short lines, like a calendar widget rather than a plain list.
                if(eventName.length()>24)eventName=eventName.substring(0,23)+"…";
                String extra=dayEvents.size()>1?" +"+(dayEvents.size()-1):"";
                rv.setTextViewText(label,eventName+extra);
                rv.setViewVisibility(label,View.VISIBLE);
                String category=firstEvent.optString("category","");
                int color="F1".equals(category)?0xFFDFA8FF:"WRC".equals(category)?0xFF88D8FF:"MotoGP".equals(category)?0xFFFFB16C:"WEC".equals(category)?0xFF94D6C3:"GT3".equals(category)?0xFFFFAA91:0xFF96CDFF;
                rv.setTextColor(label,Color.WHITE);
                int chip="F1".equals(category)?R.drawable.widget_calendar_chip_f1_261:
                         "WRC".equals(category)?R.drawable.widget_calendar_chip_wrc_261:
                         "MotoGP".equals(category)?R.drawable.widget_calendar_chip_motogp_261:
                         "WEC".equals(category)?R.drawable.widget_calendar_chip_wec_261:
                         "GT3".equals(category)?R.drawable.widget_calendar_chip_gt3_261:
                         R.drawable.widget_calendar_chip_default_261;
                rv.setInt(label,"setBackgroundResource",chip);
            }else{rv.setTextViewText(label,"");rv.setViewVisibility(label,View.GONE);}
            rv.setOnClickPendingIntent(root,openDay(c,id,slot,current));
            current.add(Calendar.DAY_OF_MONTH,1);
        }
        manager.updateAppWidget(id,rv);
    }
}
