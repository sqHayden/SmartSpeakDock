package com.idx.smartspeakdock.baidu.unit.listener;

/**
 * Created by derik on 17-12-25.
 */

public interface IWeatherVoiceListener {
    void onWeatherInfo(String cityName);

    String onRangeTempInfo(String cityName,String time);
    String onAirQualityInfo(String cityName);
    String onCurrentTempInfo(String cityName);

    String onWeatherStatus(String cityName,String time);
    String onRainInfo(String cityName,String time);
    String onDressInfo(String cityName);
    String onUitravioletLevelInfo(String cityName);
    String onSmogInfo(String cityName,String time);
}
