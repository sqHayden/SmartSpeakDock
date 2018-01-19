package com.idx.smartspeakdock.service;

import android.support.v4.app.Fragment;

import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;

/**
 * Created by ryan on 18-1-16.
 * Email: Ryan_chan01212@yeah.net
 */

public interface ControllerServiceListener {
    void onReturnWeburl(ShoppingCallBack shoppingCallBack);
    void onTop(boolean isTopActivity, Fragment isTopFragment);
    void setCalendarControllerListener(CalendarCallBack calendarCallBack);
}
