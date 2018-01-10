package com.idx.smartspeakdock.weather.model.area;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by steve on 12/14/17.
 */

@Entity(tableName = "county")
public class County {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String countyName;

    public String weatherId;

    public int cityId;
}
