package com.idx.smartspeakdock.weather.model;

import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;

/**
 * MVP：model接口
 * Created by danny on 12/21/17.
 */

public interface WeatherModel {
    void loadWeather(String name, OnWeatherListener listener);
    void loadWeatherAqi(String cityName, OnWeatherListener listener);
}
