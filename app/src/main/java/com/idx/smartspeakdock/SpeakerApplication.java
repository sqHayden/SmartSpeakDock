package com.idx.smartspeakdock;

import android.content.Context;
import android.util.Log;

import android.support.multidex.MultiDex;

import org.litepal.LitePalApplication;

/**
 * Created by steve on 12/15/17.
 */

public class SpeakerApplication extends LitePalApplication {
    private static Context context;
    private static SpeakerApplication instance;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);
        instance = this;
    }
    public static SpeakerApplication getInstance() {
        return instance;
    }

    public static Context getContext(){
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
