package io.androidapp.gallerysearch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static final Settings ourInstance = new Settings();

    public static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    public static final String SETTINGS = "settings";

    public static final String MAX_LOAD_COUNT = "max load count";
    public int maxLoadCount = 10000;   //기기에 저장된 사진 불러올 때 최대로 불러올 사진의 수

    public static final String SORT_IDX = "sort idx";
    public int sortIdx = 0;   //정렬 기준 Idx


    public void save(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(MAX_LOAD_COUNT, maxLoadCount);
        editor.putInt(SORT_IDX, sortIdx);
        editor.apply();
    }

    public void load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS, Activity.MODE_PRIVATE);
        maxLoadCount = sp.getInt(MAX_LOAD_COUNT, 10000);
        sortIdx = sp.getInt(SORT_IDX, 0);
    }

}
