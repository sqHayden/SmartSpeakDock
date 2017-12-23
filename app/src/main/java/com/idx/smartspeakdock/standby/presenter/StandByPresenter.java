package com.idx.smartspeakdock.standby.presenter;

import android.content.Context;

import com.idx.smartspeakdock.standby.IStandByView;
import com.idx.smartspeakdock.standby.mode.IStandByMode;
import com.idx.smartspeakdock.standby.mode.StandByMode;
import com.idx.smartspeakdock.weather.model.weather.Weather;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class StandByPresenter implements IStandByPresenter,OnQueryWeatherListener{
    IStandByView mStandByView;
    IStandByMode mStandByMode;
    public StandByPresenter(IStandByView iStandByView, Context context){
        mStandByView = iStandByView;
        mStandByMode = new StandByMode(context);

    }
    @Override
    public void onSuccess(Weather weather) {
        mStandByView.setCurrentCityWeatherInfo(weather);
    }

    @Override
    public void onError(String errorMsg) {
        mStandByView.onError(errorMsg);
    }

    @Override
    public void requestWeather(String cityName) {
        mStandByMode.requestWeather(cityName,this);
    }
}
