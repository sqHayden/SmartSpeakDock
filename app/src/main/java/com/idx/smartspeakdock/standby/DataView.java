package com.idx.smartspeakdock.standby;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextClock;

/**
 * Created by peter on 12/20/17.
 */

public class DataView extends TextClock {
    public DataView(Context context){
        super(context);
    }

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFormat12Hour("EEEE, MM月dd日");
        setFormat24Hour("EEEE, MM月dd日");
        setTypeface(FontCustom.setHeiTi(context));
    }

    public DataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
    }
}
