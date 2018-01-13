package com.idx.smartspeakdock;

import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.litepal.LitePalApplication;

/**
 * Created by steve on 12/15/17.
 */

public class SpeakerApplication extends LitePalApplication {
    private static Context context;
    private static SpeakerApplication instance;
    private RefWatcher mRefWatcher;

    @Override
    public void onCreate(){
        super.onCreate();
        mRefWatcher = LeakCanary.install(this);
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

    public static RefWatcher getRefWatcher(Context context){
        SpeakerApplication application = (SpeakerApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }
}
