package com.idx.smartspeakdock.standby.mode;

import com.idx.smartspeakdock.standby.presenter.OnQueryWeatherListener;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public interface IStandByMode {
    void requestWeather(String cityName, OnQueryWeatherListener onQueryWeatherListener);
}
