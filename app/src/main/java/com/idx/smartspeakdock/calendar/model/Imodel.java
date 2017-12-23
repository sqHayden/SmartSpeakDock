package com.idx.smartspeakdock.calendar.model;

import com.idx.smartspeakdock.calendar.bean.Schedule;

import java.util.List;

/**
 * Created by geno on 22/12/17.
 */

public interface Imodel {
    void setdata(int hour,int minute,String event);
    List<Schedule> getdata();
}
