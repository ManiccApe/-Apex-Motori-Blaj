package com.apexmotoriblaj.amb;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.*;
import java.util.*;
/** Inexact local-time 19:00 brief notification; battery-friendly, no invented headlines. */
public final class DailyBriefReceiver extends BroadcastReceiver {
 public static void schedule(Context c){
  c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).edit().putBoolean("brief_261_enabled",true).apply();
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(am==null)return;
  Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,19);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);
  if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_YEAR,1);
  Intent intent=new Intent(c,DailyBriefReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(c,261,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  am.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pi);
 }
 @Override public void onReceive(Context c,Intent intent){
  if(!c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getBoolean("brief_261_enabled",false))return;
  if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  String title="Brief delle 19:00 · A.M.B.",body="Apri il Brief per le notizie automotive e motorsport.";
  try{JSONArray arr=new JSONArray(c.getSharedPreferences("amb_shared",Context.MODE_PRIVATE).getString("widget_news","[]"));JSONObject first=arr.optJSONObject(0);if(first!=null){String t=first.optString("title","");if(!t.isEmpty())body="Ultime notizie: "+t;}}catch(Exception ignored){}
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);if(nm==null)return;
  String channel="amb_brief_261";if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(channel,"A.M.B. · Brief delle 19:00",NotificationManager.IMPORTANCE_DEFAULT));
  PendingIntent open=PendingIntent.getActivity(c,261,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,channel):new Notification.Builder(c);
  nm.notify(261,b.setSmallIcon(R.drawable.ic_racing_car_notification).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setAutoCancel(true).setContentIntent(open).build());
 }
}
