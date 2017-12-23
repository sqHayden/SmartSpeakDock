package com.idx.smartspeakdock.Swipe;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.weather.model.weather.Forecast;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;
import com.idx.smartspeakdock.weather.utils.WeatherUtil;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class SwipeFragment extends Fragment implements OnWeatherListener {
    private static final String TAG = SwipeFragment.class.getSimpleName();
    ImageView mWeatherSelectCity;
    View mWeatherView;
    ImageView mWeatherNowIcon;
    private LocationClient mLocationClient;
    private Dialog loadingDialog;
    private TextView mNowTemperature,mCond,mTemperature,
            mDate,mTitle,mLifestyleClothes,mLifestyleCar,
            mLifestyleAir,mAirQuality,mPM10,mPM25,mNO2,mSO2,mO3,mCO;
    private ScrollView mScrollView;
    private LinearLayout mForecastLayout;
    public SwipeRefreshLayout mRefreshWeather;
    private String mCurrentCity = "深圳";
    private String mCurrentCounty = "深圳";

    public static SwipeFragment newInstance(){return new SwipeFragment();}

    @Override
    public void onAttach(Context context) {
        Log.i("ryan", "onAttach: ");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getActivity().getApplicationContext());
        mLocationClient.registerLocationListener(new StandbyLocationListener());
        if (savedInstanceState != null) {
            mCurrentCity = savedInstanceState.getString("city");
            mCurrentCounty = savedInstanceState.getString("county");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("ryan", "onCreateView: ");
        mWeatherView = inflater.inflate(R.layout.activity_weather,container,false);
        mWeatherSelectCity = mWeatherView.findViewById(R.id.weather_choose_city);
        mWeatherSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() instanceof OnSelectCityListener){
                    ((OnSelectCityListener)getActivity()).onSelectCity(mWeatherView);
                }
            }
        });
        initView();
        requestLocation();
        return mWeatherView;
    }

    private void initView() {
        mRefreshWeather = mWeatherView.findViewById(R.id.weather_swipe_refresh);
        mForecastLayout = mWeatherView.findViewById(R.id.weather_daily_forecast_list);
        mScrollView = mWeatherView.findViewById(R.id.weather_layout);
        mForecastLayout = mWeatherView.findViewById(R.id.weather_daily_forecast_list);
        mWeatherNowIcon = mWeatherView.findViewById(R.id.weather_now_icon);
        mNowTemperature = mWeatherView.findViewById(R.id.weather_now_temperature);
        mCond = mWeatherView.findViewById(R.id.weather_now_cond_txt_n);
        mTemperature = mWeatherView.findViewById(R.id.weather_daily_forecast_tmp_max_min);
//        mDate=findViewById(R.id.weather_temperature_date);
        mTitle = mWeatherView.findViewById(R.id.weather_title);
        mLifestyleClothes = mWeatherView.findViewById(R.id.weather_lifestyle_clothes_text);
        mLifestyleCar = mWeatherView.findViewById(R.id.weather_lifestyle_car_text);
        mLifestyleAir = mWeatherView.findViewById(R.id.weather_lifestyle_air_text);
        mAirQuality = mWeatherView.findViewById(R.id.weather_air_now_city);
        mPM10 = mWeatherView.findViewById(R.id.weather_air_pm10);
        mPM25 = mWeatherView.findViewById(R.id.weather_air_pm25);
        mNO2 = mWeatherView.findViewById(R.id.weather_air_no2);
        mSO2 = mWeatherView.findViewById(R.id.weather_air_so2);
        mO3 = mWeatherView.findViewById(R.id.weather_air_o3);
        mCO = mWeatherView.findViewById(R.id.weather_air_co);
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setTitle("加载天气中...");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i("ryan", "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        if(getActivity() instanceof OnSelectCityListener){
            ((OnSelectCityListener)getActivity()).OnInitView(mWeatherView);
        }
        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WeatherUtil.loadWeather(mCurrentCounty,SwipeFragment.this);
                WeatherUtil.loadWeatherAqi(mCurrentCity,SwipeFragment.this);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("city", mCurrentCity);
        outState.putString("county", mCurrentCounty);
    }

    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }


    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(10 * 60 * 1000);
//        option.setScanSpan(0);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onSuccess(final Weather weather) {
        compelete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWeatherINfo(weather);
                mRefreshWeather.setRefreshing(false);
                mTitle.setText(mCurrentCounty);
            }
        });
    }

    public void updateWeatherINfo(Weather weather) {
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
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_weather_daily_forecast_item, mForecastLayout, false);
            String date = HandlerWeatherUtil.parseDate(forecast.date);
            ((TextView) view.findViewById(R.id.weather_daily_forecast_item_date)).setText(date);
            ((ImageView) view.findViewById(R.id.weather_daily_forecast_item_icon)).setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(forecast.code)));
            ((TextView) view.findViewById(R.id.weather_daily_forecast_item_max_min)).setText(forecast.max + "℃ / " + forecast.min + "℃");
            mForecastLayout.addView(view);
        }
    }

    @Override
    public void onSuccessAqi(final Weather weather) {
        compelete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWeatherAqi(weather);
                mRefreshWeather.setRefreshing(false);
            }
        });
    }

    public void updateWeatherAqi(Weather weather) {
        mAirQuality.setText(weather.air.qlty);
        mPM10.setText(weather.air.pm10);
        mPM25.setText(weather.air.pm25);
        mNO2.setText(weather.air.no2);
        mSO2.setText(weather.air.so2);
        mO3.setText(weather.air.o3);
        mCO.setText(weather.air.co);
        mScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {
        compelete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefreshWeather.setRefreshing(false);
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.get_weather_info_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loading(){loadingDialog.show();}

    public void compelete(){loadingDialog.dismiss();}

    private class StandbyLocationListener  implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d(TAG, "onReceiveLocation: "+ bdLocation.getCity());
            loading();
           /* WeatherUtil.loadWeather(bdLocation.getCity(),SwipeFragment.this);
            WeatherUtil.loadWeatherAqi(bdLocation.getCity(),SwipeFragment.this);*/
//            mCurrentCity=bdLocation.getCity();
//            mCurrentCounty=bdLocation.getCountry();
            Log.d(TAG, "onReceiveLocation: "+mCurrentCity+":"+mCurrentCounty);
            WeatherUtil.loadWeather(mCurrentCounty,SwipeFragment.this);
            WeatherUtil.loadWeatherAqi(mCurrentCity,SwipeFragment.this);

        }
    }


    @Override
    public void onDestroy() {
        Log.i("ryan", "onDestroy: ");
        super.onDestroy();
        mLocationClient.stop();
    }
}
