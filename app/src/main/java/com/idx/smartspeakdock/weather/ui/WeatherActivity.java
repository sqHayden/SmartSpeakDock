package com.idx.smartspeakdock.weather.ui;

import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.standby.StandbyActivity;
import com.idx.smartspeakdock.utils.CityUtils;
import com.idx.smartspeakdock.weather.model.weather.Forecast;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenter;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenterImpl;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by danny on 12/13/17.
 */

public class WeatherActivity extends AppCompatActivity implements WeatherUi,ChooseCityDialogFragment.OnChooseCityCompleted{

    private static final String TAG = "WeatherActivity";
    private TextView mNowTemperature,mCond,mTemperature,
            mDate,mTitle,mLifestyleClothes,mLifestyleCar,
            mLifestyleAir,mAirQuality,mPM10,mPM25,mNO2,mSO2,mO3,mCO;
    private ImageView mWeatherNowIcon,mChooseCity;
    //private SwipeRefreshLayout mRefreshWeather;
    private ListView mListView;
    private WeatherPresenter mWeatherPresenter;
    private Dialog loadingDialog;
    private String city="深圳";

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
        mWeatherPresenter.getWeather(city);
        mWeatherPresenter.getWeatherAqi(city);
//        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
//        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mWeatherPresenter.getWeather("深圳");
//                mWeatherPresenter.getWeatherAqi("深圳");
//            }
//        });
    }

    /**
     * 初始化组件
     */
    private void initView(){
        //mRefreshWeather=findViewById(R.id.weather_swipe_refresh);
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
        mListView=findViewById(R.id.weather_daily_forecast_list);
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
        if (cityNime.equals("香港")||cityNime.equals("澳门")||cityNime.equals("台北")||cityNime.equals("高雄")||cityNime.equals("台中")){

        }else {
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
        Toast.makeText(getApplicationContext(), "获取信息失败！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 天气基本信息
     *
     * @param weather 天气
     */
    @Override
    public void setWeatherInfo(Weather weather) {
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
    }

    /**
     * 空气适量
     *
     * @param weather 天气
     */
    @Override
    public void setWeatherAqi(Weather weather) {
        mAirQuality.setText(weather.air.qlty);
        mPM10.setText(weather.air.pm10);
        mPM25.setText(weather.air.pm25);
        mNO2.setText(weather.air.no2);
        mSO2.setText(weather.air.so2);
        mO3.setText(weather.air.o3);
        mCO.setText(weather.air.co);
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

    /**
     * 解析日期，返回指定格式
     *
     * @param position 条目位置
     * @return ××月××日星期几
     */
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