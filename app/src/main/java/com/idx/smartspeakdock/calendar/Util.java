package com.idx.smartspeakdock.calendar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.idx.calendarview.CalendarView;
import com.idx.calendarview.LunarCalendar;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.bean.Schedule;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by geno on 06/01/18.
 */

public  class Util {
    static   Context context;
    static   String answer1 = "";
    static   String answer = "";
    static CalendarView mCalendarView;
    public Util(Context context,CalendarView calendarView) {
        this.context = context;
        this.mCalendarView = calendarView;
    }

    public static String getActInfo(String time, Integer year, Integer month, Integer day) {
        answer1 = "";
        answer = "";
        String date = String.valueOf(year) + String.valueOf(month);
        List<Schedule> listSchedule = DataSupport.where("date = ? and day = ?", date, day.toString()).find(Schedule.class);
        if (listSchedule.size() != 0) {
            for (int i = 0; i < listSchedule.size(); i++) {
                answer1 = listSchedule.get(i).getTime() + listSchedule.get(i).getEvent();
                answer = answer + answer1;
            }

        } else {
            answer = "没有安排事情";
        }
        return time + answer;
    }
    public static String getFestivalInfo(String time,Integer year,Integer month, Integer day) {
        answer = "";
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
    public static String getCurrentTime(){
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }
    /*
    * 获取节假日
    * */
    public String getFestivalInfo(String time){
        answer="";
        switch (time){
            case TimeData.YESTERDAY:
                answer = getFestivalInfo(time,mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                break;
            case TimeData.TODAY:
                answer =  getFestivalInfo(time,mCalendarView.getCurYear(),mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                break;
            case TimeData.TOMORROW:
                answer =  getFestivalInfo(time,mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                break;
            default:
                break;

        }
        return answer;
    }
    /*
    * 获取安排的事情
    * */
    public String getActionInfo(String time){
        answer="";
        switch (time){
            case TimeData.YESTERDAY:
                answer =  Util.getActInfo(time,mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                break;
            case TimeData.TODAY:
                answer =  Util.getActInfo(time,mCalendarView.getCurYear(),mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                break;
            case TimeData.TOMORROW:
                answer =  Util.getActInfo(time,mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                break;
            default:
                break;
        }
        return answer;
    }
    /*
   * 获取日期
   * */
    public String getDate(String time){
        answer="";
        switch (time){
            case TimeData.YESTERDAY:
                answer = time + mCalendarView.getYesData().get("month") + "月" + mCalendarView.getYesData().get("day") + "号";
                break;
            case TimeData.TODAY:
                answer = time + mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "号";
                break;
            case TimeData.TOMORROW:
                answer = time + mCalendarView.getTomoData().get("month") + "月" + mCalendarView.getTomoData().get("day") + "号";
                break;
            default:
                break;

        }
        return answer;
    }
    /*
    * 获取农历时间
    * */
    public String getLunarDateInfo(String time){
        answer="";
        switch (time){
            case TimeData.YESTERDAY:
                answer = time + "农历"+ LunarCalendar.solarToLunar(mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                break;
            case TimeData.TODAY:
                answer = time+"农历" +mCalendarView.getLunar();
                break;
            case TimeData.TOMORROW:
                answer = time + "农历"+LunarCalendar.solarToLunar(mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                break;
            default:
                break;

        }
        return answer;
    }
    /*
    * 返回英文月份
    * */
    public static String getEnglishMonth(int month){
        String englishmonth ="";
        switch (month){
            case 1:
                englishmonth = context.getResources().getString(R.string.january);
                break;
            case 2:
                englishmonth = context.getResources().getString(R.string.february);
                break;
            case 3:
                englishmonth = context.getResources().getString(R.string.march);
                break;
            case 4:
                englishmonth = context.getResources().getString(R.string.april);
                break;
            case 5:
                englishmonth = context.getResources().getString(R.string.may);
                break;
            case 6:
                englishmonth = context.getResources().getString(R.string.june);
                break;
            case 7:
                englishmonth = context.getResources().getString(R.string.july);
                break;
            case 8:
                englishmonth = context.getResources().getString(R.string.augest);
                break;
            case 9:
                englishmonth = context.getResources().getString(R.string.september);
                break;
            case 10:
                englishmonth = context.getResources().getString(R.string.october);
                break;
            case 11:
                englishmonth = context.getResources().getString(R.string.november);
                break;
            case 12:
                englishmonth = context.getResources().getString(R.string.december);
                break;
            default:
                break;

        }
        return englishmonth;
    }
}
