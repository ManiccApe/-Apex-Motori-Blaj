package com.apexmotoriblaj.amb;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import java.util.Set;

/** Per-widget configuration with category, density and transparency controls. */
public final class WidgetConfigActivity extends Activity {
    private int widgetId=AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner count,opacity,contrast;
    private CheckBox images;
    private CheckBox[] cats;
    private final String[] levels={"100%","85%","70%","50%","30%","0%"};
    private final int[] values={100,85,70,50,30,0};
    private final String[] names={"20 notizie","40 notizie","60 notizie"};
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        widgetId=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID) {
            int[] ids=AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this,NewsWidget.class));
            if(ids.length>0)widgetId=ids[0];
        }
        ScrollView scroll=new ScrollView(this);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(18),dp(12),dp(18),dp(22));
        list.setBackgroundColor(Color.rgb(246,248,250));scroll.addView(list);
        title(list,"A.M.B. · Configurazione widget",20);
        sub(list,"Scegli le categorie, il numero di notizie, le immagini e l'aspetto del widget.");
        if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){
            sub(list,"Aggiungi prima il widget dalla schermata Home di Android, quindi apri nuovamente queste impostazioni.");
            Button close=button("Chiudi");close.setOnClickListener(v->finish());list.addView(close);setContentView(scroll);return;
        }
        label(list,"Notizie da visualizzare");count=spinner(list,names);
        int current=WidgetPrefs.count(this,widgetId);count.setSelection(current==20?0:current==60?2:1);
        label(list,"Trasparenza sfondo");opacity=spinner(list,levels);
        int o=WidgetPrefs.opacity(this,widgetId);
        int selected=0;for(int i=0;i<values.length;i++)if(values[i]==o)selected=i;
        opacity.setSelection(selected);
        label(list,"Colore dei testi");contrast=spinner(list,new String[]{"Scuro (sfondo chiaro)","Chiaro (sfondo scuro)"});
        contrast.setSelection(WidgetPrefs.dark(this,widgetId)?1:0);
        images=new CheckBox(this);images.setText("Mostra miniature degli articoli");images.setTextColor(Color.rgb(32,45,60));
        images.setChecked(WidgetPrefs.images(this,widgetId));list.addView(images);
        label(list,"Categorie nel widget");
        sub(list,"Seleziona le categorie che vuoi vedere. Se nessuna è selezionata, saranno mostrate tutte.");
        Set<String> enabled=WidgetPrefs.categories(this,widgetId);
        cats=new CheckBox[WidgetPrefs.CATEGORIES.length];
        for(int i=0;i<cats.length;i++){
            CheckBox check=new CheckBox(this);check.setText(WidgetPrefs.CATEGORIES[i]);
            check.setTextColor(Color.rgb(32,45,60));check.setTextSize(14);
            check.setChecked(enabled.isEmpty()||enabled.contains(WidgetPrefs.CATEGORIES[i]));
            cats[i]=check;list.addView(check);
        }
        Button save=button("Salva e applica");save.setOnClickListener(v->save());list.addView(save);
        setContentView(scroll);
    }
    private void save(){
        JSONArray selected=new JSONArray();for(int i=0;i<cats.length;i++)if(cats[i].isChecked())selected.put(WidgetPrefs.CATEGORIES[i]);
        SharedPreferences.Editor editor=WidgetPrefs.prefs(this).edit();
        editor.putInt(widgetId+"_count",new int[]{20,40,60}[count.getSelectedItemPosition()]);
        editor.putInt(widgetId+"_opacity",values[opacity.getSelectedItemPosition()]);
        editor.putBoolean(widgetId+"_dark",contrast.getSelectedItemPosition()==1);
        editor.putBoolean(widgetId+"_images",images.isChecked());
        editor.putString(widgetId+"_cats",selected.toString());editor.apply();
        NewsWidget.notifyData(this);
        WidgetNewsSync.refreshAsync(getApplicationContext(),false);
        Intent result=new Intent();result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);
        setResult(RESULT_OK,result);finish();
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void title(LinearLayout root,String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(32,43,55));t.setPadding(0,dp(7),0,dp(8));t.setTypeface(null,1);root.addView(t);}
    private void label(LinearLayout root,String s){title(root,s,14);}
    private void sub(LinearLayout root,String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.rgb(99,115,128));t.setTextSize(13);t.setPadding(0,0,0,dp(10));root.addView(t);}
    private Spinner spinner(LinearLayout root,String[] items){Spinner spin=new Spinner(this);ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items);spin.setAdapter(a);spin.setBackgroundColor(Color.WHITE);root.addView(spin,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(47)));return spin;}
    private Button button(String text){Button b=new Button(this);b.setText(text);b.setAllCaps(false);return b;}
}
