package com.idx.smartspeakdock.weather.model;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by danny on 12/21/17.
 */

public class WeatherModelImpl implements WeatherModel {

    /**
     * 加载城市天气
     *
     * @param name 城市名称
     * @param listener 获取数据结果监听
     */
    @Override
    public void loadWeather(String name, final OnWeatherListener listener) {
        RequestQueue queue= Volley.newRequestQueue(SpeakerApplication.getContext());
        JsonObjectRequest request=new JsonObjectRequest(
                "https://free-api.heweather.com/s6/weather?location="+name+"&key=537664b7e2124b3c845bc0b51278d4af",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONArray jsonArray= null;
                try {
                    jsonArray = jsonObject.getJSONArray("HeWeather6");
                    String weatherContent=jsonArray.getJSONObject(0).toString();
                    Weather weather=new Gson().fromJson(weatherContent,Weather.class);
                    if (weather!=null){
                        listener.onSuccess(weather);
                    }else {
                        listener.onError();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onError();
            }
        });
        queue.add(request);
    }

    /**
     * 加载城市空气质量
     *
     * @param cityName 城市名称
     * @param listener 获取数据结果监听
     */
    @Override
    public void loadWeatherAqi(String cityName, final OnWeatherListener listener) {
        RequestQueue queue= Volley.newRequestQueue(SpeakerApplication.getContext());
        JsonObjectRequest request=new JsonObjectRequest(
                "https://free-api.heweather.com/s6/air/now?location="+cityName+"&key=537664b7e2124b3c845bc0b51278d4af",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONArray jsonArray= null;
                try {
                    jsonArray = jsonObject.getJSONArray("HeWeather6");
                    String weatherContent=jsonArray.getJSONObject(0).toString();
                    Weather weather=new Gson().fromJson(weatherContent,Weather.class);
                    if (weather!=null){
                        listener.onSuccessAqi(weather);
                    }else {
                        listener.onError();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onError();
            }
        });
        queue.add(request);
    }
}
