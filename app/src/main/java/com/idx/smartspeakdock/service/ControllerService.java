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
import com.idx.smartspeakdock.calendar.TimeData;
import com.idx.smartspeakdock.calendar.Util;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
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
    CalendarView mCalendarView;
    String answer;
    private final int msgKey1 = 1;
    private String currenttime;
    private Util util;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }
    private  Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case msgKey1:
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    currenttime = format.format(date);
                    break;
                default:
                    break;
            }
        }
    };
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
        util = new Util(getApplicationContext());
        UnitManager.getInstance(getApplicationContext()).setCalenderVoiceListener(new ICalenderVoiceListener() {
            @Override
            public String onWeekInfo(String time) {
                if (mCalendarCallBack != null){
                    mCalendarCallBack.onCalendarCallBack();
                }
                answer="";
                switch (mCalendarView.getWeek(time)){
                    case 1:
                        answer = time+ getString(R.string.monday);
                        break;
                    case 2:
                        answer = time+ getString(R.string.tuesday);
                        break;
                    case 3:
                        answer = time+ getString(R.string.wednesday);
                        break;
                    case 4:
                        answer = time+ getString(R.string.thursday);
                        break;
                    case 5:
                        answer = time+ getString(R.string.friday);
                        break;
                    case 6:
                        answer = time+ getString(R.string.saturday);
                        break;
                    case 7:
                        answer = time+ getString(R.string.sunday);
                        break;
                    default:
                        break;
                }
                return answer;
            }

            @Override
            public String onTimeInfo() {
                if (mCalendarCallBack != null){
                    mCalendarCallBack.onCalendarCallBack();
                }
                answer = "";
                answer = getString(R.string.now)+currenttime;
                return answer;
            }

            @Override
            public String onFestivalInfo(String time) {
                if (mCalendarCallBack != null){
                    mCalendarCallBack.onCalendarCallBack();
                }
                answer="";
                switch (time){
                    case TimeData.YESTERDAY:
                        answer = util.getFestivalInfogetActInfo(time,mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                        break;
                    case TimeData.TODAY:
                        answer =  util.getFestivalInfogetActInfo(time,mCalendarView.getCurYear(),mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                        break;
                    case TimeData.TOMORROW:
                        answer =  util.getFestivalInfogetActInfo(time,mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                        break;
                    default:
                        break;

                }
                return answer;
            }

            @Override
            public String onActInfo(String time) {
                if (mCalendarCallBack != null){
                    mCalendarCallBack.onCalendarCallBack();
                }
                answer="";
                switch (time){
                    case TimeData.YESTERDAY:
                        answer =  util.getActInfo(time,mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                        break;
                    case TimeData.TODAY:
                        answer =  util.getActInfo(time,mCalendarView.getCurYear(),mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                        break;
                    case TimeData.TOMORROW:
                        answer =  util.getActInfo(time,mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                        break;
                    default:
                        break;
                }
                return answer;
            }

            @Override
            public String onDateInfo(String time) {
                if (mCalendarCallBack != null){
                    mCalendarCallBack.onCalendarCallBack();
                }
                answer="";
                switch (time){
                    case TimeData.YESTERDAY:
                        answer = time + mCalendarView.getYesData().get("month") + getString(R.string.month) + mCalendarView.getYesData().get("day") + getString(R.string.day);
                        break;
                    case TimeData.TODAY:
                        answer = time + mCalendarView.getCurMonth() + getString(R.string.month) + mCalendarView.getCurDay() + getString(R.string.day);
                        break;
                    case TimeData.TOMORROW:
                        answer = time + mCalendarView.getTomoData().get("month") + getString(R.string.month) + mCalendarView.getTomoData().get("day") + getString(R.string.day);
                        break;
                    default:
                        break;

                }
                return answer;
            }

            @Override
            public String onLunarDateInfo(String time) {
                if (mCalendarCallBack != null){
                    mCalendarCallBack.onCalendarCallBack();
                }
                answer="";
                switch (time){
                    case TimeData.YESTERDAY:
                        answer = time + getString(R.string.lunar_calendar)+ LunarCalendar.solarToLunar(mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                        break;
                    case TimeData.TODAY:
                        answer = time+getString(R.string.lunar_calendar) +mCalendarView.getLunar();
                        break;
                    case TimeData.TOMORROW:
                        answer = time + getString(R.string.lunar_calendar)+LunarCalendar.solarToLunar(mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                        break;
                    default:
                        break;

                }
                return answer;
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
        mCalendarView = new CalendarView(getApplicationContext());
        //获取当前时间
        getcurrenttime();
        //注册购物语音模块
        registerShoppingModule();
        //注册日历语音模块
        registerCalendarModule();
        return super.onStartCommand(intent, flags, startId);
    }
    public  void getcurrenttime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                do{
                    try {
                        Thread.sleep(1000);
                        Message msg = new Message();
                        msg.what = msgKey1;
                        mHandler.sendMessage(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }while (true);
            }
        }).start();
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
    }
}
