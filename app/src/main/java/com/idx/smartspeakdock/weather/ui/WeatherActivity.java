package com.idx.smartspeakdock.weather.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.weather.model.weather.Forecast;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenter;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenterImpl;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

/**
 * Created by danny on 12/13/17.
 */

public class WeatherActivity extends AppCompatActivity implements WeatherUi,ChooseCityDialogFragment.OnChooseCityCompleted{

    private static final String TAG = "WeatherActivity";
    private TextView mNowTemperature,mCond,mTemperature,
            mDate,mTitle,mLifestyleClothes,mLifestyleCar,
            mLifestyleAir,mAirQuality,mPM10,mPM25,mNO2,mSO2,mO3,mCO;
    private ImageView mWeatherNowIcon,mChooseCity;
    public SwipeRefreshLayout mRefreshWeather;
    private ScrollView mScrollView;
    private LinearLayout mForecastLayout;
    private ListView mListView;
    private WeatherPresenter mWeatherPresenter;
    private Dialog loadingDialog;
    private String city="深圳";
    private String county="深圳";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        setContentView(R.layout.activity_weather);
        initView();
        mChooseCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCityDialogFragment cityDialogFragment=new ChooseCityDialogFragment();
                cityDialogFragment.setOnChooseCityCompleted(WeatherActivity.this);
                cityDialogFragment.show(getFragmentManager(),"ChooseCityDialog");
            }
        });

        Log.d(TAG, "onCreate: "+city);
        mWeatherPresenter.getWeather(county);
        mWeatherPresenter.getWeatherAqi(city);
        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWeatherPresenter.getWeather(county);
                mWeatherPresenter.getWeatherAqi(city);
            }
        });
    }

    /**
     * 初始化组件
     */
    private void initView(){
        mRefreshWeather=findViewById(R.id.weather_swipe_refresh);
        mScrollView=findViewById(R.id.weather_layout);
        mWeatherNowIcon=findViewById(R.id.weather_now_icon);
        mChooseCity=findViewById(R.id.weather_choose_city);
        mNowTemperature=findViewById(R.id.weather_now_temperature);
        mCond=findViewById(R.id.weather_now_cond_txt_n);
        mTemperature=findViewById(R.id.weather_daily_forecast_tmp_max_min);
//        mDate=findViewById(R.id.weather_temperature_date);
        mTitle=findViewById(R.id.weather_title);
        mLifestyleClothes=findViewById(R.id.weather_lifestyle_clothes_text);
        mLifestyleCar=findViewById(R.id.weather_lifestyle_car_text);
        mLifestyleAir=findViewById(R.id.weather_lifestyle_air_text);
//        mListView=findViewById(R.id.weather_daily_forecast_list);
        mForecastLayout=findViewById(R.id.weather_daily_forecast_list);
        mAirQuality=findViewById(R.id.weather_air_now_city);
        mPM10=findViewById(R.id.weather_air_pm10);
        mPM25=findViewById(R.id.weather_air_pm25);
        mNO2=findViewById(R.id.weather_air_no2);
        mSO2=findViewById(R.id.weather_air_so2);
        mO3=findViewById(R.id.weather_air_o3);
        mCO=findViewById(R.id.weather_air_co);
        mWeatherPresenter = new WeatherPresenterImpl(this); //传入WeatherView
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("加载天气中...");
    }

    /**
     * 选择城市回调：县名用于查天气基本信息，市名用于查询空气质量
     *
     * @param countyName 县名
     * @param cityNime 市名
     */
    @Override
    public void chooseCityCompleted(String countyName, String cityNime) {
        mWeatherPresenter.getWeather(countyName);
        Log.d(TAG,cityNime);
        county=countyName;
        city=cityNime;
        if (cityNime.equals("东城")||cityNime.equals("西城")){
            cityNime="北京";
            city="北京";
        }else if (cityNime.equals("黄浦")||cityNime.equals("长宁")||
                cityNime.equals("静安")||cityNime.equals("普陀")||
                cityNime.equals("虹口")||cityNime.equals("杨浦")){
            cityNime="上海";
            city="上海";
        }else if (cityNime.equals("和平")||cityNime.equals("河东")||
                cityNime.equals("河西")||cityNime.equals("南开")||
                cityNime.equals("河北")||cityNime.equals("红桥")){
            cityNime="天津";
            city="天津";
        }else if (cityNime.equals("渝中")||cityNime.equals("大渡口")||
                cityNime.equals("江北")||cityNime.equals("沙坪坝")||
                cityNime.equals("九龙坡")||cityNime.equals("南岸")||cityNime.equals("开州")){
            cityNime="重庆";
            city="重庆";
        }
        Log.d(TAG, "chooseCityCompleted: "+cityNime);
        if (!(cityNime.equals("香港")||cityNime.equals("澳门")||cityNime.equals("台北")||cityNime.equals("高雄")||cityNime.equals("台中"))){
            mWeatherPresenter.getWeatherAqi(cityNime);
        }
        mTitle.setText(countyName);
    }

    /**
     * 显示进度对话框
     */
    @Override
    public void showLoading() {
        loadingDialog.show();
    }

    /**
     * 隐藏进度对话框
     */
    @Override
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    /**
     * 天气信息获取失败
     */
    @Override
    public void showError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefreshWeather.setRefreshing(false);
                Toast.makeText(getApplicationContext(), "获取信息失败！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 天气基本信息
     *
     * @param weather 天气
     */
    @Override
    public void setWeatherInfo(final Weather weather) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWeatherINfo(weather);
                mRefreshWeather.setRefreshing(false);
            }
        });
    }

    private void updateWeatherINfo(Weather weather) {
        mWeatherNowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
        mNowTemperature.setText(weather.now.tmperature+"℃");
        mCond.setText(HandlerWeatherUtil.getWeatherType(Integer.parseInt(weather.now.code)));
        mTemperature.setText(weather.forecastList.get(0).max+"℃ / "+weather.forecastList.get(0).min+"℃");
        mLifestyleClothes.setText(weather.lifestyleList.get(1).brf);
        mLifestyleCar.setText(weather.lifestyleList.get(6).brf);
        mLifestyleAir.setText(weather.lifestyleList.get(7).brf);
//                    mDate.setText(new SimpleDateFormat("HH:mm").format(new Date()));
        mTitle.setText(weather.basic.cityName);
        mForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.activity_weather_daily_forecast_item, mForecastLayout, false);
            String date=HandlerWeatherUtil.parseDate(forecast.date);
            ((TextView)view.findViewById(R.id.weather_daily_forecast_item_date)).setText(date);
            ((ImageView)view.findViewById(R.id.weather_daily_forecast_item_icon)).setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(forecast.code)));
            ((TextView)view.findViewById(R.id.weather_daily_forecast_item_max_min)).setText(forecast.max+"℃ / "+forecast.min+"℃");
            mForecastLayout.addView(view);
        }
    }

    /**
     * 空气质量
     *
     * @param weather 天气
     */
    @Override
    public void setWeatherAqi(final Weather weather) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWeatherAqi(weather);
                mRefreshWeather.setRefreshing(false);
            }
        });
    }

    private void updateWeatherAqi(Weather weather) {
        mAirQuality.setText(weather.air.qlty);
        mPM10.setText(weather.air.pm10);
        mPM25.setText(weather.air.pm25);
        mNO2.setText(weather.air.no2);
        mSO2.setText(weather.air.so2);
        mO3.setText(weather.air.o3);
        mCO.setText(weather.air.co);
        mScrollView.setVisibility(View.VISIBLE);
    }
}