package com.idx.smartspeakdock.weather.model.weatherroom;

import android.content.Context;

import com.idx.smartspeakdock.data.local.SmartDatabase;
import com.idx.smartspeakdock.utils.AppExecutors;

/**
 * Created by danny on 1/6/18.
 */

public class WeatherBasicInjection {
    public static WeatherBasicRepository getNoteRepository(Context context){
        SmartDatabase smartDatabase=SmartDatabase.getInstance(context);
        return WeatherBasicRepository.getInstance(RemoteWeatherBasicDataSource.getInstance(new AppExecutors(),smartDatabase.weatherBasicDao()));
    }
}
