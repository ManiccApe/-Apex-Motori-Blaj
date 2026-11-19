package com.apexmotoriblaj.amb;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.Manifest;
import android.os.Build;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Opt-in notification scheduling with a six-hour interval and Android power constraints. */
public final class NotificationScheduler extends JobService {
    private static final int JOB_ID=2531;
    private static final String CHANNEL="amb_news";
    private static final ExecutorService WORK=Executors.newSingleThreadExecutor();
    static void enable(Context c){
        c.getSharedPreferences("amb_shared",MODE_PRIVATE).edit().putBoolean("notifications_enabled",true).apply();
        if(Build.VERSION.SDK_INT>=26){
            NotificationManager n=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
            if(n!=null)n.createNotificationChannel(new NotificationChannel(CHANNEL,"A.M.B. · Motorsport",NotificationManager.IMPORTANCE_DEFAULT));
        }
        JobScheduler jobs=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(jobs!=null){
            JobInfo job=new JobInfo.Builder(JOB_ID,new ComponentName(c,NotificationScheduler.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(6*60*60*1000L)
                .setPersisted(false)
                .build();
            jobs.schedule(job);
        }
    }
    @Override public boolean onStartJob(JobParameters params){
        WORK.execute(()->{
            try{
                String previous=getSharedPreferences("amb_shared",MODE_PRIVATE).getString("widget_news","[]");
                String lastTitle=new JSONArray(previous).optJSONObject(0)==null?"":new JSONArray(previous).optJSONObject(0).optString("title","");
                WidgetNewsSync.refreshForJob(getApplicationContext());
                String refreshed=getSharedPreferences("amb_shared",MODE_PRIVATE).getString("widget_news","[]");
                JSONObject first=new JSONArray(refreshed).optJSONObject(0);
                if(first!=null&&!lastTitle.isEmpty()&&!lastTitle.equals(first.optString("title"))){
                    send(1,"Nuove notizie motorsport",first.optString("title","A.M.B."));
                }
                JSONArray events=new JSONArray(getSharedPreferences("amb_shared",MODE_PRIVATE).getString("calendar_events","[]"));
                long now=System.currentTimeMillis();
                for(int i=0;i<events.length();i++){
                    JSONObject e=events.optJSONObject(i);if(e==null)continue;
                    long ts=e.optLong("date",0);String id=e.optString("id","");
                    if(ts>now && ts<now+24*60*60*1000L && !id.isEmpty()){
                        String previousEvent=getSharedPreferences("amb_shared",MODE_PRIVATE).getString("last_event_id","");
                        if(!previousEvent.equals(id)){
                            send(2,"Evento motorsport nelle prossime 24 ore",e.optString("name","Gara"));
                            getSharedPreferences("amb_shared",MODE_PRIVATE).edit().putString("last_event_id",id).apply();
                        }
                        break;
                    }
                }
            }catch(Exception ignored){}
            finally{jobFinished(params,false);}
        });return true;
    }
    private void send(int id,String title,String desc){
        if(!getSharedPreferences("amb_shared",MODE_PRIVATE).getBoolean("notifications_enabled",false))return;
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        NotificationManager m=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);if(m==null)return;
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        b.setContentTitle(title).setContentText(desc).setSmallIcon(R.drawable.ic_racing_car_notification)
            .setContentIntent(pi).setAutoCancel(true).setStyle(new Notification.BigTextStyle().bigText(desc));
        m.notify(id,b.build());
    }
    @Override public boolean onStopJob(JobParameters params){return true;}
}
