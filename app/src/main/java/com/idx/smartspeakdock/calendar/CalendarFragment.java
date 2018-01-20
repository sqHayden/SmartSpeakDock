package com.idx.smartspeakdock.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
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
    private List<Schedule> list;
    private String date ="";
    private Integer day;
    public MyRecyclerView myRecyclerView;
    private Context context;
    String hour,minutes;
    String answer;
    String time;
    Boolean yearopen = false;
    int year;
    private TextView selectyeartext;
    private SwipeActivity.MyOnTouchListener onTouchListener;

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
        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_calendar,container,false);
        initView();
        onTouchListener = new SwipeActivity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        mContext.stopService(new Intent(mContext.getApplicationContext(), SplachService.class));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
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
        if (savedInstanceState != null && savedInstanceState.getBoolean("year")){
            mCalendarView.showSelectLayout(year);
            mTextMonthDay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            addButton.setVisibility(View.INVISIBLE);
            mTextYear.setText(String.valueOf(year+ getString(R.string.year)));
            yearopen = true;
        }
        presenter = new Presenter(this,mContext,mCalendarView);
        initData();
    }

    private void initData() {
        addButton.setOnClickListener(this);
        yearSelect.setOnClickListener(this);
        monthSelect.setOnClickListener(this);
        mCalendarView.setOnYearChangeListener(this);
        if (mTextMonthDay.getVisibility() != View.GONE) {
            mCalendarView.setOnDateSelectedListener(this);
        }
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
        if ( getResources().getConfiguration().locale.getCountry().equals("UK") || getResources().getConfiguration().locale.getCountry().equals("US")){
            selectyeartext = mView.findViewById(R.id.selectyeartext);
            TextView selectmonthtext = mView.findViewById(R.id.selectmonthtext);
            selectyeartext.setTextSize(getResources().getDimension(R.dimen.calendar_textsize_select));
            selectmonthtext.setTextSize(getResources().getDimension(R.dimen.calendar_textsize_select));
        }
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("year",yearopen);
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

   /*
   * 点击年按钮
   * */
    @Override
    public void showyear(int year) {
        this.year = year;
        mCalendarView.showSelectLayout(year);
        mTextMonthDay.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        addButton.setVisibility(View.INVISIBLE);
        mTextYear.setText(String.valueOf(year+ getString(R.string.year)));
        yearopen = true;
    }
    /*
    * 点击月按钮
    * */
    @Override
    public void showmonth(int year, int month, int day) {
        mCalendarView.selectCurrentMonth();
        mCalendarView.scrollToCalendar(year,month,day);
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
        mTextYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setVisibility(View.VISIBLE);
        mTextMonthDay.setText(calendar.getMonth() + getString(R.string.month));
        mTextYear.setText(String.valueOf(calendar.getYear()+ getString(R.string.year)));
    }

    @Override
    public void onYearChange(int year) {
        mTextYear.setText(String.valueOf(year+ getString(R.string.year)));}

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
