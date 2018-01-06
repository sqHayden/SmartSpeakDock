package com.idx.smartspeakdock.standby;


import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextClock;

/**
 * Created by peter on 12/15/17.
 */

public class TimeView extends TextClock{
    public TimeView(Context context){
        super(context);
    }

    public TimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        setFormat12Hour("HH:mm");
        setFormat24Hour("HH:mm");
        setTypeface(FontCustom.setAvenir(context));
        setGravity(Gravity.CENTER_VERTICAL);
    }

    public TimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
    }

}