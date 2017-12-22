package com.idx.smartspeakdock.weather.model;

import com.idx.smartspeakdock.standby.HttpUtil;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by danny on 12/21/17.
 */

public class WeatherModelImpl implements WeatherModel {

    /**
     * 加载城市天气
     *
     * @param name     城市名称
     * @param listener 获取数据结果监听
     */
    @Override
    public void loadWeather(String name, final OnWeatherListener listener) {
        String address = "https://free-api.heweather.com/s6/weather?location=" + name + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    listener.onSuccess(weather);
                } else {
                    listener.onError();
                }
            }
        });
    }

    /**
     * 加载城市空气质量
     *
     * @param cityName 城市名称
     * @param listener 获取数据结果监听
     */
    @Override
    public void loadWeatherAqi(String cityName, final OnWeatherListener listener) {
        String address = "https://free-api.heweather.com/s6/air/now?location=" + cityName + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    listener.onSuccessAqi(weather);
                } else {
                    listener.onError();
                }
            }
        });
    }
}
