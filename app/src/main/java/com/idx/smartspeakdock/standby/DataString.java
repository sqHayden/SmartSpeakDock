package com.idx.smartspeakdock.standby;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by peter on 12/15/17.
 */

public class DataString {
    private static String mMonth;
    private static String mDay;

    public static String StringData(){
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mMonth = String.valueOf(calendar.get(Calendar.MONTH)+1);
        mDay = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));

        if("1".equals(mMonth)){
            mMonth ="Jan";
        }else if("2".equals(mMonth)){
            mMonth ="Feb";
        }else if("3".equals(mMonth)){
            mMonth ="Mar";
        }else if("4".equals(mMonth)){
            mMonth ="Apr";
        }else if("5".equals(mMonth)){
            mMonth ="May";
        }else if("6".equals(mMonth)){
            mMonth ="June";
        }else if("7".equals(mMonth)){
            mMonth ="July";
        }else if("8".equals(mMonth)){
            mMonth ="Aug";
        }else if("9".equals(mMonth)){
            mMonth ="Sept";
        }else if("10".equals(mMonth)){
            mMonth ="Oct";
        }else if("11".equals(mMonth)){
            mMonth ="Nov";
        }else if("12".equals(mMonth)){
            mMonth ="Dec";
        }

        return "SUNDAY,"+mMonth +","+mDay;
    }
}
