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
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IWeatherVoiceListener;
import com.idx.smartspeakdock.calendar.Util;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.service.MusicCallBack;
//import com.idx.smartspeakdock.music.service.MusicVoice;
import com.idx.smartspeakdock.music.service.MusicPlay;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;

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
    ReturnVoice mWeather_return_voice;
    MusicCallBack mMusicCallBack;
    CalendarView mCalendarView;
    Util util;
    MusicPlay musicPlay;

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

    public ReturnVoice getReturnVoice(){
        return mWeather_return_voice;
    }

    //注册购物语音模块
    public void registerShoppingModule() {
        UnitManager.getInstance(getApplicationContext()).setShoppingVoiceListener(new IShoppingVoiceListener() {
            @Override
            public void openSpecifyWebsites(String web_sites_url) {
                if (mShoppingCallBack != null) {
                    mShoppingCallBack.onShoppingCallback(web_sites_url);
                }
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
                answer = getString(R.string.now)+Util.getCurrentTime();
                return answer;
            }

            @Override
            public String onFestivalInfo(String time) {
                ifmCalendarCallBackNoNull();
                return util.getFestivalInfo(time);
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
            public void onPlay(String name) {
                if (mMusicCallBack!=null) {
                    mMusicCallBack.onMusicCallBack(name);
                     musicPlay.play(name);
                }
            }

            @Override
            public void onPause() {
                 musicPlay.pause();

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
        return super.onStartCommand(intent, flags, startId);
    }
    //注册天气语音模块
    private void registerWeatherModule() {
        UnitManager.getInstance(getApplicationContext()).setWeatherVoiceListener(new IWeatherVoiceListener() {
            @Override
            public void onWeatherInfo(String cityName,ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onWeatherInfo",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onRangeTempInfo(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onRangeTempInfo", GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onAirQualityInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onAirQualityInfo",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onCurrentTempInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onCurrentTempInfo",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onWeatherStatus(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onWeatherStatus",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onRainInfo(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onRainInfo",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onDressInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onDressInfo",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onUitravioletLevelInfo(String cityName, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,"",returnVoice,"onUitravioletLevelInfo",GlobalUtils.WEATHER_VOICE_FLAG);
                }
            }

            @Override
            public void onSmogInfo(String cityName, String time, ReturnVoice returnVoice) {
                if(mWeatherCallback != null){
                    mWeather_return_voice = returnVoice;
                    mWeatherCallback.onWeatherCallback(cityName,time,returnVoice,"onSmogInfo",GlobalUtils.WEATHER_VOICE_FLAG);
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
        if (mMusicCallBack!=null){
            mMusicCallBack=null;
        }
        if (mWeatherCallback != null){
            mWeatherCallback = null;
        }
        if (mWeather_return_voice != null){
            mWeather_return_voice = null;
        }
        if (mCalendarView != null){
            mCalendarView = null;
        }
        if (util != null){
            util = null;
        }
        if (musicPlay != null){
            musicPlay = null;
        }
    }
}
