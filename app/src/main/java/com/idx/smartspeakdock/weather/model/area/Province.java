package com.idx.smartspeakdock.weather.model.area;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


/**
 * Created by danny on 12/14/17.
 */

@Entity(tableName = "province")
public class Province {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String provinceName;

    public int provinceCode;
}