package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.weather.presenter.ReturnVoice;

/**
 * Created by derik on 17-12-25.
 */

public interface IWeatherVoiceListener {
    void onWeatherInfo(String cityName,ReturnVoice returnVoice);

    void onRangeTempInfo(String cityName,String time,ReturnVoice returnVoice);
    void onAirQualityInfo(String cityName,ReturnVoice returnVoice);
    void onCurrentTempInfo(String cityName,ReturnVoice returnVoice);
    void onWeatherStatus(String cityName,String time,ReturnVoice returnVoice );
    void onRainInfo(String cityName,String time,ReturnVoice returnVoice);
    void onDressInfo(String cityName,ReturnVoice returnVoice);
    void onUitravioletLevelInfo(String cityName,ReturnVoice returnVoice);
    void onSmogInfo(String cityName,String time,ReturnVoice returnVoice);
}
