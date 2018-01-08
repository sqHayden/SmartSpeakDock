/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.idx.calendarview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日历布局
 * 各个类使用包权限，避免不必要的public
 */
@SuppressWarnings("deprecation,unused")
public class CalendarView extends FrameLayout {

    /**
     * 使用google官方推荐的方式抽取自定义属性
     */
    private CustomCalendarViewDelegate mDelegate;

    /**
     * 自定义自适应高度的ViewPager
     */
    private MonthViewPager mMonthPager;

    /**
     * 日历周视图
     */
    private WeekViewPager mWeekPager;

    /**
     * 月份快速选取
     */
    private MonthSelectLayout mSelectLayout;

    /**
     * 星期栏
     */
    private WeekBar mWeekBar;

    /**
     * 日历外部收缩布局
     */
    CalendarLayout mParentLayout;
    private View view;


    public CalendarView(@NonNull Context context) {
        this(context, null);
    }

    public CalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDelegate = new CustomCalendarViewDelegate(context, attrs);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context context
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cv_layout_calendar_view, this, true);
        FrameLayout frameContent = (FrameLayout) findViewById(R.id.frameContent);
        this.mWeekPager = (WeekViewPager) findViewById(R.id.vp_week);
        this.mWeekPager.setup(mDelegate);

        if (TextUtils.isEmpty(mDelegate.getWeekBarClass())) {
            this.mWeekBar = new WeekBar(getContext());
        } else {
            try {
                Class cls = Class.forName(mDelegate.getWeekBarClass());
                @SuppressWarnings("unchecked")
                Constructor constructor = cls.getConstructor(Context.class);
                mWeekBar = (WeekBar) constructor.newInstance(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        frameContent.addView(mWeekBar, 2);
        mSelectLayout = (MonthSelectLayout) findViewById(R.id.selectLayout);
        view = (View) findViewById(R.id.line);
        this.mMonthPager = (MonthViewPager) findViewById(R.id.vp_calendar);
        this.mMonthPager.mWeekPager = mWeekPager;

        mSelectLayout.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mWeekPager.getVisibility() == VISIBLE) {
                    return;
                }
                if (mDelegate.mDateChangeListener != null) {
                    mDelegate.mDateChangeListener.onYearChange(position + mDelegate.getMinYear());
                }
                if (mDelegate.mYearChangeListener != null) {
                    mDelegate.mYearChangeListener.onYearChange(position + mDelegate.getMinYear());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDelegate.mInnerListener = new OnInnerDateSelectedListener() {
            @Override
            public void onDateSelected(Calendar calendar) {
                if (calendar.getYear() == mDelegate.getCurrentDay().getYear() &&
                        calendar.getMonth() == mDelegate.getCurrentDay().getMonth()
                        && mMonthPager.getCurrentItem() != mDelegate.mCurrentMonthViewItem) {
                    return;
                }
                String aa = String.valueOf(calendar.getYear()) + String.valueOf(calendar.getMonth());
                EventBus.getDefault().post(new MessageEvent(aa,calendar.getDay()));
                mDelegate.mSelectedCalendar = calendar;
                mWeekPager.updateSelected(mDelegate.mSelectedCalendar);
                mMonthPager.updateSelected();
            }

            @Override
            public void onWeekSelected(Calendar calendar) {
                mDelegate.mSelectedCalendar = calendar;
                int y = calendar.getYear() - mDelegate.getMinYear();
                int position = 12 * y + mDelegate.mSelectedCalendar.getMonth() - mDelegate.getMinYearMonth();
                mMonthPager.setCurrentItem(position);
                mMonthPager.updateSelected();
            }
        };

        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate();

        int mCurYear = mDelegate.mSelectedCalendar.getYear();
//        if (mDelegate.getMinYear() >= mCurYear) mDelegate.setMinYear(mCurYear);
//        if (mDelegate.getMaxYear() <= mCurYear) mDelegate.setMaxYear(mCurYear + 2);
        mSelectLayout.setup(mDelegate);
        int y = mDelegate.mSelectedCalendar.getYear() - mDelegate.getMinYear();
        mDelegate.mCurrentMonthViewItem = 12 * y + mDelegate.mSelectedCalendar.getMonth() - mDelegate.getMinYearMonth();
        mMonthPager.setup(mDelegate);
        mMonthPager.setCurrentItem(mDelegate.mCurrentMonthViewItem);
        mSelectLayout.setOnMonthSelectedListener(new MonthRecyclerView.OnMonthSelectedListener() {
            @Override
            public void onMonthSelected(int year, int month) {
                int position = 12 * (year - mDelegate.getMinYear()) + month - mDelegate.getMinYearMonth();
                closeSelectLayout(position);
            }
        });
        mSelectLayout.setSchemeColor(mDelegate.getSchemeThemeColor());
        mWeekPager.updateSelected(mDelegate.mSelectedCalendar);

    }


    /**
     * 获取当天
     *
     * @return 返回今天
     */
    public int getCurDay() {
        return mDelegate.getCurrentDay().getDay();
    }
    /*
    * 获取当天的农历
    * */
    public String getLunar(){
        return mDelegate.getCurrentDay().getLunar();
    }
    /*
    *
    * 获取星期几
    * */
    public int getWeek(String date){
        int weekNumber = mDelegate.getCurrentDay().getWeek();
        switch (date){
            case Week.Time.YESTERDAY:
                if (mDelegate.getCurrentDay().getWeek() < 2 ){
                    weekNumber = mDelegate.getCurrentDay().getWeek() + 6;
                } else {
                    weekNumber = mDelegate.getCurrentDay().getWeek() - 1;
                }
                break;
            case Week.Time.TODAY:
                weekNumber = mDelegate.getCurrentDay().getWeek();
                break;
            case Week.Time.TOMORROW:
                if (mDelegate.getCurrentDay().getWeek() > 6 ){
                    weekNumber = mDelegate.getCurrentDay().getWeek() - 6;
                } else {
                    weekNumber = mDelegate.getCurrentDay().getWeek() + 1;
                }
                break;
            default:
                break;

        }
        return weekNumber;
    }
    /*
    * 获取明天的日期
    *
    * */
    public Map<String,Integer> getTomoData(){

        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
        String[] months_little = { "4", "6", "9", "11" };

        List<String> list_big = Arrays.asList(months_big);
        List<String> list_little = Arrays.asList(months_little);

        int year = this.getCurYear();
        int month = this.getCurMonth();
        int day = this.getCurDay();
        if(day==30){
            if (list_big.contains(String.valueOf(month))){
                day=31;
            }
            if (list_little.contains(String.valueOf(month))){
                day=1;
                if(month==12){
                    year++;
                    month=1;
                }else{
                    month++;
                }

            }
        }else if(day==31){
            day=1;
            if(month==12){
                year++;
                month=1;
            }else{
                month++;
            }

        }else if (month == 2){
            if (Util.isLeapYear(year)){
               if (day == 29){
                   month++;
                   day = 1;
               }
            }else {
                if (day == 28){
                    month++;
                    day = 1;
                }
            }

        } else {
            day++;
        }
       Map<String,Integer> map = new HashMap<>();
       map.put("year",year);
       map.put("month",month);
       map.put("day",day);
        return map;
    }
    /*
   * 获取昨天的日期
   *
   * */
    public Map<String,Integer> getYesData(){

        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
        String[] months_little = { "4", "6", "9", "11" };

        List<String> list_big = Arrays.asList(months_big);
        List<String> list_little = Arrays.asList(months_little);

        int year = this.getCurYear();
        int month = this.getCurMonth();
        int day = this.getCurDay();
        if (day == 1){
            if (list_big.contains(String.valueOf(month))){
                if (month == 8 || month == 1){
                    day = 31;
                }else if (month == 3){
                    if (Util.isLeapYear(year)){
                        day = 29;
                    }else {
                        day = 28;
                    }
                }else {
                    day = 30;
                }
            }
            if (list_little.contains(String.valueOf(month))) {
                  day = 31;
            }
            if (month == 1){
                month = 12;
                year--;
            }else {
                month--;
            }
        }else {
            day--;
        }
        Map<String,Integer> map = new HashMap<>();
        map.put("year",year);
        map.put("month",month);
        map.put("day",day);
        return map;
    }
    /**
     * 获取本月
     *
     * @return 返回本月
     */
    public int getCurMonth() {
        return mDelegate.getCurrentDay().getMonth();
    }

    /**
     * 获取本年
     *
     * @return 返回本年
     */
    public int getCurYear() {
        return mDelegate.getCurrentDay().getYear();
    }

    /**
     * 打开日历月份快速选择
     *
     * @param year 年
     */
    public void showSelectLayout(final int year) {
        if (mParentLayout != null && mParentLayout.mContentView != null) {
            mParentLayout.expand();
            mParentLayout.mContentView.setVisibility(GONE);
        }
        mWeekPager.setVisibility(GONE);
        view.setVisibility(GONE);
        mWeekBar.animate()
                .translationY(-mWeekBar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(GONE);
                        mSelectLayout.setVisibility(VISIBLE);
                        mSelectLayout.init(year);
                    }
                });

        mMonthPager.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(180)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mMonthPager.setVisibility(GONE);
                    }
                });
    }

    /**
     * 滚动到当前
     */
    public void scrollToCurrent() {
        if(!Util.isCalendarInRange(mDelegate.getCurrentDay(), mDelegate)){
            return;
        }
        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate();
        if (mDelegate.mDateSelectedListener != null &&
                mDelegate.mCurrentWeekViewItem == mWeekPager.getCurrentItem()) {
            mDelegate.mDateSelectedListener.onDateSelected(mDelegate.createCurrentDate());
        }
        mWeekPager.scrollToCurrent();
        mMonthPager.scrollToCurrent();
        if (mDelegate.mDateChangeListener != null) {
            mDelegate.mDateChangeListener.onDateChange(mDelegate.createCurrentDate());
        }
        mSelectLayout.scrollToYear(mDelegate.getCurrentDay().getYear());
    }

    /**
     * 滚动到下一个月
     */
    public void scrollToNext() {
        if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.getCurrentItem() + 1);
        } else {
            mMonthPager.setCurrentItem(mMonthPager.getCurrentItem() + 1);
        }

    }

    /**
     * 滚动到上一个月
     */
    public void scrollToPre() {
        if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.getCurrentItem() - 1);
        } else {
            mMonthPager.setCurrentItem(mMonthPager.getCurrentItem() - 1);
        }
    }

    /**
     * 滚动到指定日期
     *
     * @param year  year
     * @param month month
     * @param day   day
     */
    public void scrollToCalendar(int year, int month, int day) {
        if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.scrollToCalendar(year, month, day);
        } else {
            mMonthPager.scrollToCalendar(year, month, day);
        }
    }

    /**
     * 滚动到某一年
     *
     * @param year 快速滚动的年份
     */
    @SuppressWarnings("unused")
    public void scrollToYear(int year) {
        mMonthPager.setCurrentItem(12 * (year - mDelegate.getMinYear()) +
                mDelegate.getCurrentDay().getMonth() - mDelegate.getMinYearMonth());
        mSelectLayout.scrollToYear(year);
    }

    /**
     * 关闭日历布局，同时会滚动到指定的位置
     *
     * @param position 某一年
     */
    public void closeSelectLayout(final int position) {
        Log.v("1218","guandaoyiyue");
        mSelectLayout.setVisibility(GONE);
        mWeekBar.setVisibility(VISIBLE);
        mMonthPager.setVisibility(VISIBLE);
        if (position == mMonthPager.getCurrentItem()) {
            Calendar calendar = new Calendar();
            calendar.setYear((position + mDelegate.getMinYearMonth() - 1) / 12 + mDelegate.getMinYear());
            calendar.setMonth((position + mDelegate.getMinYearMonth() - 1) % 12 + 1);
            calendar.setDay(1);
            calendar.setLunar(LunarCalendar.getLunarText(calendar));
            mDelegate.mSelectedCalendar = calendar;
            if (mDelegate.mDateChangeListener != null) {
                Log.v("1218","guandaoyiyue11");
                mDelegate.mDateChangeListener.onDateChange(calendar);
            }
            if (mDelegate.mDateSelectedListener != null) {
                Log.v("1218","guandaoyiyue22");
                mDelegate.mDateSelectedListener.onDateSelected(calendar);
            }
        } else {
            Log.v("1218","guandaoyiyue33b" + position);
            mMonthPager.setCurrentItem(position, true);
        }
        mWeekBar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(VISIBLE);
                        if (mParentLayout != null && mParentLayout.mContentView != null) {
                            mParentLayout.mContentView.setVisibility(VISIBLE);
                        }
                    }
                });
        mMonthPager.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(180)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mMonthPager.setVisibility(VISIBLE);

                    }
                });
    }

    /**
     * 点击按钮月，关闭日历布局，同时会滚动到当前日期
     *
     */
    public void selectCurrentMonth( ) {
        Log.v("1218","指定未知");
        mSelectLayout.setVisibility(GONE);
        mWeekBar.setVisibility(VISIBLE);
        mMonthPager.setVisibility(VISIBLE);
        view.setVisibility(VISIBLE);
        Calendar calendar = new Calendar();
        calendar.setYear(getCurYear());
        calendar.setMonth(getCurMonth());
        calendar.setDay(getCurDay());
        calendar.setLunar(LunarCalendar.getLunarText(calendar));
        mDelegate.mSelectedCalendar = calendar;
        if (mDelegate.mDateChangeListener != null) {
            mDelegate.mDateChangeListener.onDateChange(calendar);
        }
        if (mDelegate.mDateSelectedListener != null) {
            mDelegate.mDateSelectedListener.onDateSelected(calendar);
        }
        mWeekBar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(VISIBLE);
                        if (mParentLayout != null && mParentLayout.mContentView != null) {
                            mParentLayout.mContentView.setVisibility(VISIBLE);
                        }
                    }
                });
        mMonthPager.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(180)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mMonthPager.setVisibility(VISIBLE);

                    }
                });
    }

    /**
     * 日期改变监听器
     *
     * @param listener 监听
     */
    @Deprecated
    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.mDelegate.mDateChangeListener = listener;
        if (mDelegate.mDateChangeListener != null) {
            mDelegate.mDateChangeListener.onDateChange(mDelegate.mSelectedCalendar);
        }

    }


    /**
     * 年份改变事件
     *
     * @param listener listener
     */
    @SuppressWarnings("unused")
    public void setOnYearChangeListener(OnYearChangeListener listener) {
        this.mDelegate.mYearChangeListener = listener;
    }

    /**
     * 设置日期选中事件
     *
     * @param listener 日期选中事件
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.mDelegate.mDateSelectedListener = listener;
        if (mDelegate.mDateSelectedListener != null) {
            if(!Util.isCalendarInRange(mDelegate.mSelectedCalendar, mDelegate)){
                return;
            }
            mDelegate.mDateSelectedListener.onDateSelected(mDelegate.mSelectedCalendar);
        }
    }

    /**
     * 初始化时初始化日历卡默认选择位置
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getParent() != null && getParent() instanceof CalendarLayout) {
            mParentLayout = (CalendarLayout) getParent();
            mParentLayout.mItemHeight = mDelegate.getCalendarItemHeight();
            mMonthPager.mParentLayout = mParentLayout;
            mWeekPager.mParentLayout = mParentLayout;
            mParentLayout.initCalendarPosition(mDelegate.mSelectedCalendar);
            mParentLayout.initStatus();
        }
    }

    /**
     * 设置背景色
     *
     * @param monthLayoutBackground 月份卡片的背景色
     * @param weekBackground        星期栏背景色
     * @param lineBg                线的颜色
     */
    @SuppressWarnings("unused")
    public void setBackground(int monthLayoutBackground, int weekBackground, int lineBg) {
        mWeekBar.setBackgroundColor(weekBackground);
        mSelectLayout.setBackgroundColor(monthLayoutBackground);
        findViewById(R.id.line).setBackgroundColor(lineBg);
    }


    /**
     * 设置文本颜色
     *
     * @param curMonthTextColor        当前月份字体颜色
     * @param otherMonthColor          其它月份字体颜色
     * @param curMonthLunarTextColor   当前月份农历字体颜色
     * @param otherMonthLunarTextColor 其它农历字体颜色
     */
    @Deprecated
    public void setTextColor(int curMonthTextColor,
                             int otherMonthColor,
                             int curMonthLunarTextColor,
                             int otherMonthLunarTextColor) {
        mDelegate.setTextColor(mDelegate.getCurDayTextColor(), curMonthTextColor, otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor);
    }


    /**
     * 设置文本颜色
     *
     * @param currentDayTextColor      今天字体颜色
     * @param curMonthTextColor        当前月份字体颜色
     * @param otherMonthColor          其它月份字体颜色
     * @param curMonthLunarTextColor   当前月份农历字体颜色
     * @param otherMonthLunarTextColor 其它农历字体颜色
     */
    @SuppressWarnings("unused")
    public void setTextColor(
            int currentDayTextColor,
            int curMonthTextColor,
            int otherMonthColor,
            int curMonthLunarTextColor,
            int otherMonthLunarTextColor) {
        mDelegate.setTextColor(currentDayTextColor, curMonthTextColor, otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor);
    }

    /**
     * 设置选择的效果
     *
     * @param selectedThemeColor 选中的标记颜色
     * @param selectedTextColor  选中的字体颜色
     */
    @Deprecated
    public void setSelectedColor(int selectedThemeColor, int selectedTextColor) {
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, mDelegate.getSelectedLunarTextColor());
    }


    /**
     * 设置选择的效果
     *
     * @param selectedThemeColor     选中的标记颜色
     * @param selectedTextColor      选中的字体颜色
     * @param selectedLunarTextColor 选中的农历字体颜色
     */
    @Deprecated
    public void setSelectedColor(int selectedThemeColor, int selectedTextColor, int selectedLunarTextColor) {
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor);
    }


    /**
     * 设置标记的色
     *
     * @param schemeColor     标记背景色
     * @param schemeTextColor 标记字体颜色
     */
    @Deprecated
    public void setSchemeColor(int schemeColor, int schemeTextColor) {
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, mDelegate.getSchemeLunarTextColor());
        mSelectLayout.setSchemeColor(schemeColor);
    }

    /**
     * 设置标记的色
     *
     * @param schemeColor     标记背景色
     * @param schemeTextColor 标记字体颜色
     */
    @Deprecated
    public void setSchemeColor(int schemeColor, int schemeTextColor, int schemeLunarTextColor) {
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor);
        mSelectLayout.setSchemeColor(schemeColor);
    }

    /**
     * 设置星期栏的背景和字体颜色
     *
     * @param weekBackground 背景色
     * @param weekTextColor  字体颜色
     */
    @SuppressWarnings("unused")
    public void setWeeColor(int weekBackground, int weekTextColor) {
        mWeekBar.setBackgroundColor(weekBackground);
        mWeekBar.setTextColor(weekTextColor);
    }


    /**
     * 更新界面，
     * 重新设置颜色等都需要调用该方法
     */
    @SuppressWarnings("unused")
    public void update() {
        mSelectLayout.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    /**
     * 获取选择的日期
     */
    @SuppressWarnings("unused")
    public Calendar getSelectedCalendar() {
        return mDelegate.mSelectedCalendar;
    }


    /**
     * 年份改变事件，快速年份切换
     */
    public interface OnYearChangeListener {
        void onYearChange(int year);
    }

    /**
     * 日期改变、左右切换、快速年份、月份切换
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public interface OnDateChangeListener {
        /**
         * 这个方法是准确传递的，但和onDateSelected一样会跟新日历选中状态，造成误区，故新版本建议弃用，
         * 统一使用onDateSelected
         */
        @Deprecated
        void onDateChange(Calendar calendar);

        @Deprecated
        void onYearChange(int year);
    }


    /**
     * 内部日期选择，不暴露外部使用
     */
    interface OnInnerDateSelectedListener {
        void onDateSelected(Calendar calendar);

        void onWeekSelected(Calendar calendar);
    }

    /**
     * 外部日期选择事件
     */
    public interface OnDateSelectedListener {
        void onDateSelected(Calendar calendar);
    }
}
