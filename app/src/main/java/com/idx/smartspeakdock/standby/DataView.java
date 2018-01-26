package com.idx.smartspeakdock.standby;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextClock;

/**
 * Created by Franck on 12/20/17.
 */

public class DataView extends TextClock {
    public DataView(Context context){
        super(context);
    }

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFormat12Hour("EEEE  M/dd");
        setTypeface(FontCustom.setHeiTi(context));
    }

    public DataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
    }
}
