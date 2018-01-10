package com.idx.smartspeakdock;

import android.content.Context;

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

}
