package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.idx.calendarview.CalendarView;
import com.idx.calendarview.LunarCalendar;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.unit.listener.ICalenderVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.calendar.TimeData;
import com.idx.smartspeakdock.calendar.Util;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.music.service.MusicService;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ryan on 18-1-16.
 * Email: Ryan_chan01212@yeah.net
 */

public class ControllerService extends Service {
    public final String TAG = "ControllerService";
    ShoppingCallBack mShoppingCallBack;
    CalendarCallBack mCalendarCallBack;
    MusicCallBack mMusicCallBack;
    CalendarView mCalendarView;
    Util util;

    MusicListFragment musicListFragment;

    String answer;
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
        mCalendarView = new CalendarView(getApplicationContext());
        util = new Util(getApplicationContext(),mCalendarView);
        musicListFragment=new MusicListFragment();


    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new MyBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind: ");
        super.onRebind(intent);
    }

    public class MyBinder extends Binder implements ControllerServiceListener {

        @Override
        public void onReturnWeburl(ShoppingCallBack shoppingCallBack) {
            mShoppingCallBack = shoppingCallBack;
        }
        //日历
        @Override
        public void setCalendarControllerListener(CalendarCallBack calendarCallBack) {
            mCalendarCallBack = calendarCallBack;
        }

        @Override
        public void onGetMusicName(MusicCallBack musicCallBack) {
            mMusicCallBack=musicCallBack;
        }

        @Override
        public void onTop(boolean isTopActivity, Fragment isTopFragment) {}
    }
    //注册购物语音模块
    public void registerShoppingModule(){
        UnitManager.getInstance(getApplicationContext()).setShoppingVoiceListener(new IShoppingVoiceListener() {
            @Override
            public void openSpecifyWebsites(String web_sites_url) {
                if (mShoppingCallBack != null){
                    mShoppingCallBack.onShoppingCallback(web_sites_url);
                }
            }
        });
    }
/*
* 注册日历语音模块
* */
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
                mMusicCallBack.onMusicCallBack(name);
                musicListFragment.getService().play(name);
                
            }

            @Override
            public void onPause() {
                   musicListFragment.getService().pause();
            }

            @Override
            public void onContinue() {
                   musicListFragment.getService().continuePlay();
            }

            @Override
            public void onNext() {
                    musicListFragment.getService().next();

            }

            @Override
            public void onPrevious() {
                    musicListFragment.getService().pre();

            }
        });
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: ");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        //注册购物语音模块
        registerShoppingModule();
        //注册日历语音模块
        registerCalendarModule();
        //注册音乐语音模块
        registerMusicModule();
        return super.onStartCommand(intent, flags, startId);
    }
    public void ifmCalendarCallBackNoNull(){
        if (mCalendarCallBack != null){
            mCalendarCallBack.onCalendarCallBack();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mShoppingCallBack != null){
            mShoppingCallBack = null;
        }
        if (mCalendarCallBack != null){
            mCalendarCallBack = null;
        }
        if (mMusicCallBack!=null){
            mMusicCallBack=null;
        }
    }
}
