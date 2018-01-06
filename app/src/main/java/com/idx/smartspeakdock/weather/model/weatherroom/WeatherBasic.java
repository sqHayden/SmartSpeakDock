package com.idx.smartspeakdock.weather.model.weatherroom;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by danny on 17-11-14.
 */
@Entity(tableName = "weather_basic")
public class WeatherBasic {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String cityName;
    public String weatherBasic;
}
