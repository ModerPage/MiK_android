package me.modernpage.util;

import android.app.Application;
import android.content.res.Resources;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {
    private static App mInstance;
    private static Resources mResource;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mResource = getResources();
    }

    public static App getInstance() {
        return mInstance;
    }

    public static Resources getResource() {
        return mResource;
    }
}
