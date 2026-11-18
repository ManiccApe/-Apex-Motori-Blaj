package com.apexmotoriblaj.amb;

import java.util.regex.Pattern;

/** Recognize GT3 series coverage even when the headline omits the literal "GT3". */
final class GT3Classifier {
    private static final Pattern MARKERS=Pattern.compile(
        "(?i)(?:\\bGT[\\s-]?3\\b|\\bGTWC\\b|GT World Challenge|GT World\\s*[|:]|GT World Cup|\\bGTWCE\\b|Intercontinental GT Challenge|\\bIGTC\\b|\\bSRO\\b|\\bGTD\\b|24\\s*(?:h|ore|hours)\\s*(?:di\\s*)?Spa|Spa 24 Hours|ADAC GT Masters|GT Masters|GT3\\.R|Blancpain GT|GT Cup Macau)");
    static boolean isGT3(String title,String description) {
        return MARKERS.matcher((title==null?"":title)+" "+(description==null?"":description)).find();
    }
    private GT3Classifier() {}
}
