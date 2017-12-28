package com.idx.smartspeakdock.Swipe;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.utils.ToastUtils;
import com.idx.smartspeakdock.weather.model.weather.Forecast;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenter;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenterImpl;
import com.idx.smartspeakdock.weather.ui.ChooseCityDialogFragment;
import com.idx.smartspeakdock.weather.ui.WeatherUi;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class SwipeFragment extends BaseFragment implements WeatherUi,ChooseCityDialogFragment.OnChooseCityCompleted {
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

    public WeatherPresenter mWeatherPresenter;
    private Resources mResources;
    private Context mContext;

    public static SwipeFragment newInstance(){return new SwipeFragment();}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mResources = context.getResources();
        mContext = context;
        Logger.setEnable(true);
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
        mWeatherPresenter = new WeatherPresenterImpl(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(TAG,"onCreateView:");
        mWeatherView = inflater.inflate(R.layout.activity_weather,container,false);
        mWeatherSelectCity = mWeatherView.findViewById(R.id.weather_choose_city);
        mWeatherSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(getActivity() instanceof OnSelectCityListener){
//                    ((OnSelectCityListener)getActivity()).onSelectCity(mWeatherView);
//                }
                ChooseCityDialogFragment cityDialogFragment=new ChooseCityDialogFragment();
                cityDialogFragment.setOnChooseCityCompleted(SwipeFragment.this);
                cityDialogFragment.show(getActivity().getFragmentManager(),"ChooseCityDialog");
            }
        });
        initView();
        if(NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)){
            requestLocation();
        }else{
            ToastUtils.showMessage(mContext,mResources.getString(R.string.network_not_connected));
        }
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
        loadingDialog = new ProgressDialog(mContext);
        loadingDialog.setTitle(mResources.getString(R.string.weather_loading_dialog));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Logger.info("ryan", "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        if(getActivity() instanceof OnSelectCityListener){
            ((OnSelectCityListener)getActivity()).OnInitView(mWeatherView);
        }
        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetStatusUtils.isMobileConnected(mContext) || NetStatusUtils.isWifiConnected(mContext)){
                    mWeatherPresenter.getWeather(mCurrentCity);
                    mWeatherPresenter.getWeatherAqi(mCurrentCity);
                }else{
                    mRefreshWeather.setRefreshing(false);
                    ToastUtils.showMessage(mContext,mResources.getString(R.string.network_not_connected));
                }
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

    public void loading(){loadingDialog.show();}

    public void compelete(){loadingDialog.dismiss();}

    @Override
    public void showLoading() {loading();}

    @Override
    public void hideLoading() {compelete();}

    @Override
    public void showError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefreshWeather.setRefreshing(false);
                ToastUtils.showError(mContext,mResources.getString(R.string.get_weather_info_error));
            }
        });
    }

    @Override
    public void setWeatherInfo(final Weather weather) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWeatherINfo(weather);
                mRefreshWeather.setRefreshing(false);
                mTitle.setText(weather.basic.cityName);
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
            View view = LayoutInflater.from(mContext).inflate(R.layout.activity_weather_daily_forecast_item, mForecastLayout, false);
            String date = HandlerWeatherUtil.parseDate(forecast.date);
            ((TextView) view.findViewById(R.id.weather_daily_forecast_item_date)).setText(date);
            ((ImageView) view.findViewById(R.id.weather_daily_forecast_item_icon)).setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(forecast.code)));
            ((TextView) view.findViewById(R.id.weather_daily_forecast_item_max_min)).setText(forecast.max + "℃ / " + forecast.min + "℃");
            mForecastLayout.addView(view);
        }
    }


    @Override
    public void setWeatherAqi(final Weather weather) {
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
    public void chooseCityCompleted(String countyName, String cityNime) {
        if(NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
            mWeatherPresenter.getWeather(cityNime);
            Log.d(TAG, cityNime);
            mCurrentCounty = countyName;
            mCurrentCity = cityNime;
            if (cityNime.equals("东城") || cityNime.equals("西城")) {
                cityNime = "北京";
                mCurrentCity = "北京";
            } else if (cityNime.equals("黄浦") || cityNime.equals("长宁") ||
                    cityNime.equals("静安") || cityNime.equals("普陀") ||
                    cityNime.equals("虹口") || cityNime.equals("杨浦")) {
                cityNime = "上海";
                mCurrentCity = "上海";
            } else if (cityNime.equals("和平") || cityNime.equals("河东") ||
                    cityNime.equals("河西") || cityNime.equals("南开") ||
                    cityNime.equals("河北") || cityNime.equals("红桥")) {
                cityNime = "天津";
                mCurrentCity = "天津";
            } else if (cityNime.equals("渝中") || cityNime.equals("大渡口") ||
                    cityNime.equals("江北") || cityNime.equals("沙坪坝") ||
                    cityNime.equals("九龙坡") || cityNime.equals("南岸") || cityNime.equals("开州")) {
                cityNime = "重庆";
                mCurrentCity = "重庆";
            }
            Log.d(TAG, "chooseCityCompleted: " + cityNime);
            if (!(cityNime.equals("香港") || cityNime.equals("澳门") || cityNime.equals("台北") || cityNime.equals("高雄") || cityNime.equals("台中"))) {
                mWeatherPresenter.getWeatherAqi(cityNime);
            }
            mTitle.setText(countyName);
        }else{
            ToastUtils.showError(mContext,getResources().getString(R.string.network_not_connected));
        }
    }

    private class StandbyLocationListener  implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mCurrentCity=bdLocation.getCity();
            mCurrentCounty=bdLocation.getCountry();
            Logger.info(TAG, "onReceiveLocation: mCurrentCity = "+mCurrentCity+",mCurrentCounty = "+mCurrentCounty);
            mWeatherPresenter.getWeather(mCurrentCity);
            mWeatherPresenter.getWeatherAqi(mCurrentCity);
        }
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Logger.info(TAG, "onDestroy: ");
        super.onDestroy();
        mLocationClient.stop();
    }
}
