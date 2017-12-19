package com.idx.smartspeakdock.standby;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by peter on 12/15/17.
 */

public class DataString {
    private static String mMonth;
    private static String mDay;
    public static String mWay;

    public static String StringData(){
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mMonth = String.valueOf(calendar.get(Calendar.MONTH)+1);
        mDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        mWay = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
        if("1".equals(mWay)){
            mWay ="星期天";
        }else if("2".equals(mWay)){
            mWay ="星期一";
        }else if("3".equals(mWay)){
            mWay ="星期二";
        }else if("4".equals(mWay)){
            mWay ="星期三";
        }else if("5".equals(mWay)){
            mWay ="星期四";
        }else if("6".equals(mWay)){
            mWay ="星期五";
        }else if("7".equals(mWay)) {
            mWay = "星期六";
        }
        return mWay+","+mMonth + "月" +mDay + "日";
    }
}

