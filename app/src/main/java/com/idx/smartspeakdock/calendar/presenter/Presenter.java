package com.idx.smartspeakdock.calendar.presenter;

import com.idx.calendarview.CalendarView;
import com.idx.smartspeakdock.calendar.Iview;

/**
 * Created by geno on 20/12/17.
 */

public class Presenter implements Ipresenter{
    Iview iview;
    CalendarView calendarView;
    int mYear,mMonth,mDay;
    public Presenter(Iview iview, CalendarView calendarView) {
        super();
        this.iview = iview;
        this.calendarView = calendarView;
        init();
    }
    private void init(){
        mYear = calendarView.getCurYear();
        mMonth = calendarView.getCurMonth();
        mDay = calendarView.getCurDay();
    }
    @Override
    public void selectyear() {
      iview.showyear(mYear);
    }

    @Override
    public void selectmonth() {
      iview.showmonth(mYear,mMonth,mDay);
    }
}
