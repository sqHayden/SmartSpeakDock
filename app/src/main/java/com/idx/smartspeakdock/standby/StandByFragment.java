package com.idx.smartspeakdock.standby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.standby.presenter.StandByPresenter;
import com.idx.smartspeakdock.utils.BitmapUtils;
import com.idx.smartspeakdock.utils.ToastUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasic;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicDataSource;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicInjection;
import com.idx.smartspeakdock.weather.model.weatherroom.WeatherBasicRepository;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class StandByFragment extends BaseFragment implements IStandByView,ReturnCityName{
    private static final String TAG = StandByFragment.class.getSimpleName();
    private TextView location_textView;
    private TextView standby_life_clothes;
    private TextView standby_life_car;
    private TextView standby_weather_tmp;
    private ImageView weatherIcon;
    private StandByPresenter mStandByPresenter;
    public static String cityName = "" ;
    private Context mContext;
    private View view;
    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;
    BroadcastReceiver mReceiver;
    private ControllerService.MyBinder mControllerBinder;

    private WeatherBasicRepository mWeatherBasicRepository;
    private ImageView image_clothes;
    private ImageView image_car;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("cityName",cityName);
        Log.d(TAG, "onSaveInstanceState: ");
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (!cityName.isEmpty()){
                getWeatherBasic(cityName);
            }
        }else {
            //绑定service
            getActivity().bindService(((BaseActivity)getActivity()).mControllerintent, ((BaseActivity)getActivity()).myServiceConnection, 0);
            //设置回调
            ((BaseActivity)getActivity()).setReturnCityName(this);
        }

       getWeatherBasic(cityName);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("ryan", "onResume: standByFragment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_standby,container,false);
        init();
        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST_UPDATE_WEATHER");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                Log.d(TAG, "onReceive: ");
                getWeatherBasic(cityName);
            }
        };
        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    public void init(){
        weatherIcon = view.findViewById(R.id.weatherIcon);
        location_textView = view.findViewById(R.id.location_textView);
        standby_life_clothes = view.findViewById(R.id.standby_life_clothes);
        standby_life_car = view.findViewById(R.id.standby_life_car);
        standby_weather_tmp = view.findViewById(R.id.standby_weather_tmp);
        image_clothes = view.findViewById(R.id.image_clothes);
        image_car = view.findViewById(R.id.image_car);
        bitmap1 = BitmapUtils.decodeBitmapFromResources(getContext(),R.drawable.weather_life_clothes);
        bitmap2 = BitmapUtils.decodeBitmapFromResources(getContext(),R.drawable.weather_life_car_wash);
        bitmap3 = BitmapUtils.decodeBitmapFromResources(getContext(),R.drawable.weather_cloudy);
        image_clothes.setImageBitmap(bitmap1);
        image_car.setImageBitmap(bitmap2);
        weatherIcon.setImageBitmap(bitmap3);
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
        Log.d(TAG, "onDestroy: ");
//        getActivity().stopService(new Intent(getContext(),GetCityService.class));
        broadcastManager.unregisterReceiver(mReceiver);
        super.onDestroy();
        if (bitmap1 != null){
            bitmap1 = null;
        }
        if (bitmap2 != null){
            bitmap2 = null;
        }
        if (bitmap2 != null){
            bitmap2 = null;
        }
        getActivity().unbindService(((BaseActivity)getActivity()).myServiceConnection);
    }

    private void showWeatherInfo(Weather weather) {
        if(weather.now.code != null) {
            bitmap3 = BitmapUtils.decodeBitmapFromResources(getContext(),
                    HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
            weatherIcon.setImageBitmap(bitmap3);
        } else {
            bitmap3 = BitmapUtils.decodeBitmapFromResources(getContext(),
                    R.drawable.weather_unknown);
            weatherIcon.setImageBitmap(bitmap3);
        }
        standby_weather_tmp.setText(weather.forecastList.get(0).max + " / " + weather.forecastList.get(0).min + "℃");
        standby_life_clothes.setText(getActivity().getString(R.string.clothes)+": " + weather.lifestyleList.get(1).brf);
        standby_life_car.setText(getActivity().getString(R.string.wash_car)+": " + weather.lifestyleList.get(6).brf);
        location_textView.setText(weather.basic.cityName);
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

    /**
     * 回调拿值
     * **/
    @Override
    public void getCityName(String city) {
        cityName = city;
        getWeatherBasic(cityName);
    }
}
