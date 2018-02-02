package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.weather.presenter.ReturnVoice;

/**
 * Created by derik on 17-12-25.
 */

public interface IWeatherVoiceListener {
    //天气信息,城市时间 , 城市 , 时间 ,无任何指定
    void onWeatherInfo(String cityName,ReturnVoice returnVoice);
    void onCityWeatherInfo(String cityName,ReturnVoice returnVoice);
    void onTimeWeatherINfo(String time,ReturnVoice returnVoice);
    void onNoWeatherInfo(ReturnVoice returnVoice);
    //最温差信息,城市时间 , 城市 , 时间 ,无任何指定
    void onRangeTempInfo(String cityName,String time,ReturnVoice returnVoice);
    void onCityRangeTempInfo(String cityName,ReturnVoice returnVoice);
    void onTimeRangeTempInfo(String time,ReturnVoice returnVoice);
    void onNoRangeTempInfo(ReturnVoice returnVoice);
    //空气质量信息,城市时间 , 城市 , 时间 ,无任何指定
    void onAirQualityInfo(String cityName,String time,ReturnVoice returnVoice);
    void onCityAirQualityInfo(String cityName,ReturnVoice returnVoice);
    void onTimeAirQualityInfo(String time,ReturnVoice returnVoice);
    void onNoAiqQualityInfo(ReturnVoice returnVoice);
    //当前温度信息,城市 ,无任何指定
    void onCurrentTempInfo(String cityName,ReturnVoice returnVoice);
    void onNoCurrentTempInfo(ReturnVoice returnVoice);
    //天气状况信息,城市时间 , 城市 , 时间 ,无任何指定
    void onWeatherStatus(String cityName,String time,ReturnVoice returnVoice );
    void onCityWeatherStatus(String cityName,ReturnVoice returnVoice);
    void onTimeWeatherStatus(String time,ReturnVoice returnVoice);
    void onNoWeatherStatus(ReturnVoice returnVoice);
    //下雨信息,城市时间 , 城市 , 时间 ,无任何指定
    void onRainInfo(String cityName,String time,ReturnVoice returnVoice);
    void onCityRainInfo(String cityName,ReturnVoice returnVoice);
    void onTimeRainInfo(String time,ReturnVoice returnVoice);
    void onNoRainInfo(ReturnVoice returnVoice);
    //穿衣信息,城市时间 , 城市 , 时间
    void onDressInfo(String cityName,String time, ReturnVoice returnVoice);
    void onCityDressInfo(String cityName,ReturnVoice returnVoice);
    void onTimeDressInfo(String time,ReturnVoice returnVoice);
    //紫外线强度信息,城市时间 , 城市 , 时间
    void onUitravioletLevelInfo(String cityName,String time,ReturnVoice returnVoice);
    void onCityUitravioletLevelInfo(String cityName,ReturnVoice returnVoice);
    void onTimeUitravioletLevelInfo(String time,ReturnVoice returnVoice);
    //雾霾信息,城市时间 , 城市 , 时间
    void onSmogInfo(String cityName,String time,ReturnVoice returnVoice);
    void onCitySmogInfo(String cityName,ReturnVoice returnVoice);
    void onTimeSmogInfo(String time,ReturnVoice returnVoice);
}
