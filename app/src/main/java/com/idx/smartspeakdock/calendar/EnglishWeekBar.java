package com.idx.smartspeakdock.calendar;

import android.content.Context;
import android.view.LayoutInflater;

import com.idx.calendarview.WeekBar;
import com.idx.smartspeakdock.R;

/**
 * Created by geno on 18/12/17.
 */

public class EnglishWeekBar extends WeekBar {
    public EnglishWeekBar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.week_bar, this, true);
    }
}
