package com.idx.smartspeakdock.calendar.model;

import com.idx.smartspeakdock.calendar.bean.Schedule;

import org.litepal.tablemanager.Connector;

import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public class Model implements Imodel {
    Schedule schedule;
    @Override
    public void setdata(int hour, int minute, String event) {
        Connector.getDatabase();
        schedule = new Schedule();
        schedule.setTime(hour + ":" + minute);
        schedule.setEvent(event);
        schedule.save();

    }

    @Override
    public List<Schedule> getdata() {
        return null;
    }
}
