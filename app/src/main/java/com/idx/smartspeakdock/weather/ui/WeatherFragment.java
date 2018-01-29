package com.idx.smartspeakdock.weather.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.standby.Utility;
import com.idx.smartspeakdock.utils.AppExecutors;
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
import com.idx.smartspeakdock.weather.presenter.ReturnAnswerCallback;
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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class WeatherFragment extends BaseFragment implements WeatherUi/*, ChooseCityDialogFragment.OnChooseCityCompleted*/ {
    private static final String TAG = WeatherFragment.class.getSimpleName();
    private static final int VOICE = 0;
    private static final int UNVOICE = 1;
    public SwipeRefreshLayout mRefreshWeather;
    public WeatherPresenter mWeatherPresenter;
    private ImageView mWeatherSelectCity;
    private View mWeatherView;
    private ImageView mWeatherNowIcon, mTomorrowIcon, mAfterIcon;
    private Dialog loadingDialog;
    private TextView mCond, mTemperature,
            mDate, mTime, mTitle, mTomorrow, mTomorrowTem, mAfter, mAfterTem, mLifestyleClothes, mLifestyleCar,
            mLifestyleAir, mPM25, mNO2, mCO, mVTomorrowWeek, mVAfterWeek;
    private LinearLayout mAirLayout;
    public static String mCurrentCity = "深圳";
    public static String mCurrentCounty = "深圳";
    private static String mSelectProvince="北京";
    private static String mSelectCity="北京";
    private static String mSelectCounty="北京";
    private Resources mResources;
    private Context mContext;

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
    private SharedPreferences sp;
//    private AppExecutors mAppExecutors;

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

    /**
     * 定时更新天气界面
     */
    private void initTimer() {
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mCurrentCounty)) {
                    getWeatherBasic(UNVOICE,mCurrentCity,"","");
                } else {
                    getWeatherBasic(UNVOICE,mCurrentCounty,"","");
                }
                if (judgeCityAqiIsQuery()) {
                    getWeatherAqi(UNVOICE, mCurrentCity, "", "");
                }
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTask, 60 * 1000, 60 * 1000);
    }

    public WeatherFragment(){}

    /**
     * 通过语音获取WeatherFragment对象，并传递相关参数
     *
     * @param cityName 城市名
     * @param time 时间
     * @param fun_flag 所问天气类型
     * @param flag 是否是语音
     * @return WeatherFragment对象
     */
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
        sp=getActivity().getSharedPreferences("city",Context.MODE_PRIVATE);
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
            Log.i(TAG, "onAttach: mWeather_voice_flag = "+mWeather_Voice_flag);
        }else {
            if (sp!=null){
                mCurrentCity=sp.getString("city","深圳");
                mCurrentCounty=sp.getString("county","深圳");
            }
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

//        mAppExecutors=new AppExecutors();
        if (savedInstanceState != null) {
            mCurrentCity = savedInstanceState.getString("city");
            mCurrentCounty = savedInstanceState.getString("county");
            Log.d(TAG, "onCreate: " + mCurrentCounty + ":" + mCurrentCity);
            mSelectProvince=savedInstanceState.getString("selectProvince");
            mSelectCity=savedInstanceState.getString("selectCity");
            mSelectCounty=savedInstanceState.getString("selectCounty");
        }
        mWeatherPresenter = new WeatherPresenterImpl(this);
        if (mWeather_Voice_flag == GlobalUtils.Weather.WEATHER_VOICE_FLAG){
            Log.i(TAG, "onCreate: voice_flag = "+mWeather_Voice_flag);
            judgeVoiceAnswer(mWeather_city,mWeather_time,mWeather_fun_flag);
        }else {
            Log.i(TAG, "onCreate: voice_flag_un = "+mWeather_Voice_flag);
            getWeatherBasic(UNVOICE,mCurrentCounty,"","");
            getWeatherAqi(UNVOICE, mCurrentCity, "", "");
        }
//        initTimer();
        //注册天气语音广播
        registerWeatherVoiceBroadcast();
//        mAppExecutors.getDiskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                CityPickerView.getInstance().init(mContext);
//            }
//        });
    }

    /**
     * 注册广播
     */
    private void registerWeatherVoiceBroadcast() {
        mWeatherBroadcastReceiver = new WeatherBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalUtils.Weather.WEATHER_BROADCAST_ACTION);
        mContext.registerReceiver(mWeatherBroadcastReceiver,intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.info(TAG, "onCreateView:");
        mWeatherView = inflater.inflate(R.layout.fragment_weather, container, false);
        //判断当前屏幕方向
        if (2 != getActivity().getResources().getConfiguration().orientation) {//竖屏
            mVTomorrowWeek=mWeatherView.findViewById(R.id.weather_vertical_forecast_tomorrow_week);
            mVAfterWeek=mWeatherView.findViewById(R.id.weather_vertical_forecast_after_week);
        }
        initView();
        judgeAirIsShow();
        return mWeatherView;
    }

    /**
     * 判断空气质量信息是否显示
     */
    private void judgeAirIsShow(){
        if (!(mCurrentCity.equals("香港") || mCurrentCity.equals("澳门") || mCurrentCity.equals("台北") ||
                mCurrentCity.equals("高雄") ||
                mCurrentCity.equals("台中"))) {
            mAirLayout.setVisibility(View.VISIBLE);
        }else {
            mAirLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 判断是否可以查询城市空气质量
     *
     * @return true:可以查询 false:不可以查询
     */
    private boolean judgeCityAqiIsQuery(){
        boolean flag=false;
        if (!(mCurrentCity.equals("香港") || mCurrentCity.equals("澳门") || mCurrentCity.equals("台北") ||
                mCurrentCity.equals("高雄") ||
                mCurrentCity.equals("台中"))) {
            flag=true;
        }
        return flag;
    }

    /**
     * view初始化
     */
    private void initView() {
        mWeatherSelectCity = mWeatherView.findViewById(R.id.weather_choose_city);
        mRefreshWeather = mWeatherView.findViewById(R.id.weather_swipe_refresh);
        mWeatherNowIcon = mWeatherView.findViewById(R.id.weather_now_icon);
        mCond = mWeatherView.findViewById(R.id.weather_now_cond_txt_n);
        mTemperature = mWeatherView.findViewById(R.id.weather_temp);
        mDate = mWeatherView.findViewById(R.id.weather_date);
        mTime = mWeatherView.findViewById(R.id.weather_time);
        mTitle = mWeatherView.findViewById(R.id.weather_city);
        mTomorrow = mWeatherView.findViewById(R.id.weather_forecast_tomorrow_time);
        mTomorrowTem = mWeatherView.findViewById(R.id.weather_forecast_tomorrow_tem);
        mTomorrowIcon = mWeatherView.findViewById(R.id.weather_forecast_tomorrow_icon);
        mAfter = mWeatherView.findViewById(R.id.weather_forecast_after_time);
        mAfterTem = mWeatherView.findViewById(R.id.weather_forecast_after_tem);
        mAfterIcon = mWeatherView.findViewById(R.id.weather_forecast_after_icon);
        mLifestyleClothes = mWeatherView.findViewById(R.id.weather_lifestyle_clothes_text);
        mLifestyleCar = mWeatherView.findViewById(R.id.weather_lifestyle_car_text);
        mLifestyleAir = mWeatherView.findViewById(R.id.weather_lifestyle_air_text);
        mAirLayout=mWeatherView.findViewById(R.id.weather_air);
        mPM25 = mWeatherView.findViewById(R.id.weather_air_pm25);
        mNO2 = mWeatherView.findViewById(R.id.weather_air_no2);
        mCO = mWeatherView.findViewById(R.id.weather_air_co);
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
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    /**
     * 语音赋值当前城市名
     *
     * @param cityName 城市名
     */
    private void voiceCityName(String cityName) {
        mCurrentCity = cityName;
        mCurrentCounty = cityName;
    }

    /**
     * 刷新天气
     */
    private void refresh() {
        mRefreshWeather.setColorSchemeResources(R.color.colorPrimary);
        mRefreshWeather.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetStatusUtils.isMobileConnected(mContext) || NetStatusUtils.isWifiConnected(mContext)) {
                    loading();
                    mWeatherPresenter.getWeather(mCurrentCounty);
                    if (judgeCityAqiIsQuery()) {
                        mWeatherPresenter.getWeatherAqi(mCurrentCity);
                    }else {
                        ToastUtils.showMessage(mContext,mResources.getString(R.string.weather_air_not_support));
                    }
                } else {
                    compelete();
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
        outState.putString("selectProvince", mSelectProvince);
        outState.putString("selectCity", mSelectCity);
        outState.putString("selectCounty", mSelectCounty);
    }

    /**
     * 显示加载对话框
     */
    public void loading(){
        try {
            loadingDialog.show();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * 隱藏加载对话框
     */
    public void compelete(){
        try {
            loadingDialog.dismiss();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
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
        compelete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefreshWeather.setRefreshing(false);
                Log.d(TAG, "run: 111111111111");
                ToastUtils.showError(mContext, mResources.getString(R.string.get_weather_info_error));
            }
        });
    }

    @Override
    public void setWeatherInfo(final Weather weather) {
        compelete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (weather==null) {
                    Log.d(TAG, "run: " + weather);
                }
                Log.i(TAG, "run: setWeatherInfo = "+weather.basic.cityName);
                updateWeatherInfo(weather);
                mRefreshWeather.setRefreshing(false);
                mTitle.setText(weather.basic.cityName);
            }
        });
    }

    /**
     * 更新天气信息
     *
     * @param weather 天气
     */
    public void updateWeatherInfo(Weather weather) {
        mCurrentCounty=weather.basic.cityName;
        mCurrentCity=weather.basic.pCityName;
        mWeatherNowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
        mCond.setText(HandlerWeatherUtil.getWeatherType(Integer.parseInt(weather.now.code)));
        mTemperature.setText(weather.forecastList.get(0).max + "℃ / " + weather.forecastList.get(0).min + "℃");
        mLifestyleClothes.setText(weather.lifestyleList.get(1).brf);
        mLifestyleCar.setText(weather.lifestyleList.get(6).brf);
        mLifestyleAir.setText(weather.lifestyleList.get(7).brf);
        mDate.setText(HandlerWeatherUtil.parseDate(new Date()));
        mTime.setText(HandlerWeatherUtil.parseTime(new Date()));
        mTitle.setText(weather.basic.cityName);
        if (mVTomorrowWeek != null) {
            mVTomorrowWeek.setText(HandlerWeatherUtil.parseDateWeek(weather.forecastList.get(1).date));
            mVAfterWeek.setText(HandlerWeatherUtil.parseDateWeek(weather.forecastList.get(2).date));
        }
        String tomorrow = HandlerWeatherUtil.parseDateLand(weather.forecastList.get(1).date);
        mTomorrow.setText(tomorrow);
        mTomorrowTem.setText(weather.forecastList.get(1).max + "℃ / " + weather.forecastList.get(1).min + "℃");
        mTomorrowIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(1).code)));

        String after = HandlerWeatherUtil.parseDateLand(weather.forecastList.get(2).date);
        mAfter.setText(after);
        mAfterTem.setText(weather.forecastList.get(2).max + "℃ / " + weather.forecastList.get(2).min + "℃");
        mAfterIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.forecastList.get(2).code)));
    }

    @Override
    public void setWeatherAqi(final Weather weather) {
        compelete();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWeatherAqi(weather);
                mRefreshWeather.setRefreshing(false);
            }
        });
    }

    /**
     * 更新空气质量信息
     *
     * @param weather 天气
     */
    public void updateWeatherAqi(Weather weather) {
        mPM25.setText(weather.air.pm25);
        mNO2.setText(weather.air.no2);
        mCO.setText(weather.air.co);
    }

