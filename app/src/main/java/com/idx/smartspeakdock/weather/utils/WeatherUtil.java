package com.idx.smartspeakdock.weather.utils;

import android.util.Log;

import com.idx.smartspeakdock.standby.HttpUtil;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by ryan on 17-12-23.
 * Email: Ryan_chan01212@yeah.net
 */

public class WeatherUtil {
    private static final String TAG = WeatherUtil.class.getSimpleName();

    public static void loadWeather(String name, final OnWeatherListener listener) {
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + name + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = "+weather.status);
                if (weather != null && "ok".equals(weather.status)) {
                    if (listener != null) {
                        listener.onSuccess(weather);
                    }
                } else {
                    if (listener != null) {
                        listener.onError();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onError();
                }
            }
        });
    }

    public static void loadWeatherAqi(String cityName, final OnWeatherListener listener) {
        String weatherUrl = "https://free-api.heweather.com/s6/air/now?location=" + cityName + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = "+weather.status);
                if (weather != null && "ok".equals(weather.status)) {
                    if (listener != null) {
                        listener.onSuccessAqi(weather);
                    }
                } else {
                    if (listener != null) {
                        listener.onError();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onError();
                }
            }
        });
    }
}
