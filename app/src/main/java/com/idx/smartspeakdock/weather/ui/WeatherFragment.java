package com.idx.smartspeakdock.weather.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

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
import com.idx.smartspeakdock.weather.event.ReturnVoiceEvent;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqi;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherAqiRepository;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;
import com.idx.smartspeakdock.weather.presenter.ReturnAnswerCallback;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.ReturnWeather;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenter;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenterImpl;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;
import com.idx.smartspeakdock.weather.utils.WeatherUtil;
import com.lljjcoder.Interface.OnCityItemClickListener;
import com.lljjcoder.bean.CityBean;
import com.lljjcoder.bean.DistrictBean;
import com.lljjcoder.bean.ProvinceBean;
import com.lljjcoder.citywheel.CityConfig;
import com.lljjcoder.style.citypickerview.CityPickerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class WeatherFragment extends BaseFragment implements WeatherUi, ChooseCityDialogFragment.OnChooseCityCompleted {
    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final int VOICE = 0;
    private static final int UNVOICE = 1;
    public SwipeRefreshLayout mRefreshWeather;
    public WeatherPresenter mWeatherPresenter;
    private ImageView mWeatherSelectCity;
    private View mWeatherView;
    private ImageView mWeatherNowIcon, mTomorrowIcon, mAfterIcon, mVTomorrowIcon, mVAfterIcon;
    private Dialog loadingDialog;
    private TextView mCond, mTemperature,
            mDate, mTime, mTitle, mTomorrow, mTomorrowTem, mAfter, mAfterTem, mLifestyleClothes, mLifestyleCar,
            mLifestyleAir, mPM25, mNO2, mCO, mVTomorrowDate, mVTomorrowTem, mVAfterDate, mVAfterTem, mTextAir;
//    private LinearLayout mForecastLayout;
    private LinearLayout mLayoutAir;
    public static String mCurrentCity = "深圳";
    public static String mCurrentCounty = "深圳";
    private Resources mResources;
    private Context mContext;
    private SwipeActivity.MyOnTouchListener onTouchListener;

    private String voice_answer;
    private Weather voice_weather;
    private Weather voice_aqi;

    private Timer mTimer;
    private TimerTask mTask;

    private WeatherBasicRepository mWeatherBasicRepository;
    private WeatherAqiRepository mWeatherAqiRepository;
    private ReturnAnswerCallback mReturnAnswerCallback;
    private String mWeather_city;
    private String mWeather_time;
    private String mWeather_fun_flag;
    private int mWeather_Voice_flag;
    private WeatherBroadcastReceiver mWeatherBroadcastReceiver;

//    private GetCityService.MyBinder mCityBinder;
//    private ServiceConnection mCityConn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mCityBinder = (GetCityService.MyBinder) service;
//            mCityBinder.getCity(new GetCityService.CallBack() {
//                @Override
//                public void call(BDLocation location) {
//                    mCurrentCity = location.getCity();
//                    getWeatherBasic(UNVOICE,mCurrentCity,"",null,"");
//                    getWeatherAqi(UNVOICE,mCurrentCity,"",null,"");
//                }
//            });
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

    //定时更新界面
    private void initTimer() {
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mCurrentCounty)) {
                    getWeatherBasic(UNVOICE,mCurrentCity,"",null,"");
                } else {
                    getWeatherBasic(UNVOICE,mCurrentCounty,"",null,"");
                }
                getWeatherAqi(UNVOICE,mCurrentCity,"",null,"");
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTask, 60 * 1000, 60 * 1000);
    }

    public WeatherFragment(){}

    public static WeatherFragment newInstance(String cityName,String time,String fun_flag,int flag){
        WeatherFragment weatherFragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString("cityname",cityName);
        args.putString("time",time);
        args.putString("fun_flag",fun_flag);
        args.putInt("voice_flag",flag);
        weatherFragment.setArguments(args);
        return weatherFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mResources = context.getResources();
        mContext = context;
        Logger.setEnable(true);
        mWeather_Voice_flag = -1;
        if (getArguments() != null){
            Bundle args = getArguments();
            mWeather_city = args.getString("cityname");
            mWeather_time = args.getString("time");
            mWeather_fun_flag = args.getString("fun_flag");
            mWeather_Voice_flag = args.getInt("voice_flag");
            Log.i("ryan", "onAttach: mWeather_voice_flag = "+mWeather_Voice_flag);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*BaseActivity baseActivity = (BaseActivity) getActivity();
        if (!BaseActivity.isServiceRunning(baseActivity.getApplicationContext(), "com.idx.smartspeakdock.start.GetCityService")) {
            Log.d("启动服务", "startService");
            Intent intent = new Intent(baseActivity.getApplicationContext(), GetCityService.class);
            //启动
            baseActivity.getApplicationContext().startService(intent);
            //绑定
            baseActivity.getApplicationContext().bindService(intent, mCityConn, BIND_AUTO_CREATE);
        }*/

        if (savedInstanceState != null) {
            mCurrentCity = savedInstanceState.getString("city");
            mCurrentCounty = savedInstanceState.getString("county");
            Log.d(TAG, "onCreate: " + mCurrentCounty + ":" + mCurrentCity);
        }
        mWeatherPresenter = new WeatherPresenterImpl(this);
        if (mWeather_Voice_flag == GlobalUtils.WEATHER_VOICE_FLAG){
            Log.i("ryan", "onCreate: voice_flag = "+mWeather_Voice_flag);
            judgeVoiceAnswer(mWeather_city,mWeather_time,mWeather_fun_flag);
        }else {
            Log.i("ryan", "onCreate: voice_flag_un = "+mWeather_Voice_flag);
            getWeatherBasic(UNVOICE,mCurrentCounty,"",null,"");
            getWeatherAqi(UNVOICE,mCurrentCity,"",null,"");
        }
        /*if (mCurrentCity.isEmpty()) {
            if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
                getWeatherBasic(UNVOICE,mCurrentCity,"",null,"");
                getWeatherAqi(UNVOICE,mCurrentCity,"",null,"");
            } else {
                ToastUtils.showMessage(mContext, mResources.getString(R.string.network_not_connected));
            }
        } else {
            if (!TextUtils.isEmpty(mCurrentCounty)) {
                getWeatherBasic(UNVOICE,mCurrentCounty,"",null,"");
            } else {
                getWeatherBasic(UNVOICE,mCurrentCity,"",null,"");
            }
            getWeatherAqi(UNVOICE,mCurrentCity,"",null,"");
        }*/
        initTimer();
        //注册天气语音广播
        registerWeatherVoiceBroadcast();
    }

    private void registerWeatherVoiceBroadcast() {
        mWeatherBroadcastReceiver = new WeatherBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalUtils.WEATHER_BROADCAST_ACTION);
        mContext.registerReceiver(mWeatherBroadcastReceiver,intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(TAG, "onCreateView:");
        //判断当前屏幕方向
        if (2 == getActivity().getResources().getConfiguration().orientation) {//横屏
            mWeatherView = inflater.inflate(R.layout.fragment_weather_land, container, false);
            mTomorrow = mWeatherView.findViewById(R.id.weather_forecast_tomorrow);
            mTomorrowTem = mWeatherView.findViewById(R.id.weather_forecast_tomorrow_tem);
            mTomorrowIcon = mWeatherView.findViewById(R.id.weather_forecast_tomorrow_icon);
            mAfter = mWeatherView.findViewById(R.id.weather_forecast_after);
            mAfterTem = mWeatherView.findViewById(R.id.weather_forecast_after_tem);
            mAfterIcon = mWeatherView.findViewById(R.id.weather_forecast_after_icon);
        } else {
            mWeatherView = inflater.inflate(R.layout.fragment_weather_vertical, container, false);
            //mForecastLayout = mWeatherView.findViewById(R.id.weather_forecast);
            mVTomorrowDate=mWeatherView.findViewById(R.id.weather_vertical_forecast_tomorrow_date);
            mVTomorrowIcon=mWeatherView.findViewById(R.id.weather_vertical_forecast_tomorrow_icon);
            mVTomorrowTem=mWeatherView.findViewById(R.id.weather_vertical_forecast_tomorrow_tem);
            mVAfterDate=mWeatherView.findViewById(R.id.weather_vertical_forecast_after_date);
            mVAfterIcon=mWeatherView.findViewById(R.id.weather_vertical_forecast_after_icon);
            mVAfterTem=mWeatherView.findViewById(R.id.weather_vertical_forecast_after_tem);
        }

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
        mWeatherNowIcon = mWeatherView.findViewById(R.id.weather_now_icon);
        mCond = mWeatherView.findViewById(R.id.weather_now_cond_txt_n);
        mTemperature = mWeatherView.findViewById(R.id.weather_temp);
        mDate = mWeatherView.findViewById(R.id.weather_date);
        mTime = mWeatherView.findViewById(R.id.weather_time);
        mTitle = mWeatherView.findViewById(R.id.weather_city);
        mLifestyleClothes = mWeatherView.findViewById(R.id.weather_lifestyle_clothes_text);
        mLifestyleCar = mWeatherView.findViewById(R.id.weather_lifestyle_car_text);
        mLifestyleAir = mWeatherView.findViewById(R.id.weather_lifestyle_air_text);
        mPM25 = mWeatherView.findViewById(R.id.weather_air_pm25);
        mNO2 = mWeatherView.findViewById(R.id.weather_air_no2);
        mCO = mWeatherView.findViewById(R.id.weather_air_co);
        mTextAir=mWeatherView.findViewById(R.id.weather_title_3);
        mLayoutAir=mWeatherView.findViewById(R.id.weather_air);
        mTextAir.setVisibility(View.INVISIBLE);
        mLayoutAir.setVisibility(View.INVISIBLE);
        loadingDialog = new ProgressDialog(mContext);
        loadingDialog.setTitle(mResources.getString(R.string.weather_loading_dialog));
        mWeatherSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wheel();
                /*ChooseCityDialogFragment cityDialogFragment = new ChooseCityDialogFragment();
                cityDialogFragment.setOnChooseCityCompleted(WeatherFragment.this);
                cityDialogFragment.show(getActivity().getFragmentManager(), "ChooseCityDialog");*/
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Logger.info("ryan", "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    //语音赋值当前城市名
    private void voiceCityName(String cityName) {
        mCurrentCity = cityName;
        mCurrentCounty = cityName;
    }

    //刷新天气
    private void refresh() {
        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetStatusUtils.isMobileConnected(mContext) || NetStatusUtils.isWifiConnected(mContext)) {
                    mWeatherPresenter.getWeather(mCurrentCity);
                    mWeatherPresenter.getWeatherAqi(mCurrentCity);
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

    @Override
    public void showLoading() {
        loadingDialog.show();
    }

    @Override
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    @Override
    public void showError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefreshWeather.setRefreshing(false);
                ToastUtils.showError(mContext, mResources.getString(R.string.get_weather_info_error));
                mTextAir.setVisibility(View.INVISIBLE);
                mLayoutAir.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void setWeatherInfo(final Weather weather) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (weather==null) {
                    Log.d(TAG, "run: " + weather);
                }
                Log.i("ryan", "run: setWeatherInfo = "+weather.basic.cityName);
                updateWeatherInfo(weather);
                mRefreshWeather.setRefreshing(false);
                mTitle.setText(weather.basic.cityName);
            }
        });
    }

    public void updateWeatherInfo(Weather weather) {
        voice_weather = weather;
        Log.d(TAG, "updateWeatherInfo: " + voice_weather);
        mWeatherNowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
        mCond.setText(mResources.getString(R.string.weather) + ":" + HandlerWeatherUtil.getWeatherType(Integer.parseInt(weather.now.code)));
        mTemperature.setText(weather.forecastList.get(0).max + "℃ / " + weather.forecastList.get(0).min + "℃");
        mLifestyleClothes.setText(weather.lifestyleList.get(1).brf);
        mLifestyleCar.setText(weather.lifestyleList.get(6).brf);
        mLifestyleAir.setText(weather.lifestyleList.get(7).brf);
        mDate.setText(new SimpleDateFormat("EEEE MM dd").format(new Date()));
        mTime.setText(new SimpleDateFormat("aa hh:mm").format(new Date()));
        mTitle.setText(weather.basic.cityName);

        if (mVTomorrowDate == null) {
            String tomorrow = HandlerWeatherUtil.parseDateLand(weather.forecastList.get(1).date);
            mTomorrow.setText(mResources.getString(R.string.weather_tomorrow) + tomorrow);
            mTomorrowTem.setText(weather.forecastList.get(1).max + "℃ / " + weather.forecastList.get(1).min + "℃");
            mTomorrowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(1).code)));

            String after = HandlerWeatherUtil.parseDateLand(weather.forecastList.get(2).date);
            mAfter.setText(mResources.getString(R.string.weather_after) + after);
            mAfterTem.setText(weather.forecastList.get(2).max + "℃ / " + weather.forecastList.get(2).min + "℃");
            mAfterIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(2).code)));
        } else {
//            mForecastLayout.removeAllViews();

            String tomorrow = HandlerWeatherUtil.parseDate(weather.forecastList.get(1).date);
            mVTomorrowDate.setText(tomorrow);
            mVTomorrowTem.setText(weather.forecastList.get(1).max + "℃ / " + weather.forecastList.get(1).min + "℃");
            mVTomorrowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(1).code)));

            String after = HandlerWeatherUtil.parseDate(weather.forecastList.get(2).date);
            mVAfterDate.setText(after);
            mVAfterTem.setText(weather.forecastList.get(2).max + "℃ / " + weather.forecastList.get(2).min + "℃");
            mVAfterIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(2).code)));
        }
    }

    @Override
    public void setWeatherAqi(final Weather weather) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextAir.setVisibility(View.VISIBLE);
                mLayoutAir.setVisibility(View.VISIBLE);
                updateWeatherAqi(weather);
                mRefreshWeather.setRefreshing(false);
            }
        });
    }

    public void updateWeatherAqi(Weather weather) {
        voice_aqi = weather;
        Log.d(TAG, "updateWeatherAqi: voice" + voice_aqi);
        Log.d(TAG, "updateWeatherAqi: weather" + voice_aqi);
        mPM25.setText(weather.air.pm25);
        mNO2.setText(weather.air.no2);
        mCO.setText(weather.air.co);
    }

    @Override
    public void chooseCityCompleted(String countyName, String cityName) {
        Log.d(TAG, "chooseCityCompleted: " + cityName + ":" + countyName);
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
        mContext.stopService(new Intent(mContext.getApplicationContext(), SplachService.class));
        ((SwipeActivity) getActivity()).unregisterMyOnTouchListener(onTouchListener);
        //解绑广播注册
        mContext.unregisterReceiver(mWeatherBroadcastReceiver);
        mWeather_city = "";
        mWeather_time = "";
        mWeather_fun_flag = "";
        mWeather_Voice_flag = -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.startService(new Intent(mContext.getApplicationContext(), SplachService.class));
        Log.d(TAG, "onResume: ");
    }

    //优先加载本地天气数据
    private void getWeatherBasic(final int way, final String cityName, final String time, final ReturnVoice returnVoice, final String funcTag) {
        mWeatherBasicRepository = WeatherBasicInjection.getNoteRepository(getActivity());
        mWeatherBasicRepository.getWeatherBasic(cityName + "%", new WeatherBasicDataSource.LoadWeatherBasicsCallback() {
            @Override
            public void onWeatherBasicsLoaded(WeatherBasic weatherBasic) {
                Log.d(TAG, "onWeatherBasicsLoaded: 加载数据库天气数据");
                String weatherBasicInfo = weatherBasic.weatherBasic;
                voice_weather = Utility.handleWeatherResponse(weatherBasicInfo);
                setWeatherInfo(voice_weather);
//                voiceReturnJudge(cityName,time,returnVoice,funcTag);
                if(funcTag != "onWeatherInfo"){
                    voiceReturnJudge(cityName,time,funcTag);
                }
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "onDataNotAvailable: 加载网络天气数据");
                switch (way) {
                    case UNVOICE:
                        mWeatherPresenter.getWeather(cityName);
                        break;
                    case VOICE:
                        WeatherUtil.loadWeather(cityName, new ReturnWeather() {
                            @Override
                            public void onReturnWeather(Weather weather) {
                                voice_weather = weather;
                                if (mReturnAnswerCallback != null) {
                                    if (weather != null) {
                                        voice_weather = weather;
                                        Log.i("ryan", "onReturnWeather: voice_weather_city = "+voice_weather.basic.cityName);
                                        setWeatherInfo(weather);
                                        if(funcTag != "onWeatherInfo"){
                                            voiceReturnJudge(cityName,time,funcTag);
                                        }
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
    private void getWeatherAqi(final int way, final String cityName, final String time, final ReturnVoice returnVoice, final String funcTag) {
        mWeatherAqiRepository = WeatherAqiInjection.getInstance(getActivity());
        mWeatherAqiRepository.getWeatherAqi(cityName + "%", new WeatherAqiDataSource.LoadWeatherAqisCallback() {
            @Override
            public void onWeatherAqisLoaded(WeatherAqi weatherAqi) {
                Log.d(TAG, "onWeatherAqisLoaded: 加载数据库空气质量信息");
                String weatherAqiInfo = weatherAqi.weatherAqi;
                voice_aqi = Utility.handleWeatherResponse(weatherAqiInfo);
                setWeatherAqi(voice_aqi);
                if(funcTag != "onWeatherInfo"){
                    voiceReturnJudge(cityName,time,funcTag);
                }
            }

            @Override
            public void onDataNotAvailable() {
                switch (way) {
                    case UNVOICE:
                        mWeatherPresenter.getWeatherAqi(cityName);
                        break;
                    case VOICE:
                        WeatherUtil.loadWeatherAqi(cityName, new ReturnWeather() {
                            @Override
                            public void onReturnWeather(Weather weather) {
                                if (weather!=null) {
                                    voice_aqi = weather;
                                    setWeatherAqi(weather);
                                    if(funcTag != "onWeatherInfo"){
                                        voiceReturnJudge(cityName,time,funcTag);
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        });
    }

    //判断所问问题类型(温度、穿衣、紫外线等等)
    private void voiceReturnJudge(String cityName,String time,String funcTag){
        if (mReturnAnswerCallback!=null) {
            switch (funcTag) {
                case "onRangeTempInfo":
                    rangeTempInfo(cityName,time);
                    break;
                case "onAirQualityInfo":
                    airQualityInfo(cityName);
                    break;
                case "onCurrentTempInfo":
                    currentTempInfo(cityName);
                    break;
                case "onWeatherStatus":
                    weatherStatus(cityName,time);
                    break;
                case "onRainInfo":
                    rainInfo(cityName,time);
                    break;
                case "onDressInfo":
                    dressInfo();
                    break;
                case "onUitravioletLevelInfo":
                    uitravioletLevelInfo(cityName);
                    break;
                case "onSmogInfo":
                    smogInfo(cityName,time);
                    break;
                default:
                    break;
            }
        }
    }

    //温度是多少
    private void rangeTempInfo(String cityName,String time){
        Log.d("ryan", "rangeTempInfo: 温度信息" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            Log.i("ryan", "rangeTempInfo: city = " + voice_weather);
            judgeRangeTempInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "温度信息失败";
        }
        Log.i("ryan", "rangeTempInfo: voice_answer = "+voice_answer);
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //温度相关方法
    private void judgeRangeTempInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(0).max + "度,最低温度为" + voice_weather.forecastList.get(0).min + "度";
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(1).max + "度,最低温度为" + voice_weather.forecastList.get(1).min + "度";
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(2).max + "度,最低温度为" + voice_weather.forecastList.get(2).min + "度";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    //空气质量
    private void airQualityInfo(String cityName){
        Log.i(TAG, "airQualityInfo: cityName = " + cityName);
        if (voice_aqi != null && voice_aqi.status.equals("ok")) {
            voice_answer = cityName + "空气质量为" + voice_aqi.air.qlty;
        } else {
            voice_answer = "查询" + cityName + "空气质量信息失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //当前温度
    private void currentTempInfo(String cityName) {
        Log.d(TAG, "currentTempInfo: 当前温度" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            Log.i(TAG, "currentTempInfo: city = " + voice_weather.basic.cityName);
            voice_answer = cityName + "当前温度为" + voice_weather.now.tmperature + "度";
        } else {
            voice_answer = "查询" + cityName + "当前温度信息失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //天气状况
    private void weatherStatus(String cityName,String time){
        Log.d(TAG, "weatherStatus: 天气状况" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            judgeStatusInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "天气状况失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //天气状况相关方法
    private void judgeStatusInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code)) + "天";
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)) + "天";
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)) + "天";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    //是否有雨
    private void rainInfo(String cityName,String time){
        Log.d(TAG, "rainInfo: 下雨"+voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            judgeRainInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "是否下雨失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //雨相关方法
    private void judgeRainInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                rainResult(cityName, time);
                break;
            case GlobalUtils.WEATHER_TIME_TOMM:
                rainResult(cityName, time);
                break;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                rainResult(cityName, time);
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    //雨相关方法
    private void rainResult(String cityName, String time) {
        if (judgeRain(time)) {
            voice_answer = time + cityName + "有雨";
        } else {
            voice_answer = time + cityName + "没有雨";
        }
    }

    //雨相关方法
    private boolean judgeRain(String time) {
        switch (time) {
            case GlobalUtils.WEATHER_TIME_TODAY:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code)).equals("雨") ? true : false;
            case GlobalUtils.WEATHER_TIME_TOMM:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)).equals("雨") ? true : false;
            case GlobalUtils.WEATHER_TIME_POSTNATAL:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)).equals("雨") ? true : false;
            default:
                return false;
        }
    }

    //穿衣
    private void dressInfo(){
        Log.d(TAG, "dressInfo: 穿衣指数" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            voice_answer = voice_weather.lifestyleList.get(1).txt;
        } else {
            voice_answer = "查询穿衣指数失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //紫外线
    private void uitravioletLevelInfo(String cityName) {
        Log.d(TAG, "uitravioletLevelInfo: 紫外线强度" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            voice_answer = cityName + "紫外线强度" + voice_weather.lifestyleList.get(5).brf;
        } else {
            voice_answer = "查询" + cityName + "紫外线强度失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    //雾
    private void smogInfo(String cityName,String time){
        Log.d(TAG, "smogInfo: 雾霾" + voice_weather.status);
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            String weather_type = HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code));
            if (weather_type.equals("雾")) {
                voice_answer = time + cityName + "有雾霾";
            } else {
                voice_answer = time + cityName + "没有雾霾";
            }
        } else {
            voice_answer = "查询" + cityName + "雾霾信息失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    // 弹出选择器
    private void wheel() {
        CityConfig cityConfig = new CityConfig.Builder(getActivity()).title("添加城市")
                .titleTextSize(30)
                .titleTextColor("#0e73ba")
                .titleBackgroundColor("#C7C7C7")
                .confirTextColor("#ffffff")
                .confirmText("OK")
                .confirmTextSize(16)
                .cancelTextColor("#ffffff")
                .cancelText("CANCEL")
                .cancelTextSize(16)
//                .setCityWheelType(mWheelType)
//                .visibleItemsCount(visibleItems)
//                .province(defaultProvinceName)
//                .city(defaultCityName)
//                .district(defaultDistrict)
//                .setCityWheelType(mWheelType)
                .build();

        CityPickerView.getInstance().setConfig(cityConfig);
        CityPickerView.getInstance().setOnCityItemClickListener(new OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                if (province != null && city != null && district!=null) {
                    Log.d(TAG, "onSelected: 选择省:"+province.getName()+",选择市:"+city.getName()+",选择区:"+district.getName());
                    mCurrentCity = city.getName();
                    mCurrentCounty = district.getName();
                    Log.d(TAG, "onSelected: 市："+mCurrentCity+",县："+mCurrentCounty);
                    if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
                        getWeatherBasic(UNVOICE,mCurrentCounty,"",null,"");
//                        mWeatherPresenter.getWeather(district.getName());
                        if (!(province.getName().equals("香港") || province.getName().equals("澳门") ||
                                province.getName().equals("台湾省"))) {
                            getWeatherAqi(UNVOICE,mCurrentCity,"",null,"");
//                            mWeatherPresenter.getWeatherAqi(city.getName());
                        }else{
//                            mTextAir.setVisibility(View.INVISIBLE);
//                            mLayoutAir.setVisibility(View.INVISIBLE);
                        }
//                        mTitle.setText(district.getName());
                    } else {
                        ToastUtils.showError(mContext, getResources().getString(R.string.network_not_connected));
                    }
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(getActivity(),"取消",Toast.LENGTH_SHORT).show();
            }
        });
        CityPickerView.getInstance().showCityPicker(getActivity());
    }

    public void setReturnAnswerCallback(ReturnAnswerCallback returnAnswerCallback){
        mReturnAnswerCallback = returnAnswerCallback;
    }

    public class WeatherBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: actionId = "+intent.getAction());
            switch (intent.getAction()){
                case GlobalUtils.WEATHER_BROADCAST_ACTION:
                    mWeather_city = intent.getStringExtra("cityname");
                    mWeather_time = intent.getStringExtra("time");
                    mWeather_fun_flag = intent.getStringExtra("flag");
                    judgeVoiceAnswer(mWeather_city,mWeather_time,mWeather_fun_flag);
                    break;
                default:break;
            }
        }
    }

    private void judgeVoiceAnswer(String cityName,String time,String mWeather_fun_flag) {
        switch (mWeather_fun_flag){
            case "onWeatherInfo":
                Log.i("ryan", "judgeVoiceAnswer: weatherInfo");
                voiceCityName(cityName);
                Log.i("ryan", "onWeatherInfo: cityName = " + cityName);
                getWeatherBasic(VOICE,cityName,"",null,"");
                getWeatherAqi(VOICE,cityName,"",null,"");
                break;
            case "onRangeTempInfo":
                Log.i("ryan", "judgeVoiceAnswer: rangeTempInfo");
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,null,"onRangeTempInfo");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            case "onAirQualityInfo":
                voiceCityName(cityName);
                getWeatherAqi(VOICE,cityName,"",null,"onAirQualityInfo");
                getWeatherBasic(UNVOICE,cityName,"",null,"");
                break;
            case "onCurrentTempInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE, cityName, "", null, "onCurrentTempInfo");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            case "onWeatherStatus":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,null,"onWeatherStatus");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            case "onRainInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,null,"onRainInfo");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            case "onDressInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,"",null,"onDressInfo");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            case "onUitravioletLevelInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,"",null,"onUitravioletLevelInfo");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            case "onSmogInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,null,"onSmogInfo");
                getWeatherAqi(UNVOICE,cityName,"",null,"");
                break;
            default:break;
        }
    }
}
