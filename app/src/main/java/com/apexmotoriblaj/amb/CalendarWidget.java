package com.apexmotoriblaj.amb;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.widget.RemoteViews;
import java.text.*;
import java.util.*;
import org.json.*;
/** Month overview widget with event count, updated from the app calendar cache. */
public final class CalendarWidget extends AppWidgetProvider {
 @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids){for(int id:ids)update(c,manager,id);}
 static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);android.content.ComponentName n=new android.content.ComponentName(c,CalendarWidget.class);for(int id:m.getAppWidgetIds(n))update(c,m,id);}
 static void update(Context c,AppWidgetManager m,int id){
  RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.widget_calendar_261);
  Calendar now=Calendar.getInstance();String month=new SimpleDateFormat("MMMM yyyy",Locale.ITALIAN).format(now.getTime());rv.setTextViewText(R.id.calendar261_title,"A.M.B. · "+month);
  int count=0;StringBuilder agenda=new StringBuilder();
  try{JSONArray events=new JSONArray(c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("calendar_events","[]"));for(int i=0;i<events.length();i++){JSONObject e=events.optJSONObject(i);if(e==null)continue;long date=e.optLong("date",0);if(date<=0)continue;Calendar d=Calendar.getInstance();d.setTimeInMillis(date);if(d.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&d.get(Calendar.MONTH)==now.get(Calendar.MONTH)){count++;if(agenda.length()<120)agenda.append(new SimpleDateFormat("dd",Locale.ITALIAN).format(d.getTime())).append(" · ").append(e.optString("name","Evento")).append("\n");}}}catch(Exception ignored){}
  rv.setTextViewText(R.id.calendar261_count,count+" eventi questo mese");rv.setTextViewText(R.id.calendar261_agenda,agenda.length()>0?agenda.toString():"Nessun evento sincronizzato · Apri il calendario");
  PendingIntent pi=PendingIntent.getActivity(c,id,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);rv.setOnClickPendingIntent(R.id.calendar261_root,pi);m.updateAppWidget(id,rv);
 }
}
