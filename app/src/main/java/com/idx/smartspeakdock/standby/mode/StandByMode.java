package com.idx.smartspeakdock.standby.mode;

import android.content.Context;
import android.util.Log;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.standby.HttpUtil;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.standby.presenter.OnQueryWeatherListener;
import com.idx.smartspeakdock.weather.model.weather.Weather;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class StandByMode implements IStandByMode {
    private static final String TAG = StandByMode.class.getSimpleName();
    private Context mContext;
    public StandByMode(Context context){
        mContext = context;
    }
//    bc0418b57b2d4918819d3974ac1285d9   537664b7e2124b3c845bc0b51278d4af
    @Override
    public void requestWeather(String cityName, final OnQueryWeatherListener onQueryWeatherListener) {
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location="+cityName+"&key=537664b7e2124b3c845bc0b51278d4af";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: weather.status = "+weather.status);
                        if (weather != null &&"ok".equals(weather.status)) {
                            if(onQueryWeatherListener != null) {
                                onQueryWeatherListener.onSuccess(weather);
                            }
                        } else {
                            if(onQueryWeatherListener != null){
                                onQueryWeatherListener.onError(mContext.getResources().getString(R.string.get_weather_info_error));
                            }
                        }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(onQueryWeatherListener != null){
                    onQueryWeatherListener.onError(mContext.getResources().getString(R.string.get_weather_info_error));
                }
            }
        });
    }
}
