package com.idx.smartspeakdock.weather.utils;

import com.idx.smartspeakdock.R;

/**
 * Created by danny on 12/15/17.
 */

public class HandlerWeatherUtil {

    /**
     * 天气代码 100，900 为晴 101-213，503-508，901 为阴 300-313为雨  400-407 为雪  500-502 为雾霾
     *
     * @param code 天气代码
     * @return 天气类型
     */
    public static String getWeatherType(int code) {
        if (code == 100 || code==900) {
            return "晴";
        }
        if ((code >= 101 && code <= 213) || (code >= 500 && code <= 508) || (code==901)) {
            return "阴";
        }
        if (code >= 300 && code <= 313) {
            return "雨";
        }
        if (code>=400 && code<=407){
            return "雪";
        }
        if (code>=500 && code<=502){
            return "雾";
        }
        return "未知";
    }

    /**
     * 天气代码 100，900 为晴 101-213，503-508，901 为阴 300-313为雨  400-407 为雪  500-502 为雾霾
     *
     * @param code 天气代码
     * @return 天气图标
     */
    public static int getWeatherImageResource(int code){
        if (code == 100 || code==900) {
            return R.drawable.weather_sunny;
        }
        if ((code >= 101 && code <= 213) || (code >= 500 && code <= 508) || (code==901)) {
            return R.drawable.weather_cloudy;
        }
        if (code >= 300 && code <= 313) {
            return R.drawable.weather_thunderstorm;
        }
        if (code>=400 && code<=407){
            return R.drawable.weather_snow;
        }
        if (code>=500 && code<=502){
            return R.drawable.weather_smog;
        }
        return R.drawable.weather_unknown;
    }
}
