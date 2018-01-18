package com.idx.smartspeakdock.weather.model;

import android.util.Log;

import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.standby.HttpUtil;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqi;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiRepository;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;
import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by danny on 12/21/17.
 */

public class WeatherModelImpl implements WeatherModel {
    // bc0418b57b2d4918819d3974ac1285d9     3000次 7天预报
    // 537664b7e2124b3c845bc0b51278d4af     1000次 3天预报
    public static final String TAG=WeatherModelImpl.class.getSimpleName();
    private static String key="bc0418b57b2d4918819d3974ac1285d9";
    private WeatherBasicRepository mWeatherBasicRepository;
    private WeatherAqiRepository mWeatherAqiRepository;
    /**
     * 加载城市天气
     *
     * @param name     城市名称
     * @param listener 获取数据结果监听
     */
    @Override
    public void loadWeather(final String name, final OnWeatherListener listener) {
        mWeatherBasicRepository= WeatherBasicInjection.getNoteRepository(SpeakerApplication.getContext());
        mWeatherBasicRepository.deleteWeatherBasic(name+"%");
        String address = "https://free-api.heweather.com/s6/weather?location=" + name + "&key="+key;
        Log.d(TAG, "loadWeather: 加载城市天气");
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener!=null) {
                    try {
                        listener.onError();
                    }catch (NullPointerException e1){
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather=Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    Log.d(TAG, "onResponse: 成功");
                    WeatherBasic basic=new WeatherBasic();
                    basic.cityName=name;
                    basic.weatherBasic=responseText;
                    basic.date=new Date().toString();
                    mWeatherBasicRepository.addWeatherBasic(basic);
                    if (listener!=null) {
                        try {
                            listener.onSuccess(weather);
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }else {
                    Log.d(TAG, "onResponse: 失败");
                    if (listener!=null) {
                        try {
                            listener.onError();
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
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
    public void loadWeatherAqi(final String cityName, final OnWeatherListener listener) {
        mWeatherAqiRepository= WeatherAqiInjection.getInstance(SpeakerApplication.getContext());
        mWeatherAqiRepository.deleteWeatherAqi(cityName+"%");
        String address = "https://free-api.heweather.com/s6/air/now?location=" + cityName + "&key="+key;
        Log.d(TAG, "loadWeatherAqi: 加载城市空气质量");
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener!=null) {
                    try {
                        listener.onError();
                    }catch (NullPointerException e1){
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather=Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    Log.d(TAG, "onResponse: 成功");
                    WeatherAqi aqi=new WeatherAqi();
                    aqi.cityName=cityName;
                    aqi.date=new Date().toString();
                    aqi.weatherAqi=responseText;
                    mWeatherAqiRepository.addWeatherAqi(aqi);
                    if (listener!=null) {
                        try {
                            listener.onSuccessAqi(weather);
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "onResponse: 失败");
                    if (listener!=null) {
                        try {
                            listener.onError();
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
