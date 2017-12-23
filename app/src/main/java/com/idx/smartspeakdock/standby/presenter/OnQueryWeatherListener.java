package com.idx.smartspeakdock.standby.presenter;

import com.idx.smartspeakdock.weather.model.weather.Weather;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public interface OnQueryWeatherListener {
    void onSuccess(Weather weather);
    void onError(String errorMsg);
}
