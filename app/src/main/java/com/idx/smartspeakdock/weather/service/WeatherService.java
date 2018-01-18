/*
package com.idx.smartspeakdock.weather.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IWeatherVoiceListener;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqi;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiRepository;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.ReturnWeather;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;
import com.idx.smartspeakdock.weather.utils.WeatherUtil;

*/
/**
 * Created by danny on 12/15/17.
 *//*


public class WeatherService extends Service {
    public static final String TAG = WeatherService.class.getSimpleName();
    private String mCurrentCity = "";
    private String voice_answer;
    private Weather voice_weather;
    private Weather voice_aqi;
    private WeatherBinder mWeatherBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new WeatherBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWeatherBinder = new WeatherBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        voiceResult();
        return super.onStartCommand(intent, flags, startId);
    }

    //语音赋值当前城市名
    private void voiceCityName(String cityName) {
        mCurrentCity = cityName;
    }

    //语音接口实现
    private void voiceResult() {
        voice_answer = "";
        UnitManager.getInstance(this).setWeatherVoiceListener(new IWeatherVoiceListener() {
            @Override
            public void onWeatherInfo(String cityName) {
                voiceCityName(cityName);
                Log.i(TAG, "onWeatherInfo: cityName = " + cityName);
                mWeatherBinder.getWeatherBasic(cityName, "", null, "");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onRangeTempInfo(String cityName, String time, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, time, returnVoice, "onRangeTempInfo");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onAirQualityInfo(String cityName, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherAqi(cityName, "", returnVoice, "onAirQualityInfo");
                mWeatherBinder.getWeatherBasic(cityName, "", null, "");
            }

            @Override
            public void onCurrentTempInfo(String cityName, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, "", returnVoice, "onCurrentTempInfo");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onWeatherStatus(String cityName, String time, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, time, returnVoice, "onWeatherStatus");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onRainInfo(String cityName, String time, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, time, returnVoice, "onRainInfo");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onDressInfo(String cityName, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, "", returnVoice, "onDressInfo");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onUitravioletLevelInfo(String cityName, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, "", returnVoice, "onUitravioletLevelInfo");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }

            @Override
            public void onSmogInfo(String cityName, String time, ReturnVoice returnVoice) {
                voiceCityName(cityName);
                mWeatherBinder.getWeatherBasic(cityName, time, returnVoice, "onSmogInfo");
                mWeatherBinder.getWeatherAqi(cityName, "", null, "");
            }
        });
    }

    //判断所问问题类型(温度、穿衣、紫外线等等)
    private void voiceReturnJudge(String cityName, String time, ReturnVoice returnVoice, String funcTag) {
        if (returnVoice != null) {
            switch (funcTag) {
                case "onRangeTempInfo":
                    rangeTempInfo(cityName, time, returnVoice);
                    break;
                case "onAirQualityInfo":
                    airQualityInfo(cityName, returnVoice);
                    break;
                case "onCurrentTempInfo":
                    currentTempInfo(cityName, returnVoice);
                    break;
                case "onWeatherStatus":
                    weatherStatus(cityName, time, returnVoice);
                    break;
                case "onRainInfo":
                    rainInfo(cityName, time, returnVoice);
                    break;
                case "onDressInfo":
                    dressInfo(returnVoice);
                    break;
                case "onUitravioletLevelInfo":
                    uitravioletLevelInfo(cityName, returnVoice);
                    break;
                case "onSmogInfo":
                    smogInfo(cityName, time, returnVoice);
                    break;
                default:
                    break;
            }
        }
    }

    //温度是多少
    private void rangeTempInfo(String cityName, String time, ReturnVoice returnVoice) {
        Log.d(TAG, "rangeTempInfo: 温度信息" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            Log.i(TAG, "rangeTempInfo: city = " + voice_weather);
            judgeRangeTempInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "温度信息失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }

    //温度相关方法
    private void judgeRangeTempInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(0).max + "度,最低温度为" + voice_weather.forecastList.get(0).min + "度";
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(1).max + "度,最低温度为" + voice_weather.forecastList.get(1).min + "度";
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(2).max + "度,最低温度为" + voice_weather.forecastList.get(2).min + "度";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    //空气质量
    private void airQualityInfo(String cityName, ReturnVoice returnVoice) {
        Log.i(TAG, "airQualityInfo: cityName = " + cityName);
        if (voice_aqi != null && voice_aqi.status.equals("ok")) {
            voice_answer = cityName + "空气质量为" + voice_aqi.air.qlty;
        } else {
            voice_answer = "查询" + cityName + "空气质量信息失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }

    //当前温度
    private void currentTempInfo(String cityName, ReturnVoice returnVoice) {
        Log.d(TAG, "currentTempInfo: 当前温度" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            Log.i(TAG, "currentTempInfo: city = " + voice_weather.basic.cityName);
            voice_answer = cityName + "当前温度为" + voice_weather.now.tmperature + "度";
        } else {
            voice_answer = "查询" + cityName + "当前温度信息失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }

    //天气状况
    private void weatherStatus(String cityName, String time, ReturnVoice returnVoice) {
        Log.d(TAG, "weatherStatus: 天气状况" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            judgeStatusInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "天气状况失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }

    //天气状况相关方法
    private void judgeStatusInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code)) + "天";
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)) + "天";
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)) + "天";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    //是否有雨
    private void rainInfo(String cityName, String time, ReturnVoice returnVoice) {
        Log.d(TAG, "rainInfo: 下雨" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            judgeRainInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "是否下雨失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }
    //雨相关方法
    private void judgeRainInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                rainResult(cityName, time);
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                rainResult(cityName, time);
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                rainResult(cityName, time);
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    //雨相关方法
    private void rainResult(String cityName, String time) {
        if (judgeRain(time)) {
            voice_answer = time + cityName + "有雨";
        } else {
            voice_answer = time + cityName + "没有雨";
        }
    }

    //雨相关方法
    private boolean judgeRain(String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code)).equals("雨") ? true : false;
            case GlobalUtils.WEATHER_TIME_TOMM:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)).equals("雨") ? true : false;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)).equals("雨") ? true : false;
            default:
                return false;
        }
    }

    //穿衣
    private void dressInfo(ReturnVoice returnVoice) {
        Log.d(TAG, "dressInfo: 穿衣指数" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            voice_answer = voice_weather.lifestyleList.get(1).txt;
        } else {
            voice_answer = "查询穿衣指数失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }

    //紫外线
    private void uitravioletLevelInfo(String cityName, ReturnVoice returnVoice) {
        Log.d(TAG, "uitravioletLevelInfo: 紫外线强度" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            voice_answer = cityName + "紫外线强度" + voice_weather.lifestyleList.get(5).brf;
        } else {
            voice_answer = "查询" + cityName + "紫外线强度失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }

    //雾
    private void smogInfo(String cityName, String time, ReturnVoice returnVoice) {
        Log.d(TAG, "smogInfo: 雾霾" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            String weather_type = HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code));
            if (weather_type.equals("雾")) {
                voice_answer = time + cityName + "有雾霾";
            } else {
                voice_answer = time + cityName + "没有雾霾";
            }
        } else {
            voice_answer = "查询" + cityName + "雾霾信息失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }


    class WeatherBinder extends Binder {
        private WeatherBasicRepository mWeatherBasicRepository;
        private WeatherAqiRepository mWeatherAqiRepository;
        WeatherFragment mWeatherFragment = WeatherFragment.newInstance();

        WeatherService getService() {
            return WeatherService.this;
        }

        //优先加载本地天气数据
        private void getWeatherBasic(final String cityName, final String time, final ReturnVoice returnVoice, final String funcTag) {
            mWeatherBasicRepository = WeatherBasicInjection.getNoteRepository(WeatherService.this);
            mWeatherBasicRepository.getWeatherBasic(cityName + "%", new WeatherBasicDataSource.LoadWeatherBasicsCallback() {
                @Override
                public void onWeatherBasicsLoaded(WeatherBasic weatherBasic) {
                    Log.d(TAG, "onWeatherBasicsLoaded: 加载数据库天气数据");
                    String weatherBasicInfo = weatherBasic.weatherBasic;
                    voice_weather = Utility.handleWeatherResponse(weatherBasicInfo);
                    mWeatherFragment.setWeatherInfo(voice_weather);
                    voiceReturnJudge(cityName, time, returnVoice, funcTag);
                }

                @Override
                public void onDataNotAvailable() {
                    Log.d(TAG, "onDataNotAvailable: 加载网络天气数据");
                    WeatherUtil.loadWeather(cityName, new ReturnWeather() {
                        @Override
                        public void onReturnWeather(Weather weather) {
                            if (weather != null) {
                                voice_weather = weather;
                                mWeatherFragment.setWeatherInfo(weather);
                                voiceReturnJudge(cityName, time, returnVoice, funcTag);
                            }
                        }
                    });
                }
            });
        }

        //优先加载本地空气质量数据
        private void getWeatherAqi(final String cityName, final String time, final ReturnVoice returnVoice, final String funcTag) {
            mWeatherAqiRepository = WeatherAqiInjection.getInstance(WeatherService.this);
            mWeatherAqiRepository.getWeatherAqi(cityName + "%", new WeatherAqiDataSource.LoadWeatherAqisCallback() {
                @Override
                public void onWeatherAqisLoaded(WeatherAqi weatherAqi) {
                    Log.d(TAG, "onWeatherAqisLoaded: 加载数据库空气质量信息");
                    String weatherAqiInfo = weatherAqi.weatherAqi;
                    voice_aqi = Utility.handleWeatherResponse(weatherAqiInfo);
                    mWeatherFragment.setWeatherAqi(voice_aqi);
                    voiceReturnJudge(cityName, time, returnVoice, funcTag);
                }

                @Override
                public void onDataNotAvailable() {
                    WeatherUtil.loadWeatherAqi(cityName, new ReturnWeather() {
                        @Override
                        public void onReturnWeather(Weather weather) {
                            if (weather != null) {
                                voice_aqi = weather;
                                mWeatherFragment.setWeatherAqi(weather);
                                voiceReturnJudge(cityName, time, returnVoice, funcTag);
                            }
                        }
                    });
                }
            });
        }
    }
}
*/
