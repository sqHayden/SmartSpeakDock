package com.idx.smartspeakdock.calendar.model;

import com.idx.smartspeakdock.calendar.bean.Schedule;

import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public interface Imodel {
    void setdata(String date,Integer day,String time,String event);
    List<Schedule> getdata();
    void deletedate(String date,Integer day,String event,String time);
}
