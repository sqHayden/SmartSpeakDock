package com.idx.smartspeakdock.weather.utils;

import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.standby.HttpUtil;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqi;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiRepository;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by danny on 1/8/18.
 */

public class UpdateWeatherUtil {

    private static final String TAG = WeatherUtil.class.getSimpleName();

    private static WeatherBasicRepository mWeatherBasicRepository;
    private static WeatherAqiRepository mWeatherAqiRepository;

    //更新数据库数据
    public static void updateWeather(/*final String currentCity*/) {
//        mWeather = null;
        mWeatherBasicRepository = WeatherBasicInjection.getNoteRepository(SpeakerApplication.getContext());
        mWeatherBasicRepository.getWeatherBasics(new WeatherBasicDataSource.LoadWeatherBasicsListCallback() {
            @Override
            public void onWeatherBasicsLoaded(List<WeatherBasic> weatherBasic) {
                for (WeatherBasic basic : weatherBasic) {
                    if (new Date().getTime() - new Date(basic.date).getTime() >= 1000 * 60 * 30) {
                        String name = basic.cityName;
                        mWeatherBasicRepository.deleteWeatherBasic(name);
                        saveBasic(name);
                    }
                }
            }

            @Override
            public void onDataNotAvailable() {
//                saveBasic(currentCity);
            }
        });

        mWeatherAqiRepository = WeatherAqiInjection.getInstance(SpeakerApplication.getContext());
        mWeatherAqiRepository.getWeatherAqis(new WeatherAqiDataSource.LoadWeatherAqisListCallback() {
            @Override
            public void onWeatherAqisListLoaded(List<WeatherAqi> weatherAqis) {
                for (WeatherAqi weatherAqi : weatherAqis) {
                    if ((new Date().getTime() - new Date(weatherAqi.date).getTime()) >= 1000 * 60 * 30) {
                        String name = weatherAqi.cityName;
                        mWeatherAqiRepository.deleteWeatherAqi(name);
                        saveAqi(name);
                    }
                }
            }

            @Override
            public void onDataNotAvailable() {
//                saveAqi(currentCity);
            }
        });
    }

    //网络获取数据保存到本地
    private static void saveBasic(final String name) {
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + name + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                Weather weather= Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    WeatherBasic basic = new WeatherBasic();
                    basic.cityName = name;
                    basic.date = new Date().toString();
                    basic.weatherBasic = responseText;
                    mWeatherBasicRepository.addWeatherBasic(basic);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    //网络获取数据保存到本地
    private static void saveAqi(final String name) {
        String weatherUrl = "https://free-api.heweather.com/s6/air/now?location=" + name + "&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                Weather weather=Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    WeatherAqi aqi = new WeatherAqi();
                    aqi.cityName = name;
                    aqi.date = new Date().toString();
                    aqi.weatherAqi = responseText;
                    mWeatherAqiRepository.addWeatherAqi(aqi);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
