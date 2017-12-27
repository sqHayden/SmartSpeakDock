package com.idx.smartspeakdock.calendar.model;

import android.util.Log;

import com.idx.calendarview.MessageEvent;
import com.idx.smartspeakdock.calendar.bean.Schedule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public class Model implements Imodel {
    Schedule schedule;
    String date;
    Integer day;
    public Model() {
        EventBus.getDefault().register(this);
    }
    @Override
    public void setdata(int hour, int minute, String event) {
        Connector.getDatabase();
        schedule = new Schedule();
        schedule.setDate(date);
        schedule.setDay(day);
        schedule.setTime(hour + ":" + minute);
        schedule.setEvent(event);
        schedule.save();

    }
    @Subscribe
    public void onEvent(MessageEvent messageEvent){
        Log.v("1218","onevent:" + messageEvent.getMessage());
        date = messageEvent.getMessage();
        day = messageEvent.getday();
    }
    @Override
    public List<Schedule> getdata() {
        List<Schedule> list = DataSupport.findAll(Schedule.class);
        return list;
    }
}
