package com.idx.smartspeakdock.standby;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.service.GetCityService;
import com.idx.smartspeakdock.service.AutoUpdateService;
import com.idx.smartspeakdock.standby.presenter.StandByPresenter;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.ToastUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenter;
import com.idx.smartspeakdock.weather.presenter.WeatherPresenterImpl;
import com.idx.smartspeakdock.weather.ui.WeatherUi;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class StandByFragment extends BaseFragment implements IStandByView{
    private static final String TAG = StandByFragment.class.getSimpleName();
    private TextView location_textView;
    private TextView standby_life_clothes;
    private TextView standby_life_car;
    private TextView standby_weather_tmp;
    private ImageView weatherIcon;
    private StandByPresenter mStandByPresenter;
    private String cityname = "深圳市";
    private Context mContext;
    private View view;
    private GetCityService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("binder连接已经执行","123456");
            myBinder = (GetCityService.MyBinder) service;
            //连接调用
            myBinder.getCity(new GetCityService.CallBack(){
                @Override
                public void call(BDLocation bdLocation) {
                    mStandByPresenter.requestWeather(bdLocation.getCity());
                }
            });
        }
    };
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;
    BroadcastReceiver mReceiver;

    private WeatherBasicRepository mWeatherBasicRepository;
    private LinearLayout line6;

    public static StandByFragment newInstance(){return new StandByFragment();}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("cityName",location_textView.getText().toString());
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        if(!BaseActivity.isServiceRunning(baseActivity.getApplicationContext(),"com.idx.smartspeakdock.start.GetCityService")) {
            Log.d("启动服务", "startService");
            Intent intent = new Intent(baseActivity.getApplicationContext(), GetCityService.class);
            //启动
            baseActivity.getApplicationContext().startService(intent);
            //绑定
            baseActivity.getApplicationContext().bindService(intent, connection, BIND_AUTO_CREATE);
        }
        Logger.setEnable(true);
        if (savedInstanceState != null) {
            if (!cityname.isEmpty()){
                getWeatherBasic(cityname);
            }
        }else {
            getActivity().startService(new Intent(getActivity(), GetCityService.class));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_standby,container,false);
//        Resources resources = getContext().getResources();
//        Drawable imageDrawable = resources.getDrawable(R.drawable.bg_vertical); //图片在drawable目录下

        init();
//        line6.setBackground(imageDrawable);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                Log.d(TAG, "onReceive: 30min update weather");
                getWeatherBasic(cityname);
            }
        };
        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    public void init(){
        line6 = view.findViewById(R.id.line6);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        location_textView = view.findViewById(R.id.location_textView);
        standby_life_clothes = view.findViewById(R.id.standby_life_clothes);
        standby_life_car = view.findViewById(R.id.standby_life_car);
        standby_weather_tmp = view.findViewById(R.id.standby_weather_tmp);
        location_textView.setTypeface(FontCustom.setHeiTi(mContext));
        standby_life_clothes.setTypeface(FontCustom.setHeiTi(mContext));
        standby_life_car.setTypeface(FontCustom.setHeiTi(mContext));
        standby_weather_tmp.setTypeface(FontCustom.setAvenir(mContext));
        mStandByPresenter = new StandByPresenter(this,mContext);
    }

    @Override
    public void setCurrentCityWeatherInfo(final Weather weather) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWeatherInfo(weather);
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onError(final String errorMsg) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showError(mContext, errorMsg);
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(new Intent(getContext(),GetCityService.class));
        broadcastManager.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showWeatherInfo(Weather weather) {
        if(weather.now.code != null) {
            weatherIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
        }else {
            weatherIcon.setImageResource(R.drawable.weather_unknown);
        }
        standby_weather_tmp.setText(weather.forecastList.get(0).max + " / " + weather.forecastList.get(0).min + "℃");
        standby_life_clothes.setText("穿衣：" + weather.lifestyleList.get(1).brf);
        standby_life_car.setText("洗车：" + weather.lifestyleList.get(6).brf);
        Log.d(TAG, "showWeatherInfo: show11111111");
    }

    public void getWeatherBasic(final String cityName) {
        Log.d(TAG, "getWeatherBasic: " + cityName);
        mWeatherBasicRepository = WeatherBasicInjection.getNoteRepository(getActivity());
        mWeatherBasicRepository.getWeatherBasic(cityName + "%", new WeatherBasicDataSource.LoadWeatherBasicsCallback() {
            @Override
            public void onWeatherBasicsLoaded(WeatherBasic weatherBasic) {
                Log.d(TAG, "onWeatherBasicsLoaded: " + weatherBasic.toString());
                String weatherBasicInfo = weatherBasic.weatherBasic;
                Weather weather = Utility.handleWeatherResponse(weatherBasicInfo);
                showWeatherInfo(weather);
            }

            @Override
            public void onDataNotAvailable() {
                Log.d(TAG, "onDataNotAvailable: ");
                mStandByPresenter.requestWeather(cityName);
            }
        });
    }
}
