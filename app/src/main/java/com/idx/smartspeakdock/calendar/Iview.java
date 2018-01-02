package com.idx.smartspeakdock.calendar;

import android.app.AlertDialog;

import com.idx.smartspeakdock.calendar.bean.Schedule;

import java.util.List;

/**
 * Created by geno on 20/12/17.
 */

public interface Iview {
    void showyear(int year);
    void showmonth(int year,int month,int day);
    void showtime(String time);
}
