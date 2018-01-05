package com.idx.smartspeakdock.calendar;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.idx.calendarview.Calendar;
import com.idx.calendarview.CalendarLayout;
import com.idx.calendarview.CalendarView;
import com.idx.calendarview.LunarCalendar;
import com.idx.calendarview.MessageEvent;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.ICalenderVoiceListener;
import com.idx.smartspeakdock.calendar.adapter.MyRecyclerView;
import com.idx.smartspeakdock.calendar.bean.Schedule;
import com.idx.smartspeakdock.calendar.model.Model;
import com.idx.smartspeakdock.calendar.presenter.Presenter;
import com.idx.smartspeakdock.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by ryan on 17-12-28.
 * Email: Ryan_chan01212@yeah.net
 */

public class CalendarFragment extends BaseFragment implements
        CalendarView.OnDateSelectedListener,
        CalendarView.OnYearChangeListener,View.OnClickListener,Iview {
    private static final String TAG = CalendarFragment.class.getSimpleName();
    TextView mTextMonthDay;
    TextView mTextYear;
    TextView mCurrentTime;
    CalendarView mCalendarView;
    FrameLayout yearSelect;
    FrameLayout monthSelect;
    ImageView addButton;
    CalendarLayout mCalendarLayout;
    Context mContext;
    View mView;

    ItemRemoveRecyclerView recyclerView;
    private Presenter presenter;
    private Context context;
    private List<Schedule> list;
    private String date ="";
    private Integer day;
    public MyRecyclerView myRecyclerView;
    int hour,minutes;
    String answer;
    String lunar;
    int week;
    String answer1;
    public static CalendarFragment newInstance(){return new CalendarFragment();}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setEnable(true);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_calendar,container,false);
        initView();
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter = new Presenter(this,mContext,mCalendarView);
        initData();
        List<Schedule> listSchedule = DataSupport.where("date = ?",date).where("day = ?",day.toString()).find(Schedule.class);
        if (listSchedule.size() != 0){
            for(int i = 0;i<listSchedule.size();i++) {
                answer1 = "今天"+listSchedule.get(i).getTime() + listSchedule.get(i).getEvent();
                answer = answer + answer1;
            }

        }else {
            answer = "今天没有安排事情";
        }
        Log.v("1218","event" + answer);

        UnitManager.getInstance().setCalenderVoiceListener(new ICalenderVoiceListener() {
            @Override
            public String onWeekInfo() {
                answer="";
                answer = "今天星期"+mCalendarView.getWeek();
                return answer;
            }

            @Override
            public String onFestivalInfo() {
               answer="";
                String aa =  LunarCalendar.getSolarCalendar(mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                if (!aa.isEmpty()){
                    answer = "今天是" + aa;
                }else {
                    if (mCalendarView.getWeek() == 6||mCalendarView.getWeek() == 7){
                        answer = "今天是周末";
                    }else {
                        answer = "今天是平常日";
                    }
                }
               return answer;
            }

            @Override
            public String onActInfo() {
                answer= "";
                List<Schedule> listSchedule = DataSupport.where("date = ? and day = ?",date,day.toString()).find(Schedule.class);
                if (listSchedule.size() != 0){
                    for(int i = 0;i<listSchedule.size();i++) {
                        answer1 = "今天"+listSchedule.get(i).getTime() + listSchedule.get(i).getEvent();
                        answer = answer + answer1;
                    }

                }else {
                    answer = "今天没有安排事情";
                }
                return answer;
            }

            @Override
            public String onDateInfo() {
                answer="";
                answer = "今天" + mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "号";
                return answer;
            }

            @Override
            public String onLunarDateInfo() {
                answer="";
                answer = "今天农历" +mCalendarView.getLunar();
                return answer;
            }
        });
    }

    private void initData() {
        addButton.setOnClickListener(this);
        yearSelect.setOnClickListener(this);
        monthSelect.setOnClickListener(this);
        mCalendarView.setOnYearChangeListener(this);
        mCalendarView.setOnDateSelectedListener(this);
        presenter.getcurrenttime();
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()+ "年"));
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月");
        date = String.valueOf(mCalendarView.getCurYear()) + String.valueOf(mCalendarView.getCurMonth());
        day = mCalendarView.getCurDay();
        list = new Model().getdata();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        myRecyclerView = new MyRecyclerView(date,day,context,list);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myRecyclerView);
        recyclerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onDeleteClick(String event, String time) {
                myRecyclerView.removeItem(date,day,event,time);
                presenter.deletedate(date,day,event,time);
            }
        });
    }

    public void initView(){
        mTextMonthDay = mView.findViewById(R.id.tv_month_day);
        mTextYear = mView.findViewById(R.id.tv_year);
        mCurrentTime = mView.findViewById(R.id.currenttime);
        mCalendarView = mView.findViewById(R.id.calendarView);
        yearSelect = mView.findViewById(R.id.selectyear);
        monthSelect = mView.findViewById(R.id.selectmonth);
        addButton = mView.findViewById(R.id.event);
        mCalendarLayout = mView.findViewById(R.id.calendarLayout);
        recyclerView = mView.findViewById(R.id.recycler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.event:
                if (date.isEmpty() || day.equals(0)){
                    Toast.makeText(mContext,"请选择日期",Toast.LENGTH_SHORT).show();
                }else {
                    setCustomDialog();
                }
                break;
            case R.id.selectyear:
                presenter.selectyear();
                break;
            case R.id.selectmonth:
                presenter.selectmonth();
                break;
            default:
                break;
        }
    }

    @Override
    public void showyear(int year) {
        mCalendarView.showSelectLayout(year);
        mTextMonthDay.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        mTextYear.setText(String.valueOf(year+ "年"));
    }

    @Override
    public void showmonth(int year, int month, int day) {
        mCalendarView.selectCurrentMonth();
        mCalendarView.scrollToCalendar(year,month,day);
    }
