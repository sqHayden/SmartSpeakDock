package com.idx.smartspeakdock.weather.model.weatherroom;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by steve on 1/5/18.
 */
@Entity(tableName = "weather_aqi")
public class WeatherAqi {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String cityName;
    public String weatherAqi;
}
