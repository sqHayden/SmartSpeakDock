package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.idx.calendarview.CalendarView;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.ICalenderVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMapVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IWeatherVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.calendar.Util;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.music.service.MusicPlay;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;

//import com.idx.smartspeakdock.music.service.MusicVoice;

/**
 * Created by ryan on 18-1-16.
 * Email: Ryan_chan01212@yeah.net
 */

public class ControllerService extends Service {
    public final String TAG = "ControllerService";
    private final int msgKey1 = 1;
    ShoppingCallBack mShoppingCallBack;
    CalendarCallBack mCalendarCallBack;
    WeatherCallback mWeatherCallback;
    MapCallBack mMapCallBack;
    ReturnVoice mWeather_return_voice;
    ResultCallback mMap_result_callback;
    MusicCallBack mMusicCallBack;
    CalendarView mCalendarView;
    Util util;
    public MusicPlay musicPlay;

    String answer;
    @Override
    public void onCreate() {
        super.onCreate();
        mCalendarView = new CalendarView(getApplicationContext());
        util = new Util(getApplicationContext(),mCalendarView);
        musicPlay=new MusicPlay(getApplicationContext());
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder implements IControllerServiceListener {
        @Override
        public ControllerService getControlService() {
            return ControllerService.this;
        }

        //购物
        @Override
        public void onReturnWeburl(ShoppingCallBack shoppingCallBack) {
            mShoppingCallBack = shoppingCallBack;
        }

        //日历
        @Override
        public void setCalendarControllerListener(CalendarCallBack calendarCallBack) {
            mCalendarCallBack = calendarCallBack;
        }
        //音乐
        @Override
        public void onGetMusicName(MusicCallBack musicCallBack) {
            mMusicCallBack=musicCallBack;
        }

        //地图
        @Override
        public void setMapControllerListener(MapCallBack mapCallBack) {
            mMapCallBack = mapCallBack;
        }

        //天气
        @Override
        public void setWeatherControllerListener(WeatherCallback weatherCallback) {
            mWeatherCallback = weatherCallback;
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    //获取天气的语音接口对象
    public ReturnVoice getReturnVoice(){
        return mWeather_return_voice;
    }

    //获取地图的语音接口对象
    public ResultCallback getResultCallBack(){
        return mMap_result_callback;
    }

    //注册购物语音模块
    public void registerShoppingModule() {
        UnitManager.getInstance(getApplicationContext()).setShoppingVoiceListener(new IShoppingVoiceListener() {
            @Override
            public String openSpecifyWebsites(String web_sites_url) {
                if (mShoppingCallBack != null) {
                    mShoppingCallBack.onShoppingCallback(web_sites_url);
                }
                return "好的,即将为你开启";
            }
        });
    }

    //注册日历语音模块
    public void registerCalendarModule(){
        UnitManager.getInstance(getApplicationContext()).setCalenderVoiceListener(new ICalenderVoiceListener() {
            @Override
            public String onWeekInfo(String time) {
                ifmCalendarCallBackNoNull();
                return mCalendarView.getWeeks(time);
            }

            @Override
            public String onTimeInfo() {
                ifmCalendarCallBackNoNull();
                answer = "";
                answer = "现在"+Util.getCurrentTime();
                return answer;
            }

            @Override
            public String onFestivalInfo(String time) {
                ifmCalendarCallBackNoNull();
                return util.getFestivalInfo(time);
            }

            @Override
            public String onFestivalDate(String name) {
                return null;
            }

            @Override
            public String onActInfo(String time) {
                ifmCalendarCallBackNoNull();
                return util.getActionInfo(time);
            }

            @Override
            public String onDateInfo(String time) {
                ifmCalendarCallBackNoNull();
                return util.getDate(time);
            }

            @Override
            public String onLunarDateInfo(String time) {
                ifmCalendarCallBackNoNull();
                return util.getLunarDateInfo(time);
            }
        });
    }

    //注册音乐语音模块
    public void  registerMusicModule(){
        UnitManager.getInstance(getApplicationContext()).setMusicVoiceListener(new IMusicVoiceListener() {
            @Override
            public void onPlay(int index) {
            }

            @Override
            public void onPlay(String name, ResultCallback resultCallback) {

            }

//            @Override
//            public void onPlay(String name) {
//                if (mMusicCallBack!=null) {
//                    mMusicCallBack.onMusicCallBack(name);
//                     musicPlay.play(name);
//                }
//            }

            @Override
            public void onPause() {
                 musicPlay.pause();

            }

            @Override
            public void onStop() {
                Log.d(TAG, "onStop: music");

            }

            @Override
            public void onContinue() {
                 musicPlay.continuePlay();

            }

            @Override
            public void onNext() {
                 musicPlay.next();
            }

            @Override
            public void onPrevious() {
                 musicPlay.pre();
            }
        });
    }

    //注册地图语音模块
    public void registerMapModule(){
        UnitManager.getInstance(getApplicationContext()).setMapVoiceListener(new IMapVoiceListener() {
            /**
             * 语音实现
             ***/
            @Override
            public void onLocationInfo(ResultCallback result) {
                if(mMapCallBack!=null){
                    mMap_result_callback = result;
                    mMapCallBack.onMapCallBack("","","","",null,result);
                }
            }

            @Override
            public void onSearchInfo(String name, ResultCallback result) {
                if(mMapCallBack!=null){
                    mMap_result_callback = result;
                    mMapCallBack.onMapCallBack(name,"","","",null,result);
                }
            }

            @Override
            public void onSearchAddress(String address, ResultCallback result) {
                if(mMapCallBack!=null){
                    mMap_result_callback = result;
                    mMapCallBack.onMapCallBack("",address,"","",null,result);
                }
            }

            @Override
            public void onPathInfo(String fromAddress, String toAddress, String pathWay, ResultCallback result) {
                Log.d(TAG, "onPathInfo: ");
            }

//            @Override
//            public void onPathInfo(String fromAddress, String toAddress, PathWay pathWay, ResultCallback result) {
//                if(mMapCallBack!=null){
//                    mMap_result_callback = result;
//                    mMapCallBack.onMapCallBack("","",fromAddress,toAddress,pathWay,result);
//                }
//            }
        });
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //注册购物语音模块
        registerShoppingModule();
        //注册日历语音模块
        registerCalendarModule();
        //注册天气语音模块
        registerWeatherModule();
        //注册音乐语音模块
        registerMusicModule();
        //注册地图语音模块
        registerMapModule();
        return super.onStartCommand(intent, flags, startId);
    }
    //注册天气语音模块
    private void registerWeatherModule() {
        UnitManager.getInstance(getApplicationContext()).setWeatherVoiceListener(new IWeatherVoiceListener() {
            @Override
            public void onWeatherInfo(String cityName,ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onWeatherInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityWeatherInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityWeatherInfo: 城市名查今天天气信息");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onWeatherInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeWeatherINfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeWeatherINfo: 提供时间查深圳天气信息");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onWeatherInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onNoWeatherInfo(ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onNoWeatherInfo: 不提供城市和时间查今天深圳天气信息");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳","今天",returnVoice,"onWeatherInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onRangeTempInfo(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onRangeTempInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityRangeTempInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityRangeTempInfo: 提供城市查今天温度");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onRangeTempInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeRangeTempInfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeRangeTempInfo: 提供时间查深圳温度");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onRangeTempInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onNoRangeTempInfo(ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onNoRangeTempInfo: 不提供时间和城市查今天深圳温度");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳","今天",returnVoice,"onRangeTempInfo", GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onAirQualityInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onAirQualityInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityAirQualityInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityAirQualityInfo: 提供城市查空气质量");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onAirQualityInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeAirQualityInfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeAirQualityInfo: 提供时间查深圳空气质量");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onAirQualityInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onNoAiqQualityInfo(ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onNoAiqQualityInfo: 查今天深圳空气质量");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳","",returnVoice,"onAirQualityInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCurrentTempInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onCurrentTempInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onNoCurrentTempInfo(ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onNoCurrentTempInfo: 查深圳当前温度");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳","",returnVoice,"onCurrentTempInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onWeatherStatus(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onWeatherStatus",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityWeatherStatus(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityWeatherStatus: 提供城市查今天天气状况");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onWeatherStatus",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeWeatherStatus(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeWeatherStatus: 提供时间查深圳天气状况");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onWeatherStatus",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onNoWeatherStatus(ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onNoWeatherStatus: 查深圳今天天气状况");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳","今天",returnVoice,"onWeatherStatus",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onRainInfo(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onRainInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityRainInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityRainInfo: 提供城市查今天是否有雨");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onRainInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeRainInfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeRainInfo: 提供时间查今天深圳有雨吗");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onRainInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onNoRainInfo(ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onNoRainInfo: 查深圳今天有雨吗");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳","今天",returnVoice,"onRainInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onDressInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onDressInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityDressInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityDressInfo: 提供城市查今天穿衣建议");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onDressInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeDressInfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeDressInfo: 提供时间查深圳穿衣建议");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onDressInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onUitravioletLevelInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onUitravioletLevelInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCityUitravioletLevelInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCityUitravioletLevelInfo: 提空城市查今天空气质量");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onUitravioletLevelInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeUitravioletLevelInfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeUitravioletLevelInfo: 提供时间查深圳空气质量");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onUitravioletLevelInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onSmogInfo(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onSmogInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCitySmogInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onCitySmogInfo: 提供城市查今天深圳有雾吗");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"今天",returnVoice,"onSmogInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onTimeSmogInfo(String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    Log.d(TAG, "onTimeSmogInfo: 提供时间查深圳有雾吗");
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback("深圳",time,returnVoice,"onSmogInfo",GlobalUtils.Weather.WEATHER_VOICE_FLAG);
                }
            }
        });
    }


    public void ifmCalendarCallBackNoNull(){
        if (mCalendarCallBack != null){
            mCalendarCallBack.onCalendarCallBack();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mShoppingCallBack != null) {
            mShoppingCallBack = null;
        }
        if (mCalendarCallBack != null) {
            mCalendarCallBack = null;
        }
        if (mMusicCallBack != null) {
            mMusicCallBack = null;
        }
        if (mWeatherCallback != null) {
            mWeatherCallback = null;
        }
        if (mWeather_return_voice != null) {
            mWeather_return_voice = null;
        }
        if (mMapCallBack != null) {
            mMapCallBack = null;
        }
        if (mMap_result_callback != null) {
            mMap_result_callback = null;
            if (mCalendarView != null) {
                mCalendarView = null;
            }
            if (util != null) {
                util = null;
            }
            if (musicPlay != null) {
                musicPlay = null;
            }
        }
    }
}
