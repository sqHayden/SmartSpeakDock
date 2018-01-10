package com.idx.smartspeakdock.weather.model.area;

import android.content.Context;

import com.idx.smartspeakdock.data.local.SmartDatabase;
import com.idx.smartspeakdock.utils.AppExecutors;

/**
 * Created by steve on 12/20/17.
 */

public class AreaInjection {
    public static AreaRepository provideUserRepository(Context context){
        SmartDatabase smartDatabase = SmartDatabase.getInstance(context);

        return AreaRepository.getInstance(LocalAreaDataSource.getInstance(new AppExecutors(),
                smartDatabase.provinceDao(),smartDatabase.cityDao(),smartDatabase.countyDao()));
    }
}
