package com.idx.smartspeakdock.weather.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IWeatherVoiceListener;
import com.idx.smartspeakdock.service.SplachService;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.utils.ToastUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqi;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiRepository;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.ReturnWeather;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenter;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenterImpl;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;
import com.idx.smartspeakdock.weather.utils.WeatherUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class WeatherFragment extends BaseFragment implements WeatherUi, ChooseCityDialogFragment.OnChooseCityCompleted {
    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final int VOICE=0;
    private static final int UNVOICE=1;
    private static final int ONWEATHERINFO=3;
    private static final int ONRANGETEMPINFO=4;
    public static final int ONAIRQUALITYINFO=5;
    public static final int ONCURRENTTEMPINFO=6;
    public static final int ONWEATHERSTATUS=7;
    public static final int ONDRESSINFO=8;
    public static final int ONUITRAVIOLETLEVELINFO=9;
    public static final int ONSMOGINFO=10;
    private int mCurrentStatus;
    public SwipeRefreshLayout mRefreshWeather;
    public WeatherPresenter mWeatherPresenter;
    ImageView mWeatherSelectCity;
    View mWeatherView;
    ImageView mWeatherNowIcon, mTomorrowIcon, mAfterIcon;
    private LocationClient mLocationClient;
    private Dialog loadingDialog;
    private TextView mCond, mTemperature,
            mDate, mTime, mTitle, mTomorrow, mTomorrowTem, mAfter, mAfterTem, mLifestyleClothes, mLifestyleCar,
            mLifestyleAir, mPM25, mNO2, mCO;
    private LinearLayout mForecastLayout;
    private String mCurrentCity = "";
    private String mCurrentCounty = "";
    private Resources mResources;
    private Context mContext;
    private SwipeActivity.MyOnTouchListener onTouchListener;

    private String voice_answer;
    private Weather voice_weather;
    private Weather voice_aqi;

    private WeatherBasicRepository mWeatherBasicRepository;
    private WeatherAqiRepository mWeatherAqiRepository;

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }

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
            Log.d(TAG, "onCreate: "+mCurrentCounty+":"+mCurrentCity);
        }
        mWeatherPresenter = new WeatherPresenterImpl(this);
        if (mCurrentCity.isEmpty()){
            if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
                requestLocation();
            } else {
                ToastUtils.showMessage(mContext, mResources.getString(R.string.network_not_connected));
            }
        }else {
            if (!TextUtils.isEmpty(mCurrentCounty)){
//                getWeatherBasic(UNVOICE,mCurrentCounty);
            }else {
//                getWeatherBasic(UNVOICE, mCurrentCity);
            }
            getWeatherAqi(UNVOICE,mCurrentCity);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(TAG, "onCreateView:");
            mWeatherView = inflater.inflate(R.layout.activity_weather, container, false);
        initView();
        onTouchListener = new SwipeActivity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        Log.d(TAG, "onTouch: down");
                        mContext.stopService(new Intent(mContext.getApplicationContext(), SplachService.class));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: move");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: up");
                        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
                        break;
                }
                return false;
            }
        };

        ((SwipeActivity) getActivity()).registerMyOnTouchListener(onTouchListener);

        return mWeatherView;
    }

    private void initView() {
        mWeatherSelectCity = mWeatherView.findViewById(R.id.weather_choose_city);
        mRefreshWeather = mWeatherView.findViewById(R.id.weather_swipe_refresh);
        mForecastLayout = mWeatherView.findViewById(R.id.weather_daily_forecast_list);
        mWeatherNowIcon = mWeatherView.findViewById(R.id.weather_now_icon);
        mCond = mWeatherView.findViewById(R.id.weather_now_cond_txt_n);
        mTemperature = mWeatherView.findViewById(R.id.weather_temp);
        mDate=mWeatherView.findViewById(R.id.weather_date);
        mTime=mWeatherView.findViewById(R.id.weather_time);
        mTitle = mWeatherView.findViewById(R.id.weather_city);
//        mTomorrow=mWeatherView.findViewById(R.id.weather_forecast_tomorrow);
//        mTomorrowTem=mWeatherView.findViewById(R.id.weather_forecast_tomorrow_tem);
//        mTomorrowIcon=mWeatherView.findViewById(R.id.weather_forecast_tomorrow_icon);
//        mAfter=mWeatherView.findViewById(R.id.weather_forecast_after);
//        mAfterTem=mWeatherView.findViewById(R.id.weather_forecast_after_tem);
//        mAfterIcon=mWeatherView.findViewById(R.id.weather_forecast_after_icon);

        mLifestyleClothes = mWeatherView.findViewById(R.id.weather_lifestyle_clothes_text);
        mLifestyleCar = mWeatherView.findViewById(R.id.weather_lifestyle_car_text);
        mLifestyleAir = mWeatherView.findViewById(R.id.weather_lifestyle_air_text);
        mPM25 = mWeatherView.findViewById(R.id.weather_air_pm25);
        mNO2 = mWeatherView.findViewById(R.id.weather_air_no2);
        mCO = mWeatherView.findViewById(R.id.weather_air_co);
        loadingDialog = new ProgressDialog(mContext);
        loadingDialog.setTitle(mResources.getString(R.string.weather_loading_dialog));

        mWeatherSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(getActivity() instanceof OnSelectCityListener){
//                    ((OnSelectCityListener)getActivity()).onSelectCity(mWeatherView);
//                }
                ChooseCityDialogFragment cityDialogFragment = new ChooseCityDialogFragment();
                cityDialogFragment.setOnChooseCityCompleted(WeatherFragment.this);
                cityDialogFragment.show(getActivity().getFragmentManager(), "ChooseCityDialog");
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Logger.info("ryan", "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        /*if(getActivity() instanceof OnSelectCityListener){
            ((OnSelectCityListener)getActivity()).OnInitView(mWeatherView);
        }*/
        refresh();
        voiceResult();
        get();
    }

    private void voiceResult() {
        voice_answer = "";
//        voice_weather = null;
        UnitManager.getInstance(getContext()).setWeatherVoiceListener(new IWeatherVoiceListener() {
            @Override
            public void onWeatherInfo(String cityName) {
                Log.i(TAG, "onWeatherInfo: cityName = " + cityName);
                mWeatherPresenter.getWeather(cityName);
                mWeatherPresenter.getWeatherAqi(cityName);
            }

            @Override
            public void onRangeTempInfo(String cityName, String time,ReturnVoice returnVoice) {
                Log.i(TAG, "onRangeTempInfo: cityName = "+cityName+",time = "+time);
                voice_answer = "";
//                getWeatherBasic(VOICE,cityName);
                Log.d(TAG, "onRangeTempInfo: 温度信息"+voice_weather.status);
                if (voice_weather != null && voice_weather.status.equals("ok")){
                    Log.i(TAG, "onRangeTempInfo: city = "+voice_weather);
                    judgeRangeTempInfo(cityName,time);
                } else {
                    voice_answer = "查询"+time+cityName+"温度信息失败";
                }
            }

            @Override
            public void onAirQualityInfo(String cityName,ReturnVoice returnVoice) {
                Log.i(TAG, "onAirQualityInfo: cityName = " + cityName);
                getWeatherAqi(VOICE,cityName);
                Log.d(TAG, "onAirQualityInfo: 空气质量"+voice_aqi.status);
                if (voice_aqi != null && voice_aqi.status.equals("ok")) {
                    voice_answer = cityName+"空气质量为" + voice_aqi.air.qlty;
                } else {
                    voice_answer = "查询"+cityName+"空气质量信息失败";
                }
            }

            @Override
            public void onCurrentTempInfo(String cityName,ReturnVoice returnVoice) {
                Log.i(TAG, "onCurrentTempInfo: cityName = " + cityName);
                getWeatherBasic(VOICE,cityName,"",returnVoice,"onCurrentTempInfo");
            }

            @Override
            public void onWeatherStatus(String cityName, String time,ReturnVoice returnVoice) {
                Log.i(TAG, "onWeatherStatus: cityName = "+cityName+",time = "+time);
//                getWeatherBasic(VOICE,cityName);
                Log.d(TAG, "onWeatherStatus: 天气状况"+voice_weather.status);
                if (voice_weather != null && voice_weather.status.equals("ok")){
                    judgeStatusInfo(cityName,time);
                } else {
                    voice_answer = "查询" + time + cityName + "天气状况失败";
                }
            }

            @Override
            public void onRainInfo(String cityName, String time,ReturnVoice returnVoice) {

            }

            @Override
            public void onDressInfo(String cityName,ReturnVoice returnVoice) {
                Log.i(TAG, "onDressInfo: cityName = "+cityName);
//                getWeatherBasic(VOICE,cityName);
                Log.d(TAG, "onDressInfo: 穿衣指数"+voice_weather.status);
                if (voice_weather != null && voice_weather.status.equals("ok")){
                    voice_answer = voice_weather.lifestyleList.get(1).txt;
                } else {
                    voice_answer = "查询穿衣指数失败";
                }
            }

            @Override
            public void onUitravioletLevelInfo(String cityName,ReturnVoice returnVoice) {
                Log.i(TAG, "onUitravioletLevelInfo: cityName = "+cityName);
//                getWeatherBasic(VOICE,cityName);
                Log.d(TAG, "onUitravioletLevelInfo: 紫外线强度"+voice_weather.status);
                if (voice_weather != null && voice_weather.status.equals("ok")){
                    voice_answer = cityName + "紫外线强度" + voice_weather.lifestyleList.get(5).brf;
                } else {
                    voice_answer = "查询"+cityName+"紫外线强度失败";
                }
            }

            @Override
            public void onSmogInfo(String cityName,String time,ReturnVoice returnVoice) {
                Log.i(TAG, "onSmogInfo: cityName = "+cityName);
//                getWeatherBasic(VOICE,cityName);
                Log.d(TAG, "onSmogInfo: 雾霾"+voice_weather.status);

                if (voice_weather != null && voice_weather.status.equals("ok")) {
                    String weather_type = HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code));
                    if (weather_type.equals("雾")) {
                        voice_answer = time+cityName+"有雾霾";
                    } else {
                        voice_answer = time+cityName+"没有雾霾";
                    }
                } else {
                    voice_answer = "查询"+cityName+"雾霾信息失败";
                }
            }
        });
    }

    private void judgeStatusInfo(String cityName,String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                voice_answer = cityName+time+HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(0).code)) + "天";
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                voice_answer = cityName+time+HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)) + "天";
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName+time+HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)) + "天";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    private void judgeRangeTempInfo(String cityName,String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                voice_answer = cityName+time+"最高温度为" + voice_weather.forecastList.get(0).max + "度,最低温度为" + voice_weather.forecastList.get(0).min + "度";
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                voice_answer = cityName+time+"最高温度为" + voice_weather.forecastList.get(1).max + "度,最低温度为" + voice_weather.forecastList.get(1).min + "度";
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName+time+"最高温度为" + voice_weather.forecastList.get(2).max + "度,最低温度为" + voice_weather.forecastList.get(2).min + "度";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    private void refresh() {
        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetStatusUtils.isMobileConnected(mContext) || NetStatusUtils.isWifiConnected(mContext)) {
//                    mWeatherPresenter.getWeather(mCurrentCity);
//                    mWeatherPresenter.getWeatherAqi(mCurrentCity);
//                    getWeatherBasic(UNVOICE,mCurrentCity);
                    getWeatherAqi(UNVOICE,mCurrentCity);
                } else {
                    mRefreshWeather.setRefreshing(false);
                    ToastUtils.showMessage(mContext, mResources.getString(R.string.network_not_connected));
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

    public void requestLocation() {
        initLocation();
        mLocationClient.start();
    }


    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(10 * 60 * 1000);
//        option.setScanSpan(0);
        mLocationClient.setLocOption(option);
    }

    public void loading() {
        loadingDialog.show();
    }

    public void compelete() {
        loadingDialog.dismiss();
    }

    @Override
    public void showLoading() {
        loading();
    }

    @Override
    public void hideLoading() {
        compelete();
    }

    @Override
    public void showError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefreshWeather.setRefreshing(false);
                ToastUtils.showError(mContext, mResources.getString(R.string.get_weather_info_error));
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
        mCond.setText(mResources.getString(R.string.weather)+":"+HandlerWeatherUtil.getWeatherType(Integer.parseInt(weather.now.code)));
        mTemperature.setText(weather.forecastList.get(0).max + "℃ / " + weather.forecastList.get(0).min + "℃");
        mLifestyleClothes.setText(weather.lifestyleList.get(1).brf);
        mLifestyleCar.setText(weather.lifestyleList.get(6).brf);
        mLifestyleAir.setText(weather.lifestyleList.get(7).brf);
        mDate.setText(new SimpleDateFormat("EEEE, MM dd").format(new Date()));
        mTime.setText(new SimpleDateFormat("aa hh:mm").format(new Date()));
        mTitle.setText(weather.basic.cityName);

//        String tomorrow = HandlerWeatherUtil.parseDate(weather.forecastList.get(1).date);
//        mTomorrow.setText(mResources.getString(R.string.weather_tomorrow)+tomorrow);
//        mTomorrowTem.setText(weather.forecastList.get(1).max+"℃ / "+weather.forecastList.get(1).min+"℃");
//        mAfterIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(1).code)));
//
//        String after = HandlerWeatherUtil.parseDate(weather.forecastList.get(2).date);
//        mAfter.setText(mResources.getString(R.string.weather_after)+after);
//        mAfterTem.setText(weather.forecastList.get(2).max+"℃ / "+weather.forecastList.get(2).min+"℃");
//        mAfterIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(1).code)));

        mForecastLayout.removeAllViews();

        for (int i=1;i<weather.forecastList.size();i++){
            View view = LayoutInflater.from(mContext).inflate(R.layout.activity_weather_daily_forecast_item, mForecastLayout, false);
            String date = HandlerWeatherUtil.parseDate(weather.forecastList.get(i).date);
            ((TextView) view.findViewById(R.id.weather_daily_forecast_item_date)).setText(date);
            ((ImageView) view.findViewById(R.id.weather_daily_forecast_item_icon)).setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(i).code)));
            ((TextView) view.findViewById(R.id.weather_daily_forecast_item_max_min)).setText(weather.forecastList.get(i).max + "℃ / " + weather.forecastList.get(i).min + "℃");
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
        mPM25.setText(weather.air.pm25);
        mNO2.setText(weather.air.no2);
        mCO.setText(weather.air.co);
    }

    @Override
    public void chooseCityCompleted(String countyName, String cityName) {
        Log.d(TAG, "chooseCityCompleted: "+cityName+":"+countyName);
        mCurrentCounty = countyName;
        mCurrentCity = cityName;
        if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
            mWeatherPresenter.getWeather(countyName);
            Log.d(TAG, cityName);
            Log.d(TAG, "chooseCityCompleted: " + cityName);
            if (!(cityName.equals("香港") || cityName.equals("澳门") || cityName.equals("台北") || cityName.equals("高雄") || cityName.equals("台中"))) {
                mWeatherPresenter.getWeatherAqi(cityName);
            }
            mTitle.setText(countyName);
        } else {
            ToastUtils.showError(mContext, getResources().getString(R.string.network_not_connected));
        }
    }

    @Override
    public void onDestroy() {
        Logger.info(TAG, "onDestroy: ");
        super.onDestroy();
        mLocationClient.stop();
        mContext.stopService(new Intent(mContext.getApplicationContext(), SplachService.class));
        ((SwipeActivity) getActivity()).unregisterMyOnTouchListener(onTouchListener);
    }
    @Override
    public void onResume() {
        super.onResume();
        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
        Log.d(TAG, "onResume: ");
    }

    private class StandbyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mCurrentCity = bdLocation.getCity();
            //mCurrentCounty = bdLocation.getCountry();
            Logger.info(TAG, "onReceiveLocation: mCurrentCity = " + mCurrentCity + ",mCurrentCounty = " + mCurrentCounty);
            getWeatherBasic(UNVOICE,bdLocation.getCity(),"",null,"");
            getWeatherAqi(UNVOICE,bdLocation.getCity());
        }
    }

    //优先加载本地天气数据
    private void getWeatherBasic(final int way, final String cityName,final String time,final ReturnVoice returnVoice,final String funcTag){
        mWeatherBasicRepository= WeatherBasicInjection.getNoteRepository(getActivity());
        mWeatherBasicRepository.getWeatherBasic(cityName+"%", new WeatherBasicDataSource.LoadWeatherBasicsCallback() {
            @Override
            public void onWeatherBasicsLoaded(WeatherBasic weatherBasic) {
                Log.d(TAG, "onWeatherBasicsLoaded: "+weatherBasic.toString());
                switch (way){
                    case UNVOICE:
                        Log.d(TAG, "onWeatherBasicsLoaded: 加载数据库天气数据");
                        String weatherBasicInfo = weatherBasic.weatherBasic;
                        voice_weather = Utility.handleWeatherResponse(weatherBasicInfo);
                        setWeatherInfo(voice_weather);
                        break;
                    case VOICE:
                        Log.d(TAG, "onWeatherBasicsLoaded: 语音加载数据库天气数据");
                        break;
                }

            }

            @Override
            public void onDataNotAvailable() {
                switch (way) {
                    case UNVOICE:
                        Log.d(TAG, "onDataNotAvailable: 加载网络天气数据");
                        mWeatherPresenter.getWeather(mCurrentCity);
                        break;
                    case VOICE:
                        Log.d(TAG, "onDataNotAvailable: 语音加载网络天气数据");
                        WeatherUtil.loadWeather(cityName, new ReturnWeather() {
                            @Override
                            public void onReturnWeather(Weather weather) {
                                voice_weather = weather;
                                if (returnVoice != null){
                                    switch (funcTag){
                                        case "onCurrentTempInfo":
                                            currentTempInfo(cityName,returnVoice);
                                            break;
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        });
    }

    //优先加载本地空气质量数据
    private void getWeatherAqi(final int way, final String cityName){
        mWeatherAqiRepository= WeatherAqiInjection.getInstance(getActivity());
        mWeatherAqiRepository.getWeatherAqi(cityName+"%", new WeatherAqiDataSource.LoadWeatherAqisCallback() {
            @Override
            public void onWeatherAqisLoaded(WeatherAqi weatherAqi) {
                Log.d(TAG, "onWeatherAqisLoaded: "+weatherAqi.toString());
                switch (way) {
                    case UNVOICE:
                        Log.d(TAG, "onWeatherAqisLoaded: 加载数据库空气质量信息");
                        String weatherAqiInfo = weatherAqi.weatherAqi;
                        voice_aqi=Utility.handleWeatherResponse(weatherAqiInfo);
                        setWeatherAqi(voice_aqi);
                        break;
                    case VOICE:
                        Log.d(TAG, "onWeatherAqisLoaded: 语音加载数据库空气质量信息");
                        break;
                }
            }

            @Override
            public void onDataNotAvailable() {
                switch (way) {
                    case UNVOICE:
                        Log.d(TAG, "onDataNotAvailable: 加载网络空气质量信息");
                        mWeatherPresenter.getWeatherAqi(cityName);
                        break;
                    case VOICE:
                        Log.d(TAG, "onDataNotAvailable: 语音加载网络空气质量信息");
                        WeatherUtil.loadWeatherAqi(cityName, new ReturnWeather() {
                            @Override
                            public void onReturnWeather(Weather weather) {
                                voice_aqi = weather;
                            }
                        });
                        break;
                }
            }
        });
    }

    private void get(){
        mWeatherBasicRepository= WeatherBasicInjection.getNoteRepository(getActivity());
        mWeatherBasicRepository.getWeatherBasics(new WeatherBasicDataSource.LoadWeatherBasicsListCallback() {
            @Override
            public void onWeatherBasicsLoaded(List<WeatherBasic> weatherBasic) {
                Log.d(TAG, "onWeatherBasicsLoaded: "+weatherBasic.size());
                for (WeatherBasic basic:weatherBasic) {
                    Log.d(TAG, "onWeatherBasicsLoaded: " +basic.weatherBasic);
                }
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    public void currentTempInfo(String cityName,ReturnVoice returnVoice){
        Log.d(TAG, "onCurrentTempInfo: 当前温度"+voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            Log.i(TAG, "onCurrentTempInfo: city = "+voice_weather.basic.cityName);
            voice_answer = cityName+"当前温度为" + voice_weather.now.tmperature + "度";
        } else {
            voice_answer = "查询" + cityName + "当前温度信息失败";
        }
        returnVoice.onReturnVoice(voice_answer);
    }
}
