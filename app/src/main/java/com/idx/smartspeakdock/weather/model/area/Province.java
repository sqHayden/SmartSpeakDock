package com.idx.smartspeakdock.weather.model.area;

import org.litepal.crud.DataSupport;

/**
 * Created by steve on 12/14/17.
 */

public class Province extends DataSupport{
    private int id;
    private String provinceName;//省名

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
