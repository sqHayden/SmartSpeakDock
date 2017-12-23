package com.idx.smartspeakdock.calendar.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by geno on 22/12/17.
 */

public class Schedule extends DataSupport{
    private String event;
    private String time;
    private Integer year;
    private Integer month;
    private Integer day;

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }


}
