package com.idx.smartspeakdock.weather.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.weather.model.weather.Forecast;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by steve on 12/13/17.
 */

public class WeatherActivity extends AppCompatActivity implements ChooseCityDialogFragment.OnChooseCityCompleted{

    private static final String TAG = "WeatherActivity";
    private TextView mNowTemperature,mCond,mTemperature,
            mDate,mTitle,mLifestyleClothes,mLifestyleCar,
            mLifestyleAir,mAirQuality,mPM10,mPM25,mNO2,mSO2,mO3,mCO;
    private ImageView mWeatherNowIcon;
    private ListView mListView;
    private String weatherUrl="https://free-api.heweather.com/s6/weather?location=深圳&key=537664b7e2124b3c845bc0b51278d4af";
    private String airUrl="https://free-api.heweather.com/s6/air/now?location=深圳&key=537664b7e2124b3c845bc0b51278d4af";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_weather);
        initView();
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCityDialogFragment cityDialogFragment=new ChooseCityDialogFragment();
                cityDialogFragment.setOnChooseCityCompleted(WeatherActivity.this);
                cityDialogFragment.show(getFragmentManager(),"ChooseCityDialog");
            }
        });
        queryWeather();
        queryAir();
    }
    private void initView(){
        mWeatherNowIcon=findViewById(R.id.weather_now_icon);
        mNowTemperature=findViewById(R.id.weather_now_temperature);
        mCond=findViewById(R.id.weather_now_cond_txt_n);
        mTemperature=findViewById(R.id.weather_daily_forecast_tmp_max_min);
//        mDate=findViewById(R.id.weather_temperature_date);
        mTitle=findViewById(R.id.weather_title);
        mLifestyleClothes=findViewById(R.id.weather_lifestyle_clothes_text);
        mLifestyleCar=findViewById(R.id.weather_lifestyle_car_text);
        mLifestyleAir=findViewById(R.id.weather_lifestyle_air_text);
        mListView=findViewById(R.id.weather_daily_forecast_list);
        mAirQuality=findViewById(R.id.weather_air_now_city);
        mPM10=findViewById(R.id.weather_air_pm10);
        mPM25=findViewById(R.id.weather_air_pm25);
        mNO2=findViewById(R.id.weather_air_no2);
        mSO2=findViewById(R.id.weather_air_so2);
        mO3=findViewById(R.id.weather_air_o3);
        mCO=findViewById(R.id.weather_air_co);
    }

    /**
     * 查询并解析天气
     */
    public void queryWeather(){
        RequestQueue queue= Volley.newRequestQueue(this);
        JsonObjectRequest request=new JsonObjectRequest(
                weatherUrl,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                JSONArray jsonArray= null;
                Log.d(TAG, jsonObject.toString());
                try {
                    jsonArray = jsonObject.getJSONArray("HeWeather6");
                    String weatherContent=jsonArray.getJSONObject(0).toString();
                    Weather weather=new Gson().fromJson(weatherContent,Weather.class);
                    mWeatherNowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
                    mNowTemperature.setText(weather.now.tmperature+"℃");
                    mCond.setText(HandlerWeatherUtil.getWeatherType(Integer.parseInt(weather.now.code)));
                    mTemperature.setText(weather.forecastList.get(0).max+"℃ / "+weather.forecastList.get(0).min+"℃");
                    mLifestyleClothes.setText(weather.lifestyleList.get(1).brf);
                    mLifestyleCar.setText(weather.lifestyleList.get(6).brf);
                    mLifestyleAir.setText(weather.lifestyleList.get(7).brf);
//                    mDate.setText(new SimpleDateFormat("HH:mm").format(new Date()));
                    MyAdapter myAdapter=new MyAdapter(weather.forecastList,getApplicationContext());
                    mListView.setAdapter(myAdapter);
                    Log.d(TAG, "onResponse: "+weather.basic.cityName+":"+weather.now.code);
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
        Log.d(TAG, "onCreate: 2");
        queue.add(request);
    }

    /**
     * 查询并解析空气质量
     */
    public void queryAir(){
        RequestQueue queue= Volley.newRequestQueue(this);
        JsonObjectRequest request=new JsonObjectRequest(
                airUrl,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d(TAG, jsonObject.toString());
                JSONArray jsonArray= null;
                try {
                    jsonArray = jsonObject.getJSONArray("HeWeather6");
                    String weatherContent=jsonArray.getJSONObject(0).toString();
                    Weather weather=new Gson().fromJson(weatherContent,Weather.class);
                    mAirQuality.setText(weather.air.qlty);
                    mPM10.setText(weather.air.pm10);
                    mPM25.setText(weather.air.pm25);
                    mNO2.setText(weather.air.no2);
                    mSO2.setText(weather.air.so2);
                    mO3.setText(weather.air.o3);
                    mCO.setText(weather.air.co);
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
        Log.d(TAG, "onCreate: 2");
        queue.add(request);
    }

    @Override
    public void chooseCityCompleted(String countyName, String cityNime) {
        weatherUrl="https://free-api.heweather.com/s6/weather?location="+countyName+"&key=537664b7e2124b3c845bc0b51278d4af";
        queryWeather();
        Log.d(TAG,cityNime);
        if (cityNime.equals("东城")||cityNime.equals("西城")){
            cityNime="北京";
        }else if (cityNime.equals("黄浦")||cityNime.equals("长宁")||
                cityNime.equals("静安")||cityNime.equals("普陀")||
                cityNime.equals("虹口")||cityNime.equals("杨浦")){
            cityNime="上海";
        }else if (cityNime.equals("和平")||cityNime.equals("河东")||
                cityNime.equals("河西")||cityNime.equals("南开")||
                cityNime.equals("河北")||cityNime.equals("红桥")){
            cityNime="天津";
        }else if (cityNime.equals("渝中")||cityNime.equals("大渡口")||
                cityNime.equals("江北")||cityNime.equals("沙坪坝")||
                cityNime.equals("九龙坡")||cityNime.equals("南岸")||cityNime.equals("开州")){
            cityNime="重庆";
        }
        Log.d(TAG, "chooseCityCompleted: "+cityNime);
        airUrl="https://free-api.heweather.com/s6/air/now?location="+cityNime+"&key=537664b7e2124b3c845bc0b51278d4af";
        if (cityNime.equals("香港")||cityNime.equals("澳门")||cityNime.equals("台北")||cityNime.equals("高雄")||cityNime.equals("台中")){

        }else {
            queryAir();
        }
        mTitle.setText(countyName);
    }
}

class MyAdapter extends BaseAdapter {
    private List<Forecast> mList;
    private LayoutInflater mInflater;
    public MyAdapter(List<Forecast> list, Context context){
        mInflater= LayoutInflater.from(context);
        mList=list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view==null){
            view=mInflater.inflate(R.layout.activity_weather_daily_forecast_item,viewGroup,false);
        }

        String date=parseDate(i);
        ((TextView)view.findViewById(R.id.weather_daily_forecast_item_date)).setText(date);
        ((ImageView)view.findViewById(R.id.weather_daily_forecast_item_icon)).setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(mList.get(i).code)));
        ((TextView)view.findViewById(R.id.weather_daily_forecast_item_max_min)).setText(mList.get(i).max+"℃ / "+mList.get(i).min+"℃");
        return view;
    }

    private String parseDate(int position){
        String date="";
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat format2 = new SimpleDateFormat("MM月dd日");
        String[] weekOfDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        try {
            Date date1=format1.parse(mList.get(position).date);
            date=format2.format(date1);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//            if (w<0){
//                w=0;
//            }
            if (position==0){
                date+="今天";
            }else {
                date+=weekOfDays[w];
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}