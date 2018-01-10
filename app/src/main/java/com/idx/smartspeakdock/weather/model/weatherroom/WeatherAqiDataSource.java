package com.idx.smartspeakdock.weather.model.weatherroom;

import java.util.List;

/**
 * Created by danny on 1/5/18.
 */

public interface WeatherAqiDataSource {
    interface LoadWeatherAqisListCallback{
        void onWeatherAqisListLoaded(List<WeatherAqi> weatherAqis);

        void onDataNotAvailable();
    }

    interface LoadWeatherAqisCallback{
        void onWeatherAqisLoaded(WeatherAqi weatherAqi);

        void onDataNotAvailable();
    }

    void addWeatherAqi(WeatherAqi weatherAqi);

    void getWeatherAqis(LoadWeatherAqisListCallback callback);

    void getWeatherAqi(String cityName, LoadWeatherAqisCallback callback);

    void deleteWeatherAqis();

    void deleteWeatherAqi(String cityName);
}
