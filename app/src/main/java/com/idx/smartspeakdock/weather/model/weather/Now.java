package com.idx.smartspeakdock.weather.model.weather;

import com.google.gson.annotations.SerializedName;

/**
 * Created by danny on 17-11-14.
 */

public class Now {
    @SerializedName("tmp")
    public String tmperature;
    @SerializedName("cond_code")
    public String code;
}
