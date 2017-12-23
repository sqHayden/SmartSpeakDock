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
    public void selecttime() {
        AlertDialog.Builder dialog = setCustomDialog();
        iview.showdialog(dialog);
    }

    @Override
    public void getdata() {
        List<Schedule> list = model.getdata();
        iview.setadapter(list);
    }
    private AlertDialog.Builder setCustomDialog(){
        AlertDialog.Builder customdialog = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog,null);
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time);
        final EditText editText = (EditText) dialogView.findViewById(R.id.editevent);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimeListener());
        customdialog.setTitle("请添加事件");
        customdialog.setView(dialogView);
        customdialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("1218",hour +"//"+ minutes+"//" + editText.getText().toString());
                model.setdata(hour,minutes,editText.getText().toString());
            }
        });
        customdialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return customdialog;
    }
    class TimeListener implements TimePicker.OnTimeChangedListener {

        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            hour = hourOfDay;
            minutes = minute;
        }

    }
}
