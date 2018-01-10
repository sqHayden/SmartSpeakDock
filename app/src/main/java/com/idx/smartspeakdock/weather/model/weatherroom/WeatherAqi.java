package com.idx.smartspeakdock.weather.model.weatherroom;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * Created by danny on 1/5/18.
 */
@Entity(tableName = "weather_aqi")
public class WeatherAqi {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public String cityName;
    public String weatherAqi;

    @Override
    public String toString() {
        return "WeatherAqi{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", cityName='" + cityName + '\'' +
                ", weatherAqi='" + weatherAqi + '\'' +
                '}';
    }
}
