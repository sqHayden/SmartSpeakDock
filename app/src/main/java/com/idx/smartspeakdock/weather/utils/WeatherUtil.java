package com.idx.smartspeakdock.weather.utils;

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

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by ryan on 17-12-23.
 * Email: Ryan_chan01212@yeah.net
 */

public class WeatherUtil {
    private static final String TAG = WeatherUtil.class.getSimpleName();
    private static Weather mWeather;
    private static Weather mWeatherAqi;

    private static WeatherBasicRepository mWeatherBasicRepository;
    private static WeatherAqiRepository mWeatherAqiRepository;

    public static Weather loadWeather(final String name) {
//        mWeather = null;
        mWeatherBasicRepository= WeatherBasicInjection.getNoteRepository(SpeakerApplication.getContext());
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + name + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                mWeather = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = " + mWeather.status);
                //天气获取成功，则保存
                if (mWeather != null && "ok".equals(mWeather.status)) {
                    Log.d(TAG, "onResponse: 成功");
                    WeatherBasic basic=new WeatherBasic();
                    basic.cityName=name;
                    basic.date=new Date().toString();
                    basic.weatherBasic=responseText;
                    mWeatherBasicRepository.addWeatherBasic(basic);
                }else {
                    Log.d(TAG, "onResponse: 失败");
                }
                /*if (mWeather != null && "ok".equals(mWeather.status)) {
                    Log.i(TAG, "onResponse: mWeather.toString() = "+mWeather.toString());
                } else {
                    mWeather = null;
                }*/
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mWeather = null;
            }
        });
        return mWeather;
    }

    public static Weather loadWeatherAqi(final String cityName) {
//        mWeatherAqi = null;
        mWeatherAqiRepository= WeatherAqiInjection.getInstance(SpeakerApplication.getContext());
        String weatherUrl = "https://free-api.heweather.com/s6/air/now?location=" + cityName + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                mWeatherAqi = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = " + mWeatherAqi.status);
                //天气获取成功，则保存
                if (mWeatherAqi != null && "ok".equals(mWeatherAqi.status)) {
                    Log.d(TAG, "onResponse: 成功");
                    WeatherAqi aqi=new WeatherAqi();
                    aqi.cityName=cityName;
                    aqi.date=new Date().toString();
                    aqi.weatherAqi=responseText;
                    mWeatherAqiRepository.addWeatherAqi(aqi);
                } else {
                    Log.d(TAG, "onResponse: 失败");
                }
                /*if (weather != null && "ok".equals(weather.status)) {
                    mWeatherAqi = weather;
                } else {
                    mWeatherAqi = null;
                }*/
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                mWeatherAqi = null;
            }
        });
        return mWeatherAqi;
    }
}
