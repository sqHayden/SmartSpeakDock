package com.idx.smartspeakdock.weather.model.weather;

import android.arch.persistence.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by danny on 17-11-14.
 */
@Entity
public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;
    @SerializedName("air_now_city")
    public Air air;
}
