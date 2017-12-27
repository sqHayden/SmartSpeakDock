package com.idx.smartspeakdock.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.adapter.MyRecyclerView;
import com.idx.smartspeakdock.calendar.bean.Schedule;
import com.idx.smartspeakdock.calendar.model.Model;
import com.idx.smartspeakdock.calendar.presenter.Presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarActivity extends Activity implements
        CalendarView.OnDateSelectedListener,
        CalendarView.OnYearChangeListener,View.OnClickListener,Iview{
    @BindView(R.id.tv_month_day)
    TextView mTextMonthDay;
    @BindView(R.id.tv_year)
    TextView mTextYear;
    @BindView(R.id.calendarView)
    CalendarView mCalendarView;
    @BindView(R.id.selectyear)
    FrameLayout yearSelect;
    @BindView(R.id.selectmonth)
    FrameLayout monthSelect;
    @BindView(R.id.event)
    ImageView addButton;
    @BindView(R.id.calendarLayout)
    CalendarLayout mCalendarLayout;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    private Presenter presenter;
    private Context context;
    private List<Schedule> list;
    private String date ="";
    private Integer day;
    public MyRecyclerView myRecyclerView;
    int hour,minutes;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        context = (Context) CalendarActivity.this;
        presenter = new Presenter(this,context,mCalendarView);
        init();

    }
    private void init(){
        addButton.setOnClickListener(this);
        yearSelect.setOnClickListener(this);
        monthSelect.setOnClickListener(this);
        mCalendarView.setOnYearChangeListener(this);
        mCalendarView.setOnDateSelectedListener(this);
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()+ "年"));
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月");
        date = String.valueOf(mCalendarView.getCurYear()) + String.valueOf(mCalendarView.getCurMonth());
        day = mCalendarView.getCurDay();
        list = new Model().getdata();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        myRecyclerView = new MyRecyclerView(date,day,context,list);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myRecyclerView);
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.event:
                if (date.isEmpty() || day.equals(0)){
                    Toast.makeText(context,"请选择日期",Toast.LENGTH_SHORT).show();
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
        mTextYear.setText(String.valueOf(year+ "年"));
    }

    @Override
    public void showmonth(int year, int month, int day) {
        mCalendarView.selectCurrentMonth();
        mCalendarView.scrollToCalendar(year,month,day);
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSelected(Calendar calendar) {
        mTextYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setVisibility(View.VISIBLE);
        mTextMonthDay.setText(calendar.getMonth() + "月");
        mTextYear.setText(String.valueOf(calendar.getYear()+ "年"));
    }

    @Override
    public void onYearChange(int year) {
        mTextYear.setText(String.valueOf(year+ "年"));
    }
    @Subscribe
    public void onEvent(MessageEvent messageEvent){
        date = messageEvent.getMessage();
        day = messageEvent.getday();
        myRecyclerView.notifyDataSetChanged();
    }
    private void setCustomDialog(){
        AlertDialog.Builder customdialog = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog,null);
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time);
        final EditText editText = (EditText) dialogView.findViewById(R.id.editevent);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimeListener());
        customdialog.setTitle("请添加事件");
        customdialog.setView(dialogView);
        customdialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (editText.getText().toString().isEmpty()){
                    Toast.makeText(CalendarActivity.this,"请添加事件",Toast.LENGTH_SHORT).show();
                }else {
                    presenter.setdate(hour,minutes,editText.getText().toString());
                    Schedule schedule = new Schedule();
                    schedule.setDate(date);
                    schedule.setDay(day);
                    schedule.setTime(hour + ":" + minutes);
                    schedule.setEvent(editText.getText().toString());
                    list.add(schedule);
                    myRecyclerView.notifyItemInserted(list.size() - 1);
                }

            }
        });
        customdialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
