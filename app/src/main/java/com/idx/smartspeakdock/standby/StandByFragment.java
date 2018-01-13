package com.idx.smartspeakdock.standby;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.idx.smartspeakdock.standby.presenter.StandByPresenter;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.ToastUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class StandByFragment extends BaseFragment implements IStandByView{
    private static final String TAG = StandByFragment.class.getSimpleName();
    private LinearLayout layout;
    private TextView location_textView;
    private TextView standby_life_clothes;
    private TextView standby_life_car;
    private TextView standby_weather_tmp;
    private LocationClient mLocationClient;
    private ImageView weatherIcon;
    private StandByPresenter mStandByPresenter;
    private String cityname = "深圳";
    private ImageView image_car;
    private ImageView image_clothes;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
//        mLocationClient = new LocationClient(mContext);
//        mLocationClient.registerLocationListener(new StandByFragment.StandbyLocationListener());
        Logger.setEnable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_standby,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    public void init(){
        layout = view.findViewById(R.id.line6);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        image_car = view.findViewById(R.id.image_car);
        image_clothes = view.findViewById(R.id.image_clothes);
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

//    private class StandbyLocationListener  implements BDLocationListener {
//
//        @Override
//        public void onReceiveLocation(BDLocation bdLocation) {
//            Logger.info(TAG, "onReceiveLocation: "+ bdLocation.getCity());
//            location_textView.setText(bdLocation.getCity());
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mLocationClient.stop();
    }

    private void showWeatherInfo(Weather weather) {
        if(weather.now.code != null) {
            Logger.info(TAG, "onResponse: weather.now.code = " + weather.now.code);
            weatherIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
        }else {
            weatherIcon.setImageResource(R.drawable.weather_unknown);
        }
        standby_weather_tmp.setText(weather.forecastList.get(0).max + " / " + weather.forecastList.get(0).min + "℃");
        standby_life_clothes.setText("穿衣：" + weather.lifestyleList.get(1).brf);
        standby_life_car.setText("洗车：" + weather.lifestyleList.get(6).brf);
    }
}
