package com.idx.smartspeakdock.weather.model.weatherroom;

import android.content.Context;

import com.idx.smartspeakdock.data.local.SmartDatabase;
import com.idx.smartspeakdock.utils.AppExecutors;

/**
 * Created by danny on 1/6/18.
 */

public class WeatherAqiInjection {
    public static WeatherAqiRepository getInstance(Context context){
        SmartDatabase smartDatabase=SmartDatabase.getInstance(context);
        return WeatherAqiRepository.getInstance(RemoteWeatherAqiDataSource.getInstance(new AppExecutors(),smartDatabase.weatherAqiDao()));
    }
}
