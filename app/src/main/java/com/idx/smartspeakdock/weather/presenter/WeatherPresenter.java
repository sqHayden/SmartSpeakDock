package com.idx.smartspeakdock.weather.presenter;

/**
 * Created by danny on 12/21/17.
 */

public interface WeatherPresenter {
    /**
     * 获取天气基本信息
     */
    void getWeather(String name);

    /**
     * 获取空气质量信息
     */
    void getWeatherAqi(String cityName);
}
