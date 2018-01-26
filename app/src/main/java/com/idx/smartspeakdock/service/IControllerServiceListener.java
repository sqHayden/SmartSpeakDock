package com.idx.smartspeakdock.service;

import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;

/**
 * Created by ryan on 18-1-16.
 * Email: Ryan_chan01212@yeah.net
 */

public interface IControllerServiceListener {
    ControllerService getControlService();
    void onReturnWeburl(ShoppingCallBack shoppingCallBack);
    void setCalendarControllerListener(CalendarCallBack calendarCallBack);
    void setWeatherControllerListener(WeatherCallback weatherCallback);
    void onGetMusicName(MusicCallBack musicCallBack);
    void setMapControllerListener(MapCallBack mapCallBack);
}
