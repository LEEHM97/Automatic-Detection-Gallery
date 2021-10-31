package io.androidapp.gallerysearch.model.local;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class TypeConverter {
    @androidx.room.TypeConverter
    public static LinkedHashSet<String> toLinkedHashSet(String str) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.addAll(Arrays.asList(str.split(" ")));
        return set;
    }
    @androidx.room.TypeConverter
    public static String toString(LinkedHashSet<String> set) {
        String res = "";
        for (String s : set) {
            res = res + " " + s;
        }
        return res;
    }
}
