package com.idx.smartspeakdock.standby;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.weather.model.area.AreaInjection;
import com.idx.smartspeakdock.weather.model.area.AreaRepository;
import com.idx.smartspeakdock.weather.model.area.City;
import com.idx.smartspeakdock.weather.model.area.County;
import com.idx.smartspeakdock.weather.model.area.Province;
import com.idx.smartspeakdock.weather.model.weather.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    private static final String TAG = Utility.class.getSimpleName();
    private static AreaRepository mAreaRepository= AreaInjection.provideUserRepository(SpeakerApplication.getContext());

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.provinceName=provinceObject.getString("name");
                    province.provinceCode=provinceObject.getInt("id");
                    mAreaRepository.saveProvince(province);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.cityName=cityObject.getString("name");
                    city.cityCode=cityObject.getInt("id");
                    city.provinceId=provinceId;
                    mAreaRepository.saveCity(city);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.countyName=countyObject.getString("name");
                    county.weatherId=countyObject.getString("weather_id");
                    county.cityId=cityId;
                    mAreaRepository.saveCounty(county);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Log.i(TAG, "handleWeatherResponse: weatherContent = "+weatherContent);
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
