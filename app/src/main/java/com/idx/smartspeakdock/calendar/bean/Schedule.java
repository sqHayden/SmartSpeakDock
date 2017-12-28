package com.idx.smartspeakdock.calendar.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by geno on 22/12/17.
 */

public class Schedule extends DataSupport{
    private String event;
    private String time;
    private String date;
    private Integer day;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }


}
