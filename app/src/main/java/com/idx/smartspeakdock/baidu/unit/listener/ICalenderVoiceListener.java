package com.idx.smartspeakdock.baidu.unit.listener;

/**
 * Created by derik on 17-12-25.
 */

public interface ICalenderVoiceListener {
    String onWeekInfo(String time);

    String onTimeInfo();

    String onFestivalInfo(String time);

    String onFestivalDate(String name);

    String onActInfo(String time);

    String onDateInfo(String time);

    String onLunarDateInfo(String time);

}
