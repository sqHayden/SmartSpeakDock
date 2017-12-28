package com.idx.smartspeakdock.calendar.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.idx.calendarview.CalendarView;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.Iview;
import com.idx.smartspeakdock.calendar.bean.Schedule;
import com.idx.smartspeakdock.calendar.model.Model;

import java.util.List;

/**
 * Created by geno on 20/12/17.
 */

public class Presenter implements Ipresenter{
    Iview iview;
    CalendarView calendarView;
    Model model = new Model();
    int mYear,mMonth,mDay;
    int hour,minutes;
    Context context;
    public Presenter(Iview iview,Context context, CalendarView calendarView) {
        super();
        this.iview = iview;
        this.context = context;
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
    @Override
    public void setdate(int hours, int minutes, String event) {
        model.setdata(hours,minutes,event);
    }

    @Override
    public void deletedate(String date, Integer day, String event, String time) {
        Log.v("1218","删除"+ date + day + event + time);
     model.deletedate(date,day,event,time);
    }

    @Override
    public void getdata() {
        List<Schedule> list = model.getdata();
    }
}
