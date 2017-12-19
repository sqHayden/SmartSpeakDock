package com.idx.smartspeakdock.standby;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class StandbyActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private TextView data;
    private Intent intent;
    private LinearLayout layout;
    private TextView location_textView;
    private TextView standby_life_clothes;
    private TextView standby_life_car;
    private TextView standby_weather_tmp;

    private LocationClient mLocationClient;
    private ImageView weatherIcon;
    private String cityname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new StandbyLocationListener());
        setContentView(R.layout.activity_standby);
        init();
        data.setText(DataString.StringData());
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StandbyActivity.this, SwipeActivity.class);
                startActivity(intent);
                mLocationClient.stop();
            }
        });
        requestLocation();
        cityname = location_textView.getText().toString().trim();
        queryWeather(cityname);

    }

    public void init(){
        layout = findViewById(R.id.line6);
        weatherIcon = findViewById(R.id.weatherIcon);
        location_textView = findViewById(R.id.location_textView);
        data = findViewById(R.id.data_textView);
        standby_life_clothes = findViewById(R.id.standby_life_clothes);
        standby_life_car = findViewById(R.id.standby_life_car);
        standby_weather_tmp = findViewById(R.id.standby_weather_tmp);
    }

    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }


    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(10);
        mLocationClient.setLocOption(option);
    }

    private class StandbyLocationListener  implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d(TAG, "onReceiveLocation: "+ bdLocation.getCity());
            location_textView.setText(bdLocation.getCity());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    public void queryWeather(String cityName){
        RequestQueue queue= Volley.newRequestQueue(this);
        JsonObjectRequest request=new JsonObjectRequest(
                "https://free-api.heweather.com/s6/weather?location="+cityName+"&key=537664b7e2124b3c845bc0b51278d4af",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                JSONArray jsonArray= null;
                Log.d(TAG, jsonObject.toString());
                try {
                    jsonArray = jsonObject.getJSONArray("HeWeather6");
                    String weatherContent=jsonArray.getJSONObject(0).toString();
                    Weather weather=new Gson().fromJson(weatherContent,Weather.class);
                    weatherIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
                    standby_weather_tmp.setText(weather.forecastList.get(0).max+"℃ / "+weather.forecastList.get(0).min+"℃");
                    standby_life_clothes.setText("穿衣：" + weather.lifestyleList.get(1).brf);
                    standby_life_car.setText("洗车：" + weather.lifestyleList.get(6).brf);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,volleyError.getMessage(),volleyError );
            }
        });
        queue.add(request);
    }
}