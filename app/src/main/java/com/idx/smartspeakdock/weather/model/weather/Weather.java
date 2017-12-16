package com.idx.smartspeakdock.weather.model.weather;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by steve on 17-11-14.
 */

public class Weather {

    public Basic basic;
    public Now now;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;
    @SerializedName("air_now_city")
    public Air air;
}
