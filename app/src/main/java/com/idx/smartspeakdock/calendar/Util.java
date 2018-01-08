package com.idx.smartspeakdock.calendar;

import com.idx.calendarview.LunarCalendar;
import com.idx.smartspeakdock.calendar.bean.Schedule;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by geno on 06/01/18.
 */

public  class Util {
  static   String answer1 = "";
  static   String answer = "";
    public static String getActInfo(String time, Integer year, Integer month, Integer day) {
        String date = String.valueOf(year) + String.valueOf(month);
        List<Schedule> listSchedule = DataSupport.where("date = ? and day = ?", date, day.toString()).find(Schedule.class);
        if (listSchedule.size() != 0) {
            for (int i = 0; i < listSchedule.size(); i++) {
                answer1 = listSchedule.get(i).getTime() + listSchedule.get(i).getEvent();
                answer = answer + answer1;
            }

        } else {
            answer = time + "没有安排事情";
        }
        return answer;
    }
    public static String getFestivalInfogetActInfo(String time,Integer year,Integer month, Integer day) {
        String aa =  LunarCalendar.getSolarCalendar(month,day);
        if (!aa.isEmpty()){
            answer = time+"是" + aa;
        }else {
            if (getWeek(year,month,day) == 6||getWeek(year,month,day) == 7){
                answer = time+"是周末";
            }else {
                answer = time+"是平常日";
            }
        }

    return answer;
    }
    public static int getWeek(Integer year,Integer month, Integer day) {
        String pTime = String.valueOf(year) +"-" + String.valueOf(month)+"-"+String.valueOf(day);
        int Week = 0;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {

            c.setTime(format.parse(pTime));

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            Week += 7;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            Week += 1;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            Week += 2;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            Week += 3;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            Week += 4;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            Week += 5;
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            Week += 6;
        }
        return Week;
    }
}
