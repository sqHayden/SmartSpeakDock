package com.idx.smartspeakdock;

import android.content.Context;

import org.litepal.LitePalApplication;

import static com.idx.smartspeakdock.weather.utils.ParseAreaUtil.sendRequestWithHttpURLConnection;

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
        String url = "https://cdn.heweather.com/china-city-list.txt";
        sendRequestWithHttpURLConnection(url);
    }
    public static SpeakerApplication getInstance() {
        return instance;
    }

    public static Context getContext(){
        return context;
    }

}
