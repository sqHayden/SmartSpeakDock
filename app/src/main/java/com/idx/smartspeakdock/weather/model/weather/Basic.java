package com.idx.smartspeakdock.weather.model.weather;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 17-11-14.
 */

public class Basic {
    @SerializedName("location")
    public String cityName;
    @SerializedName("cid")
    public String weatherId;
}
