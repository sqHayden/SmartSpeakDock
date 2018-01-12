package com.idx.smartspeakdock.calendar.presenter;

/**
 * Created by geno on 20/12/17.
 */

public interface Ipresenter {
    void selectyear();
    void selectmonth();
    void getdata();
    void setdate(String date,Integer day,String time,String event);
    void deletedate(String date,Integer day,String event,String time);
    void getcurrenttime();
}
