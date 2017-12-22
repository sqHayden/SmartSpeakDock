package com.idx.smartspeakdock.weather.presenter;

import com.idx.smartspeakdock.weather.model.WeatherModel;
import com.idx.smartspeakdock.weather.model.WeatherModelImpl;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.ui.WeatherUi;

/**
 * Created by danny on 12/21/17.
 */

public class WeatherPresenterImpl implements WeatherPresenter,OnWeatherListener {
    /*Presenter作为中间层，持有View和Model的引用*/
    private WeatherUi mWeatherUi;
    private WeatherModel mWeatherModel;

    public WeatherPresenterImpl(WeatherUi weatherUi) {
        this.mWeatherUi = weatherUi;
        mWeatherModel = new WeatherModelImpl();
    }

    @Override
    public void getWeather(String name) {
        mWeatherUi.showLoading();
        mWeatherModel.loadWeather(name, this);
    }

    @Override
    public void getWeatherAqi(String cityName) {
        mWeatherUi.showLoading();
        mWeatherModel.loadWeatherAqi(cityName, this);
    }

    @Override
    public void onSuccess(Weather weather) {
        mWeatherUi.hideLoading();
        mWeatherUi.setWeatherInfo(weather);
    }

    @Override
    public void onSuccessAqi(Weather weather) {
        mWeatherUi.hideLoading();
        mWeatherUi.setWeatherAqi(weather);
    }

    @Override
    public void onError() {
        mWeatherUi.hideLoading();
        mWeatherUi.showError();
    }
}
