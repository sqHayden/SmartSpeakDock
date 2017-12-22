package com.idx.smartspeakdock.weather.model.area;

import org.litepal.crud.DataSupport;

/**
 * Created by danny on 12/14/17.
 */

public class City extends DataSupport{
    private int id;
    private String cityName;//市名
    private int provinceId;//市所属省代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
