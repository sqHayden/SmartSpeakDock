package com.idx.smartspeakdock.calendar.presenter;

import android.content.Context;
import android.util.Log;
import com.idx.calendarview.CalendarView;
import com.idx.smartspeakdock.calendar.Iview;
import com.idx.smartspeakdock.calendar.bean.Schedule;
import com.idx.smartspeakdock.calendar.model.Model;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by geno on 20/12/17.
 */

public class Presenter implements Ipresenter{
    Iview iview;
    CalendarView calendarView;
    Model model = new Model();
    int mYear,mMonth,mDay;
    int hour,minutes;
    Context context;
    private static final int msgKey1 = 1;
    public Presenter(Iview iview,Context context, CalendarView calendarView) {
        super();
        this.iview = iview;
        this.context = context;
        this.calendarView = calendarView;
        init();
    }
    private void init(){
        mYear = calendarView.getCurYear();
        mMonth = calendarView.getCurMonth();
        mDay = calendarView.getCurDay();
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case msgKey1:
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    iview.showtime(format.format(date));
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void selectyear() {
      iview.showyear(mYear);
    }

    @Override
    public void selectmonth() {
      iview.showmonth(mYear,mMonth,mDay);
    }
    @Override
    public void setdate(String date,Integer day,int hours, int minutes, String event) {
        model.setdata(date,day,hours,minutes,event);
    }

    @Override
    public void deletedate(String date, Integer day, String event, String time) {
     model.deletedate(date,day,event,time);
    }

    @Override
    public void getdata() {
        List<Schedule> list = model.getdata();
    }

    @Override
    public void getcurrenttime() {
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
}
