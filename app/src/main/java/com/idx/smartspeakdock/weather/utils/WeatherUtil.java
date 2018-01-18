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
import com.idx.smartspeakdock.weather.presenter.ReturnWeather;

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
    //537664b7e2124b3c845bc0b51278d4af
    //bc0418b57b2d4918198d3974ac1285d9  测试
    private static String key="537664b7e2124b3c845bc0b51278d4af";
    private static WeatherBasicRepository mWeatherBasicRepository;
    private static WeatherAqiRepository mWeatherAqiRepository;

    public static void loadWeather(final String name, final ReturnWeather returnWeather) {
//        mWeather = null;
        mWeatherBasicRepository= WeatherBasicInjection.getNoteRepository(SpeakerApplication.getContext());
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + name + "&key="+key;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                mWeather = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = " + mWeather.status);
                //天气获取成功，则保存
                if (mWeather != null && "ok".equals(mWeather.status)) {
                    if(returnWeather != null) {
                        returnWeather.onReturnWeather(mWeather);
                    }
                    Log.d(TAG, "onResponse: 成功");
                    WeatherBasic basic=new WeatherBasic();
                    basic.cityName=name;
                    basic.date=new Date().toString();
                    basic.weatherBasic=responseText;
                    mWeatherBasicRepository.addWeatherBasic(basic);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(returnWeather != null){
                    returnWeather.onReturnWeather(null);
                }
            }
        });
//        return mWeather;
    }


    public static void loadWeatherAqi(final String cityName,final ReturnWeather returnWeather) {
        mWeatherAqiRepository= WeatherAqiInjection.getInstance(SpeakerApplication.getContext());
        String weatherUrl = "https://free-api.heweather.com/s6/air/now?location=" + cityName + "&key="+key;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                mWeatherAqi = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = " + mWeatherAqi.status);
                //天气获取成功，则保存
                if (mWeatherAqi != null && "ok".equals(mWeatherAqi.status)) {
                    if (returnWeather != null){
                        returnWeather.onReturnWeather(mWeatherAqi);
                    }
                    Log.d(TAG, "onResponse: 成功");
                    WeatherAqi aqi=new WeatherAqi();
                    aqi.cityName=cityName;
                    aqi.date=new Date().toString();
                    aqi.weatherAqi=responseText;
                    mWeatherAqiRepository.addWeatherAqi(aqi);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (returnWeather != null){
                    returnWeather.onReturnWeather(null);
                }
            }
        });
    }


}
