package com.idx.smartspeakdock.weather.model.weatherroom;

import java.util.List;

/**
 * 对天气基本信息操作数据库接口
 * Created by danny on 1/5/18.
 */

public interface WeatherBasicDataSource {
    interface LoadWeatherBasicsCallback{
        void onWeatherBasicsLoaded(WeatherBasic weatherBasic);

        void onDataNotAvailable();
    }

    interface LoadWeatherBasicsListCallback{
        void onWeatherBasicsLoaded(List<WeatherBasic> weatherBasic);

        void onDataNotAvailable();
    }

    void addWeatherBasic(WeatherBasic weatherBasic);

    void getWeatherBasics(LoadWeatherBasicsListCallback callback);

    void getWeatherBasic(String cityName, LoadWeatherBasicsCallback callback);

    void deleteWeatherBasics();

    void deleteWeatherBasic(String cityName);
}
