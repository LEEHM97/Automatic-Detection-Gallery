package io.androidapp.gallerysearch;

import android.app.Application;

import io.androidapp.gallerysearch.utils.MyDebugTree;
import timber.log.Timber;

public class GlobalApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new MyDebugTree());
        Settings.getInstance().load(getBaseContext());
    }
}