//    @Override
//    public void chooseCityCompleted(String countyName, String cityName) {
//        Log.d(TAG, "chooseCityCompleted: " + cityName + ":" + countyName);
//        mCurrentCounty = countyName;
//        mCurrentCity = cityName;
//        if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
//            mWeatherPresenter.getWeather(countyName);
//            Log.d(TAG, cityName);
//            Log.d(TAG, "chooseCityCompleted: " + cityName);
//            if (!(cityName.equals("香港") || cityName.equals("澳门") || cityName.equals("台北") || cityName.equals("高雄") || cityName.equals("台中"))) {
//                mWeatherPresenter.getWeatherAqi(cityName);
//            }
//            mTitle.setText(countyName);
//        } else {
//            ToastUtils.showError(mContext, getResources().getString(R.string.network_not_connected));
//        }
//    }
    @Override
    public void onResume() {
        super.onResume();
        initTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTimer!=null) {
            mTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        Logger.info(TAG, "onDestroy: ");
        super.onDestroy();
        //解绑广播注册
        mContext.unregisterReceiver(mWeatherBroadcastReceiver);
        mWeather_city = "";
        mWeather_time = "";
        mWeather_fun_flag = "";
        mWeather_Voice_flag = -1;
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if (mTask!=null){
            mTask=null;
        }
        if (loadingDialog!=null){
            loadingDialog=null;
        }
        if (mWeatherPresenter!=null){
            mWeatherPresenter=null;
        }
        if (mWeatherBasicRepository!=null){
            mWeatherBasicRepository=null;
        }
        if (mWeatherAqiRepository!=null){
            mWeatherAqiRepository=null;
        }
        if (voice_weather!=null){
            voice_weather=null;
        }
        if (voice_aqi!=null){
            voice_aqi=null;
        }
        if (mReturnAnswerCallback!=null){
            mReturnAnswerCallback=null;
        }
//        if (mAppExecutors!=null){
//            mAppExecutors=null;
//        }
        spSaveInfo();
        if (sp!=null){
            sp=null;
        }
//        if (mWeather_return_voice!=null){
//            mWeather_return_voice=null;
//        }
    }

    /**
     * 保存城市名和县名
     */
    private void spSaveInfo(){
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("city",mCurrentCity);
        editor.putString("county",mCurrentCounty);
        editor.apply();
        if (editor!=null){
            editor=null;
        }
    }

    /**
     * 加载本地天气数据,未找到则加载网络天气信息
     *
     * @param way 加载天气方式：语音/非语音
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     * @param funcTag 问题类型(温度、穿衣、紫外线等等)
     */
    private void getWeatherBasic(final int way, final String cityName, final String time, final String funcTag) {
        mWeatherBasicRepository = WeatherBasicInjection.getNoteRepository(getActivity());
        mWeatherBasicRepository.getWeatherBasic(cityName + "%", new WeatherBasicDataSource.LoadWeatherBasicsCallback() {
            @Override
            public void onWeatherBasicsLoaded(WeatherBasic weatherBasic) {
                Log.d(TAG, "onWeatherBasicsLoaded: 加载数据库天气数据");
                String weatherBasicInfo = weatherBasic.weatherBasic;
                voice_weather = Utility.handleWeatherResponse(weatherBasicInfo);
                setWeatherInfo(voice_weather);
                voiceReturnJudge(cityName,time,funcTag);
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "onDataNotAvailable: 加载网络天气数据");
                if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
                    switch (way) {
                        case UNVOICE:
                            mWeatherPresenter.getWeather(cityName);
                            break;
                        case VOICE:
                            WeatherUtil.loadWeather(cityName, new ReturnWeather() {
                                @Override
                                public void onReturnWeather(Weather weather) {
                                    if (mReturnAnswerCallback != null) {
                                        if (weather != null) {
                                            voice_weather = weather;
                                            Log.i(TAG, "onReturnWeather: voice_weather_city = " + voice_weather.basic.cityName);
                                            setWeatherInfo(weather);
                                            voiceReturnJudge(cityName, time, funcTag);
                                        }
                                    }
                                }
                            });
                            break;
                    }
                }else {
                    compelete();
                    ToastUtils.showMessage(mContext, mResources.getString(R.string.network_not_connected));
                }
            }
        });
    }

    /**
     * 加载本地空气质量数据,未找到则加载网络空气质量信息
     *
     * @param way 加载天气方式：语音/非语音
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     * @param funcTag 问题类型(温度、穿衣、紫外线等等)
     */
    private void getWeatherAqi(final int way, final String cityName, final String time, final String funcTag) {
        mWeatherAqiRepository = WeatherAqiInjection.getInstance(getActivity());
        mWeatherAqiRepository.getWeatherAqi(cityName + "%", new WeatherAqiDataSource.LoadWeatherAqisCallback() {
            @Override
            public void onWeatherAqisLoaded(WeatherAqi weatherAqi) {
                Log.d(TAG, "onWeatherAqisLoaded: 加载数据库空气质量信息");
                String weatherAqiInfo = weatherAqi.weatherAqi;
                voice_aqi = Utility.handleWeatherResponse(weatherAqiInfo);
                setWeatherAqi(voice_aqi);
                voiceReturnJudge(cityName,time,funcTag);
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "onDataNotAvailable: 加载网络空气质量信息");
                if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
                    switch (way) {
                        case UNVOICE:
                            if (judgeCityAqiIsQuery()) {
                                mWeatherPresenter.getWeatherAqi(cityName);
                            }else {
                                ToastUtils.showMessage(mContext,mResources.getString(R.string.weather_air_not_support));
                            }
                            break;
                        case VOICE:
                            if (judgeCityAqiIsQuery()) {
                                WeatherUtil.loadWeatherAqi(cityName, new ReturnWeather() {
                                    @Override
                                    public void onReturnWeather(Weather weather) {
                                        if (mReturnAnswerCallback != null) {
                                            if (weather != null) {
                                                voice_aqi = weather;
                                                setWeatherAqi(weather);
                                                voiceReturnJudge(cityName, time, funcTag);
                                            }
                                        }
                                    }
                                });
                            }else {
                                voice_answer=null;
                                voiceReturnJudge(cityName, time, funcTag);
                                ToastUtils.showMessage(mContext,mResources.getString(R.string.weather_air_not_support));
                            }
                            break;
                    }
                }else {
                    compelete();
                    ToastUtils.showMessage(mContext, mResources.getString(R.string.network_not_connected));
                }
            }
        });
    }

    /**
     * 判断所问问题类型(温度、穿衣、紫外线等等)
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     * @param funcTag 问题类型(温度、穿衣、紫外线等等)
     */
    private void voiceReturnJudge(String cityName,String time,String funcTag){
        if (mReturnAnswerCallback!=null) {
            switch (funcTag) {
                case "onWeatherInfo":
                    weatherInfo(cityName);
                    break;
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

    /**
     * 语音查看城市天气信息
     *
     * @param cityName 城市名
     */
    private void weatherInfo(String cityName){
        if (voice_weather != null && voice_weather.status.equals("ok")){
            voice_answer = "好的,你可以查看"+cityName+"的天气了";
        }else {
            voice_answer = "查询"+cityName+"天气信息失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    /**
     * 语音查询城市三天内最高最低温度
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
    private void rangeTempInfo(String cityName,String time){
        if (voice_weather != null && voice_weather.status.equals("ok")) {
            Log.i(TAG, "rangeTempInfo: city = " + voice_weather);
            judgeRangeTempInfo(cityName, time);
        } else {
            voice_answer = "查询" + time + cityName + "温度信息失败";
        }
        if (mReturnAnswerCallback != null){
            mReturnAnswerCallback.onReturnAnswer(voice_answer);
        }
    }

    /**
     * 温度相关方法
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
    private void judgeRangeTempInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.Weather.WEATHER_TIME_TODAY:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(0).max + "度,最低温度为" + voice_weather.forecastList.get(0).min + "度";
                break;
            case GlobalUtils.Weather.WEATHER_TIME_TOMM:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(1).max + "度,最低温度为" + voice_weather.forecastList.get(1).min + "度";
                break;
            case GlobalUtils.Weather.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName + time + "最高温度为" + voice_weather.forecastList.get(2).max + "度,最低温度为" + voice_weather.forecastList.get(2).min + "度";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    /**
     * 语音查询城市空气质量
     *
     * @param cityName 城市名
     */
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

    /**
     * 语音查询城市当前温度
     *
     * @param cityName 城市名
     */
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

    /**
     * 语音查询城市三天内天气状况
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
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

    /**
     * 天气状况相关方法
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
    private void judgeStatusInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.Weather.WEATHER_TIME_TODAY:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code)) + "天."
                        +"最高温度为" + voice_weather.forecastList.get(0).max + "度,最低温度为" + voice_weather.forecastList.get(0).min + "度";
                break;
            case GlobalUtils.Weather.WEATHER_TIME_TOMM:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)) + "天."
                        +"最高温度为" + voice_weather.forecastList.get(1).max + "度,最低温度为" + voice_weather.forecastList.get(1).min + "度";
                break;
            case GlobalUtils.Weather.WEATHER_TIME_POSTNATAL:
                voice_answer = cityName + time + HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)) + "天."
                        +"最高温度为" + voice_weather.forecastList.get(2).max + "度,最低温度为" + voice_weather.forecastList.get(2).min + "度";
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    /**
     * 语音查看城市三天内是否有雨
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
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

    /**
     * 雨相关方法
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
    private void judgeRainInfo(String cityName, String time) {
        switch (time) {
            case GlobalUtils.Weather.WEATHER_TIME_TODAY:
                rainResult(cityName, time);
                break;
            case GlobalUtils.Weather.WEATHER_TIME_TOMM:
                rainResult(cityName, time);
                break;
            case GlobalUtils.Weather.WEATHER_TIME_POSTNATAL:
                rainResult(cityName, time);
                break;
            default:
                voice_answer = "抱歉，只能查询今天、明天、后天三天以内的天气信息";
                break;
        }
    }

    /**
     * 雨相关方法
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
    private void rainResult(String cityName, String time) {
        if (judgeRain(time)) {
            voice_answer = time + cityName + "有雨";
        } else {
            voice_answer = time + cityName + "没有雨";
        }
    }

    /**
     * 雨相关方法
     *
     * @param time 时间（今天，明天，后天）
     * @return 是否有雨 true:有 false:无
     */
    private boolean judgeRain(String time) {
        switch (time) {
            case GlobalUtils.Weather.WEATHER_TIME_TODAY:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.now.code)).equals("雨") ? true : false;
            case GlobalUtils.Weather.WEATHER_TIME_TOMM:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(1).code)).equals("雨") ? true : false;
            case GlobalUtils.Weather.WEATHER_TIME_POSTNATAL:
                return HandlerWeatherUtil.getWeatherType(Integer.parseInt(voice_weather.forecastList.get(2).code)).equals("雨") ? true : false;
            default:
                return false;
        }
    }

    /**
     * 语音查看穿衣建议
     */
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

    /**
     * 语音查询城市紫外线强弱
     *
     * @param cityName 城市名
     */
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

    /**
     * 语音查看城市三天内雾霾状况
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     */
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

    /**
     * 地址选择器
     */
    private void wheel() {
        CityConfig cityConfig = new CityConfig.Builder(getActivity())
                .title(mResources.getString(R.string.weather_add_city))
//                .titleTextSize(30)
//                .titleTextColor("#0e73ba")
//                .titleBackgroundColor("#C7C7C7")
//                .confirTextColor("#ffffff")
                .confirmText(mResources.getString(R.string.weather_add_city_sure))
//                .confirmTextSize(16)
//                .cancelTextColor("#ffffff")
                .cancelText(mResources.getString(R.string.weather_add_city_cancel))
//                .cancelTextSize(16)
//                .setCityWheelType(mWheelType)
//                .visibleItemsCount(visibleItems)
                .province(mSelectProvince)
                .city(mSelectCity)
                .district(mSelectCounty)
//                .setCityWheelType(mWheelType)
                .build();

        CityPickerView.getInstance().setConfig(cityConfig);
        CityPickerView.getInstance().setOnCityItemClickListener(new OnCityItemClickListener() {

            /**
             * 选择城市回调
             * @param province 选择省对应bean
             * @param city 选择市对应bean
             * @param district 选择县（区）对应bean
             */
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                if (province != null && city != null && district!=null) {
                    mSelectProvince=province.getName();
                    mSelectCity=city.getName();
                    mSelectCounty=district.getName();
                    Log.d(TAG, "onSelected: 选择省:"+province.getName()+",选择市:"+city.getName()+",选择区:"+district.getName());
                    if (NetStatusUtils.isWifiConnected(mContext) || NetStatusUtils.isMobileConnected(mContext)) {
//                        mCurrentCity = city.getName();
//                        mCurrentCounty = district.getName();
                        Log.d(TAG, "onSelected: 市："+city.getName()+",县："+district.getName());
                        getWeatherBasic(UNVOICE,district.getName(),"","");
//                        mWeatherPresenter.getWeather(district.getName());
                        if (!(province.getName().equals("香港") || province.getName().equals("澳门") ||
                                province.getName().equals("台湾省"))) {
                            mAirLayout.setVisibility(View.VISIBLE);
                            getWeatherAqi(UNVOICE,city.getName(),"","");
//                            mWeatherPresenter.getWeatherAqi(city.getName());
                        }else{
                            mAirLayout.setVisibility(View.GONE);
                            ToastUtils.showMessage(mContext,mResources.getString(R.string.weather_air_not_support));
                        }
                    } else {
                        compelete();
                        ToastUtils.showError(mContext, getResources().getString(R.string.network_not_connected));
                    }
                }
            }

            /**
             * 取消选择城市
             */
            @Override
            public void onCancel() {
                Toast.makeText(getActivity(),mResources.getString(R.string.weather_cancel_select_city),Toast.LENGTH_SHORT).show();
            }
        });
        CityPickerView.getInstance().showCityPicker(getActivity());
    }

    /**
     * 设置语音回调对象
     *
     * @param returnAnswerCallback 回调对象
     */
    public void setReturnAnswerCallback(ReturnAnswerCallback returnAnswerCallback){
        mReturnAnswerCallback = returnAnswerCallback;
    }

    /**
     * 定义广播 接收到语音就发广播
     */
    public class WeatherBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: actionId = "+intent.getAction());
            switch (intent.getAction()){
                case GlobalUtils.Weather.WEATHER_BROADCAST_ACTION:
                    mWeather_city = intent.getStringExtra("cityname");
                    mWeather_time = intent.getStringExtra("time");
                    mWeather_fun_flag = intent.getStringExtra("flag");
                    judgeVoiceAnswer(mWeather_city,mWeather_time,mWeather_fun_flag);
                    break;
                default:break;
            }
        }
    }

    /**
     * 判断所问问题类型(温度、穿衣、紫外线等等)
     *
     * @param cityName 城市名
     * @param time 时间（今天，明天，后天）
     * @param mWeather_fun_flag 问题类型(温度、穿衣、紫外线等等)
     */
    private void judgeVoiceAnswer(String cityName,String time,String mWeather_fun_flag) {
        switch (mWeather_fun_flag){
            case "onWeatherInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,"","onWeatherInfo");
                getWeatherAqi(VOICE,cityName,"","");
                break;
            case "onRangeTempInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,"onRangeTempInfo");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            case "onAirQualityInfo":
                voiceCityName(cityName);
                getWeatherAqi(VOICE,cityName,"","onAirQualityInfo");
                getWeatherBasic(UNVOICE,cityName,"","");
                break;
            case "onCurrentTempInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE, cityName, "", "onCurrentTempInfo");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            case "onWeatherStatus":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,"onWeatherStatus");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            case "onRainInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,"onRainInfo");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            case "onDressInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,"","onDressInfo");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            case "onUitravioletLevelInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,"","onUitravioletLevelInfo");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            case "onSmogInfo":
                voiceCityName(cityName);
                getWeatherBasic(VOICE,cityName,time,"onSmogInfo");
                getWeatherAqi(UNVOICE,cityName,"","");
                break;
            default:break;
        }
    }
}
