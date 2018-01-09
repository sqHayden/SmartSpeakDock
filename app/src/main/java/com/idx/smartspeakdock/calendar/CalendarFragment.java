package com.idx.smartspeakdock.calendar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.ICalenderVoiceListener;
import com.idx.smartspeakdock.calendar.adapter.MyRecyclerView;
import com.idx.smartspeakdock.calendar.bean.Schedule;
import com.idx.smartspeakdock.calendar.model.Model;
import com.idx.smartspeakdock.calendar.presenter.Presenter;
import com.idx.smartspeakdock.service.SplachService;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.Util;

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
    private com.idx.smartspeakdock.calendar.Util util;
    private Context context;
    private List<Schedule> list;
    private String date ="";
    private Integer day;
    public MyRecyclerView myRecyclerView;
    String hour,minutes;
    String answer;
    public static CalendarFragment newInstance(){return new CalendarFragment();}
    private SwipeActivity.MyOnTouchListener onTouchListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v("1218","onattach");
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setEnable(true);
        Log.v("1218","oncreate");
        EventBus.getDefault().register(this);
        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_calendar,container,false);
        initView();
        Log.v("1218","oncreateview");
        onTouchListener = new SwipeActivity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        Log.d(TAG, "onTouch: down");
                        mContext.stopService(new Intent(mContext.getApplicationContext(), SplachService.class));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: move");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: up");
                        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
                        break;
                }
                return false;
            }
        };
        ((SwipeActivity) getActivity()).registerMyOnTouchListener(onTouchListener);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("1218","onactivitycrated");
        presenter = new Presenter(this,mContext,mCalendarView);
        util = new com.idx.smartspeakdock.calendar.Util(mContext);
        initData();
        UnitManager.getInstance().setCalenderVoiceListener(new ICalenderVoiceListener() {
            @Override
            public String onWeekInfo(String time) {
                answer="";
                answer = time+getString(R.string.week)+ mCalendarView.getWeek(time);
                return answer;
            }

            @Override
            public String onTimeInfo() {
                answer = "";
                answer = getString(R.string.now)+ mCurrentTime.getText().toString();
                return answer;
            }

            @Override
            public String onFestivalInfo(String time) {
                switch (time){
                    case TimeData.YESTERDAY:
                        answer = util.getFestivalInfogetActInfo(time,mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                        break;
                    case TimeData.TODAY:
                        answer =  util.getFestivalInfogetActInfo(time,mCalendarView.getCurYear(),mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                        break;
                    case TimeData.TOMORROW:
                        answer =  util.getFestivalInfogetActInfo(time,mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                        break;
                    default:
                        break;

                }
                return answer;
            }

            @Override
            public String onActInfo(String time) {
                Log.v("1218","answer11"+time);
                switch (time){
                    case TimeData.YESTERDAY:
                       answer =  util.getActInfo(time,mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                        break;
                    case TimeData.TODAY:
                        answer =  util.getActInfo(time,mCalendarView.getCurYear(),mCalendarView.getCurMonth(),mCalendarView.getCurDay());
                        Log.v("1218","answer"+answer);
                        break;
                    case TimeData.TOMORROW:
                        answer =  util.getActInfo(time,mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                        break;
                    default:
                        break;

                }
                return answer;
            }

            @Override
            public String onDateInfo(String time) {
                answer="";
                switch (time){
                    case TimeData.YESTERDAY:
                        answer = time + mCalendarView.getYesData().get("month") + getString(R.string.month) + mCalendarView.getYesData().get("day") + getString(R.string.day);
                        break;
                    case TimeData.TODAY:
                        answer = time + mCalendarView.getCurMonth() + getString(R.string.month) + mCalendarView.getCurDay() + getString(R.string.day);
                        break;
                    case TimeData.TOMORROW:
                        answer = time + mCalendarView.getTomoData().get("month") + getString(R.string.month) + mCalendarView.getTomoData().get("day") + getString(R.string.day);
                        break;
                    default:
                        break;

                }
                return answer;
            }

            @Override
            public String onLunarDateInfo(String time) {
                switch (time){
                    case TimeData.YESTERDAY:
                        answer = time + getString(R.string.lunar_calendar)+LunarCalendar.solarToLunar(mCalendarView.getYesData().get("year"),mCalendarView.getYesData().get("month"),mCalendarView.getYesData().get("day"));
                        break;
                    case TimeData.TODAY:
                        answer = time+getString(R.string.lunar_calendar) +mCalendarView.getLunar();
                        break;
                    case TimeData.TOMORROW:
                        answer = time + getString(R.string.lunar_calendar)+LunarCalendar.solarToLunar(mCalendarView.getTomoData().get("year"),mCalendarView.getTomoData().get("month"),mCalendarView.getTomoData().get("day"));
                        break;
                    default:
                        break;

                }
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
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()+ getString(R.string.year)));
        mTextMonthDay.setText(mCalendarView.getCurMonth() +getString(R.string.month));
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
    public void onResume() {
        super.onResume();
        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
        Log.d(TAG, "onResume: ");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mContext.stopService(new Intent(mContext.getApplicationContext(), SplachService.class));
        ((SwipeActivity) getActivity()).unregisterMyOnTouchListener(onTouchListener);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.event:
                if (date.isEmpty() || day.equals(0)){
                    Toast.makeText(mContext,getString(R.string.please_select_date),Toast.LENGTH_SHORT).show();
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
        mTextYear.setText(String.valueOf(year+ getString(R.string.year)));
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
        mTextYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setVisibility(View.VISIBLE);
        mTextMonthDay.setText(calendar.getMonth() + getString(R.string.month));
        mTextYear.setText(String.valueOf(calendar.getYear()+ getString(R.string.year)));
    }

    @Override
    public void onYearChange(int year) {mTextYear.setText(String.valueOf(year+ getString(R.string.year)));}

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
                    Toast.makeText(mContext,getString(R.string.please_add_event),Toast.LENGTH_SHORT).show();
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
            hour = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
            minutes = minute < 10 ? "0" + minute : ""+minute;
        }
    }
}
