package com.idx.smartspeakdock.weather.presenter;

/**
 * Created by ryan on 18-1-20.
 * Email: Ryan_chan01212@yeah.net
 */

public interface WeatherCallback {
    void onWeatherCallback(String cityName,String time,ReturnVoice returnVoice,String func_flag,int flag);
}
