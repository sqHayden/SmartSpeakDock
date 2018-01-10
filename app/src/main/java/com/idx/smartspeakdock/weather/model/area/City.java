package com.idx.smartspeakdock.weather.model.area;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by danny on 12/14/17.
 */

@Entity(tableName = "city")
public class City {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String cityName;

    public int cityCode;

    public int provinceId;
}
