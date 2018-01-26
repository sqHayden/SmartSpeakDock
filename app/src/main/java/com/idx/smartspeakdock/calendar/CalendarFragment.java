package com.idx.smartspeakdock.calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.idx.calendarview.MessageEvent;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.swipe.SwipeActivity;
import com.idx.smartspeakdock.calendar.adapter.MyRecyclerView;
import com.idx.smartspeakdock.calendar.bean.Schedule;
import com.idx.smartspeakdock.calendar.model.Model;
import com.idx.smartspeakdock.calendar.presenter.Presenter;
import com.idx.smartspeakdock.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * Created by ryan on 17-12-28.
 * Email: Ryan_chan01212@yeah.net
 */

public class CalendarFragment extends BaseFragment implements
        CalendarView.OnDateSelectedListener,
        CalendarView.OnYearChangeListener,View.OnClickListener,Iview {
    private static final String TAG = CalendarFragment.class.getSimpleName();
    private Presenter presenter;
    private List<Schedule> list;
    private String date ="";
    private Integer day;
    public MyRecyclerView myRecyclerView;
    private SwipeActivity.MyOnTouchListener onTouchListener;
    private TextView mYear;
    private TextView mMonth;
    int year;
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
    String hour,minutes;
    String time;
    Boolean yearopen = false;
    String  yearNumber;
    private String monthnumber;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setEnable(true);
        Log.d(TAG, "onCreate: ");
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_calendar,container,false);
        Log.d(TAG, "onCreateView: ");
        initView();
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreatedaa: " + savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("year")){
            Log.d(TAG, "onActivityCreatedbb: ");
            yearNumber = savedInstanceState.getString("yearnumber");
            mCalendarView.showSelectLayout( Integer.valueOf(yearNumber).intValue());
            mTextMonthDay.setVisibility(View.GONE);
            mMonth.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            addButton.setVisibility(View.INVISIBLE);
            mTextYear.setText(yearNumber);
            yearopen = true;
        }else {
            Log.d(TAG, "onActivityCreatedcc: ");
            initData();
        }
        presenter = new Presenter(this,mContext,mCalendarView);
        initListener();
    }
    private void initListener(){
        Log.d(TAG, "initListener: ");
        addButton.setOnClickListener(this);
        yearSelect.setOnClickListener(this);
        monthSelect.setOnClickListener(this);
        mCalendarView.setOnYearChangeListener(this);
        if (mTextMonthDay.getVisibility() != View.GONE){
            mCalendarView.setOnDateSelectedListener(this);
        }
        presenter.getcurrenttime();
        date = String.valueOf(mCalendarView.getCurYear()) + String.valueOf(mCalendarView.getCurMonth());
        day = mCalendarView.getCurDay();
        list = new Model().getdata();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        myRecyclerView = new MyRecyclerView(date,day,mContext,list);
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
    private void initData() {
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        if (getResources().getConfiguration().locale.getCountry().equals("UK") ||getResources().getConfiguration().locale.getCountry().equals("US")){
            mTextMonthDay.setText(Util.getEnglishMonth(mCalendarView.getCurMonth()));
            mMonth.setVisibility(View.GONE);
        }else {
            mTextMonthDay.setText(String.valueOf(mCalendarView.getCurMonth()));
        }

    }

    public void initView(){
        Log.d(TAG, "initView: ");
        mYear = mView.findViewById(R.id.year);
        mMonth = mView.findViewById(R.id.month);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: " + yearopen);
        yearNumber = mTextYear.getText().toString();
        monthnumber = mTextMonthDay.getText().toString();
        outState.putBoolean("year",yearopen);
        outState.putString("yearnumber",yearNumber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        EventBus.getDefault().unregister(this);
        if (presenter != null){
            presenter=null;
        }
        if (list != null){
            list = null;
        }
        if (myRecyclerView != null){
            myRecyclerView = null;
        }
        if (onTouchListener != null){
            onTouchListener = null;
        }
        if (mCalendarView != null){
            mCalendarView = null;
        }
       if (yearSelect != null){
           yearSelect = null;
       }
        if (monthSelect != null){
            monthSelect = null;
        }
        if (mCalendarLayout != null){
            mCalendarLayout = null;
        }
        if (mContext != null){
            mContext = null;
        }
        if (mView != null){
            mView = null;
        }
        if (recyclerView != null){
            recyclerView = null;
        }

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

   /*
   * 点击年按钮
   * */
    @Override
    public void showyear(int year) {
        Log.d(TAG, "showyear: " + yearopen);
        this.year = year;
        mCalendarView.showSelectLayout(year);
        mTextMonthDay.setVisibility(View.GONE);
        mMonth.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        addButton.setVisibility(View.INVISIBLE);
        mTextYear.setText(String.valueOf(year));
        yearopen = true;
    }
    /*
    * 点击月按钮
    * */
    @Override
    public void showmonth(int year, int month, int day) {
        Log.d(TAG, "showmonth: "+ yearopen);
        mCalendarView.selectCurrentMonth();
        mCalendarView.scrollToCalendar(year,month,day);
        mTextMonthDay.setVisibility(View.VISIBLE);
        if (getResources().getConfiguration().locale.getCountry().equals("UK") ||getResources().getConfiguration().locale.getCountry().equals("US")){
            mTextMonthDay.setText(Util.getEnglishMonth(mCalendarView.getCurMonth()));
            mMonth.setVisibility(View.GONE);
        }else {
            mMonth.setVisibility(View.VISIBLE);
            mTextMonthDay.setText(String.valueOf(mCalendarView.getCurMonth()));
        }
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        addButton.setVisibility(View.VISIBLE);
        yearopen = false;
    }
    /*
    * 显示时间
    * */
    @Override
    public void showtime(String time) {
        this.time = time;
        mCurrentTime.setText(time);
    }

    @Override
    public void onDateSelected(Calendar calendar) {
        Log.d(TAG, "onDateSelected: ");
        mTextYear.setVisibility(View.VISIBLE);
        mYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setVisibility(View.VISIBLE);
        mMonth.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
        if (getResources().getConfiguration().locale.getCountry().equals("UK") ||getResources().getConfiguration().locale.getCountry().equals("US")){
            mTextMonthDay.setText(Util.getEnglishMonth(calendar.getMonth()));
            mMonth.setVisibility(View.GONE);
        }else {
            mTextMonthDay.setText(String.valueOf(calendar.getMonth()));
        }
        mTextYear.setText(String.valueOf(calendar.getYear()));
    }

    @Override
    public void onYearChange(int year) {
        if (isAdded()){
            mTextYear.setText(String.valueOf(year));
        }
       }

    @Subscribe
    public void onEvent(MessageEvent messageEvent){
        if (recyclerView.getVisibility() == View.GONE){
            recyclerView.setVisibility(View.VISIBLE);
        }
        date = messageEvent.getMessage();
        day = messageEvent.getday();
        myRecyclerView.notifyDataSetChanged();
    }
    /*
    * 添加事件对话框
    * */
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
                    if (hour != null||minutes != null){
                       time = hour + ":" + minutes;
                    }
                    presenter.setdate(date,day,time,editText.getText().toString());
                    Schedule schedule = new Schedule();
                    schedule.setDate(date);
                    schedule.setDay(day);
                    schedule.setTime(time);
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
