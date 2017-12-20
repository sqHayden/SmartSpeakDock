package com.idx.smartspeakdock.standby;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by peter on 12/15/17.
 */

public class FontCustom {
    private static String Avenir = "Avenir.ttf";
    private static String HiTi = "微软正黑体.ttf";

    //Typeface是字体，这里我们创建一个对象
    private static Typeface tf;

    /**
     * 设置字体
     */
    public static Typeface setAvenir(Context context)
    {
        if (tf == null)
        {
            //给它设置你传入的自定义字体文件，再返回回来
            tf = Typeface.createFromAsset(context.getAssets(),Avenir);
        }
        return tf;
    }

    public static Typeface setHeiTi(Context context)
    {
        if (tf == null)
        {
            //给它设置你传入的自定义字体文件，再返回回来
            tf = Typeface.createFromAsset(context.getAssets(),HiTi);
        }
        return tf;
    }
}


