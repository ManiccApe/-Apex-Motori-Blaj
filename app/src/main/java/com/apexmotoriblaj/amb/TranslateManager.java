package com.apexmotoriblaj.amb;

import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.common.model.DownloadConditions;
import java.util.ArrayList;
import java.util.List;

/** On-device en -> it translation. The model is downloaded once and reused by ML Kit. */
final class TranslateManager {
    interface Callback { void done(String value,String error); }
    private static final Translator client = Translation.getClient(new TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.ITALIAN).build());
    private static final DownloadConditions conditions=new DownloadConditions.Builder().build();
    static void translate(String input,Callback callback){
        if(input==null||input.isEmpty()){callback.done("",null);return;}
        client.downloadModelIfNeeded(conditions).addOnSuccessListener(unused ->
            client.translate(input).addOnSuccessListener(s->callback.done(s,null))
                .addOnFailureListener(e->callback.done(null,e.getMessage())))
            .addOnFailureListener(e->callback.done(null,"Modello offline non disponibile: "+e.getMessage()));
    }
    /** Translate a long saved article in chunks; the HTML is then stored locally. */
    static void translateLong(String text,Callback callback){
        if(text==null||text.trim().isEmpty()){callback.done("",null);return;}
        List<String> chunks=new ArrayList<>();StringBuilder chunk=new StringBuilder();
        // Split on sentences and keep each request under ML Kit's practical input limit.
        String[] sentences=text.split("(?<=[.!?])\\s+|\\n+");
        for(String sentence:sentences){
            if(sentence.length()>1500){
                if(chunk.length()>0){chunks.add(chunk.toString());chunk.setLength(0);}
                for(int i=0;i<sentence.length();i+=1400)chunks.add(sentence.substring(i,Math.min(sentence.length(),i+1400)));
            }else if(chunk.length()+sentence.length()>1450){chunks.add(chunk.toString());chunk.setLength(0);chunk.append(sentence);}
            else chunk.append(sentence).append(' ');
        }
        if(chunk.length()>0)chunks.add(chunk.toString());
        translateNext(chunks,0,new StringBuilder(),callback);
    }
    private static void translateNext(List<String> parts,int index,StringBuilder out,Callback callback){
        if(index>=parts.size()){callback.done(out.toString(),null);return;}
        translate(parts.get(index),(value,error)->{
            if(error!=null){callback.done(null,error);return;}
            out.append(value).append("\n\n");
            translateNext(parts,index+1,out,callback);
        });
    }
}