/*
* 显示时间
 */
    @Override
    public void showtime(String time) {
        mCurrentTime.setText(time);
    }

    @Override
    public void onDateSelected(Calendar calendar) {
        Logger.info(TAG,"dataselected");
        mTextYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setVisibility(View.VISIBLE);
        mTextMonthDay.setText(calendar.getMonth() + "月");
        mTextYear.setText(String.valueOf(calendar.getYear()+ "年"));
    }

    @Override
    public void onYearChange(int year) {mTextYear.setText(String.valueOf(year+ "年"));}

    @Subscribe
    public void onEvent(MessageEvent messageEvent){
        if (recyclerView.getVisibility() == View.GONE){
            recyclerView.setVisibility(View.VISIBLE);
        }
        date = messageEvent.getMessage();
        day = messageEvent.getday();
        myRecyclerView.notifyDataSetChanged();
    }

    private void setCustomDialog(){
        final AlertDialog customdialog = new AlertDialog.Builder(mContext).create();
        final View dialogView = LayoutInflater.from(mContext).inflate(R.layout.custom_dialog,null);
        final  TextView titleview = (TextView) dialogView.findViewById(R.id.title);
        final TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time);
        final EditText editText = (EditText) dialogView.findViewById(R.id.editevent);
        final Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        final Button yes = (Button) dialogView.findViewById(R.id.yes);
        customdialog.setView(dialogView);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new CalendarFragment.TimeListener());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customdialog.dismiss();
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().isEmpty()){
                    Toast.makeText(mContext,"请添加事件",Toast.LENGTH_SHORT).show();
                }else {
                    presenter.setdate(date,day,hour,minutes,editText.getText().toString());
                    Schedule schedule = new Schedule();
                    schedule.setDate(date);
                    schedule.setDay(day);
                    schedule.setTime(hour + ":" + minutes);
                    schedule.setEvent(editText.getText().toString());
                    list.add(schedule);
                    myRecyclerView.notifyItemInserted(list.size() - 1);
                    customdialog.dismiss();
                }
            }
        });
        customdialog.show();
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