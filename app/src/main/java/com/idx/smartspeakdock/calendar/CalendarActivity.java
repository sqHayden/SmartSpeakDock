package com.idx.smartspeakdock.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.idx.calendarview.Calendar;
import com.idx.calendarview.CalendarLayout;
import com.idx.calendarview.CalendarView;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.presenter.Presenter;

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
    @BindView(R.id.ib_calendar)
    ImageView addButton;
    @BindView(R.id.calendarLayout)
    CalendarLayout mCalendarLayout;
    private Presenter presenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        presenter = new Presenter(this,mCalendarView);
        addButton.setOnClickListener(this);
        yearSelect.setOnClickListener(this);
        monthSelect.setOnClickListener(this);
        mCalendarView.setOnYearChangeListener(this);
        mCalendarView.setOnDateSelectedListener(this);
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()+ "年"));
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月");
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ib_calendar:
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
        Log.v("1218","dataselected");
        mTextYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setVisibility(View.VISIBLE);
        mTextMonthDay.setText(calendar.getMonth() + "月");
        mTextYear.setText(String.valueOf(calendar.getYear()+ "年"));
    }

    @Override
    public void onYearChange(int year) {
        Log.v("1218","yearselected");
        mTextYear.setText(String.valueOf(year+ "年"));
    }

}
