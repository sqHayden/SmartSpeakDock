package com.idx.smartspeakdock.weather.ui;

import com.idx.smartspeakdock.weather.model.weather.Weather;

/**
 * Created by danny on 12/22/17.
 */

public interface WeatherUi {
    void showLoading();

    void hideLoading();

    void showError();

    void setWeatherInfo(Weather weather);

    void setWeatherAqi(Weather weather);
}
