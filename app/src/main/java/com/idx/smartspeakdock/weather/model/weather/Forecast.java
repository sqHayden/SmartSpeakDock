package com.idx.smartspeakdock.weather.model.weather;

import com.google.gson.annotations.SerializedName;

/**
 * Created by danny on 17-11-14.
 */

public class Forecast {
    public String date;

    @SerializedName("cond_code_d")
    public String code;

    @SerializedName("tmp_max")
    public String max;

    @SerializedName("tmp_min")
    public String min;
}
