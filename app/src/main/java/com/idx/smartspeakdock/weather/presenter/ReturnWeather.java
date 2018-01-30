package com.idx.smartspeakdock.weather.presenter;

import com.idx.smartspeakdock.weather.model.weather.Weather;

/**
 * Created by ryan on 18-1-15.
 * Email: Ryan_chan01212@yeah.net
 */
//监听从网络获取Weather接口
public interface ReturnWeather {
    void onReturnWeather(Weather weather);
    void onReturnWeatherError();
}
