package com.apexmotoriblaj.amb;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Notification of future GitHub releases for users who enabled notifications.
 * No FCM/backend: cannot reach installations that lack this code. */
public final class UpdateCheckJobService extends JobService {
    private static final int JOB_ID=26150;
    private static final int NOTIFICATION_ID=26150;
    private static final String CHANNEL="amb_app_updates";
    private static final String PREFS="amb_shared";
    private static final long TWELVE_HOURS=12L*60*60*1000;
    private static final String API="https://api.github.com/repos/ManiccApe/-Apex-Motori-Blaj/releases?per_page=20";
    private static final Pattern VERSION=Pattern.compile("(?:^|[^0-9])(\\d+)\\.(\\d+)\\.(\\d+)(?:[-._]?rc(\\d+))?",Pattern.CASE_INSENSITIVE);

    static void schedule(Context c){
        if(!c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean("notifications_enabled",false)) return;
        JobScheduler scheduler=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(scheduler==null)return;
        if(scheduler.getPendingJob(JOB_ID)!=null)return;
        JobInfo job=new JobInfo.Builder(JOB_ID,new ComponentName(c,UpdateCheckJobService.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(TWELVE_HOURS)
            .setPersisted(true)
            .build();
        int result=scheduler.schedule(job);
        if(result!=JobScheduler.RESULT_SUCCESS)Log.w("AMB-Updates","Cannot schedule update check");
    }

    @Override public boolean onStartJob(JobParameters params){
        new Thread(()->{
            try{ checkForNewRelease(); }
            catch(Exception ex){ Log.w("AMB-Updates","GitHub update check unavailable",ex); }
            finally{ jobFinished(params,false); }
        },"AMB-release-check").start();
        return true;
    }
    @Override public boolean onStopJob(JobParameters params){return true;}

    private void checkForNewRelease() throws Exception {
        if(!getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean("notifications_enabled",false))return;
        HttpURLConnection conn=(HttpURLConnection)new URL(API).openConnection();
        conn.setConnectTimeout(9000);conn.setReadTimeout(9000);
        conn.setRequestProperty("Accept","application/vnd.github+json");
        conn.setRequestProperty("User-Agent","ApexMotoriBlaj-Android-UpdateChecker");
        JSONArray releases;
        try {
            if(conn.getResponseCode()!=200)return;
            try(BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"))){
                StringBuilder body=new StringBuilder();String line;
                while((line=in.readLine())!=null && body.length()<120000)body.append(line);
                releases=new JSONArray(body.toString());
            }
        }finally{conn.disconnect();}
        int[] installed=parseVersion(BuildConfig.VERSION_NAME);
        if(installed==null)return;
        JSONObject newest=null;int[] newestVersion=installed;
        for(int i=0;i<releases.length();i++){
            JSONObject item=releases.optJSONObject(i);
            if(item==null||item.optBoolean("draft",false))continue;
            int[] v=parseVersion(item.optString("tag_name",""));
            if(v!=null&&compare(v,newestVersion)>0){newestVersion=v;newest=item;}
        }
        if(newest==null)return;
        String tag=newest.optString("tag_name","");
        if(tag.isEmpty()||tag.equals(getSharedPreferences(PREFS,MODE_PRIVATE).getString("update_notified_tag","")))return;
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        NotificationManager n=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(n==null||!n.areNotificationsEnabled())return;
        n.createNotificationChannel(new NotificationChannel(CHANNEL,"A.M.B. · Aggiornamenti app",NotificationManager.IMPORTANCE_DEFAULT));
        String url=newest.optString("html_url","");
        if(!url.startsWith("https://github.com/ManiccApe/-Apex-Motori-Blaj/releases/"))return;
        Intent open=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
        PendingIntent click=PendingIntent.getActivity(this,NOTIFICATION_ID,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification alert=new Notification.Builder(this,CHANNEL)
            .setSmallIcon(R.drawable.ic_racing_car_notification)
            .setContentTitle("Nuovo aggiornamento A.M.B.")
            .setContentText("Disponibile: "+tag+" · Tocca per aprire GitHub")
            .setContentIntent(click).setAutoCancel(true).build();
        n.notify(NOTIFICATION_ID,alert);
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("update_notified_tag",tag).apply();
    }
    private static int[] parseVersion(String value){
        if(value==null)return null;
        Matcher m=VERSION.matcher(value);
        if(!m.find())return null;
        try{return new int[]{Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)),m.group(4)==null?Integer.MAX_VALUE:Integer.parseInt(m.group(4))};}
        catch(NumberFormatException ignored){return null;}
    }
    private static int compare(int[] a,int[] b){
        for(int i=0;i<4;i++)if(a[i]!=b[i])return Integer.compare(a[i],b[i]);
        return 0;
    }
}
