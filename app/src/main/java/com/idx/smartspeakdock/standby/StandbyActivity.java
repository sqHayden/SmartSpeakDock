package com.idx.smartspeakdock.standby;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.standby.presenter.StandByPresenter;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.utils.HandlerWeatherUtil;

public class StandbyActivity extends BaseActivity implements IStandByView{
    private static final String TAG = "MainActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new StandbyLocationListener());
        setContentView(R.layout.activity_standby);
        init();
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StandbyActivity.this, SwipeActivity.class);
                startActivity(intent);
                mLocationClient.stop();
            }
        });
        requestLocation();
    }

    public void init(){
        layout = findViewById(R.id.line6);
        weatherIcon = findViewById(R.id.weatherIcon);
        image_car = findViewById(R.id.image_car);
        image_clothes = findViewById(R.id.image_clothes);
        location_textView = findViewById(R.id.location_textView);
        standby_life_clothes = findViewById(R.id.standby_life_clothes);
        standby_life_car = findViewById(R.id.standby_life_car);
        standby_weather_tmp = findViewById(R.id.standby_weather_tmp);
        location_textView.setTypeface(FontCustom.setHeiTi(getApplicationContext()));
        standby_life_clothes.setTypeface(FontCustom.setHeiTi(getApplicationContext()));
        standby_life_car.setTypeface(FontCustom.setHeiTi(getApplicationContext()));
        standby_weather_tmp.setTypeface(FontCustom.setAvenir(getApplicationContext()));
        mStandByPresenter = new StandByPresenter(this,this);
    }

    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }


    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setScanSpan(10 * 60 * 1000);
        mLocationClient.setLocOption(option);
    }


    @Override
    public void setCurrentCityWeatherInfo(final Weather weather) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showWeatherInfo(weather);
            }
        });
    }

    @Override
    public void onError(final String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StandbyActivity.this,errorMsg,Toast.LENGTH_LONG).show();
            }
        });
    }

    private class StandbyLocationListener  implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d(TAG, "onReceiveLocation: "+ bdLocation.getCity());
            location_textView.setText(bdLocation.getCity());
            mStandByPresenter.requestWeather(bdLocation.getCity());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    private void showWeatherInfo(Weather weather) {
        if(weather.now.code != null) {
            Log.i(TAG, "onResponse: weather.now.code = " + weather.now.code);
            weatherIcon.setImageResource(HandlerWeatherUtil.getWeatherImageResource(Integer.parseInt(weather.now.code)));
        }else {
            weatherIcon.setImageResource(R.drawable.weather_unknown);
        }
        standby_weather_tmp.setText(weather.forecastList.get(0).max + " / " + weather.forecastList.get(0).min + "℃");
        standby_life_clothes.setText("穿衣：" + weather.lifestyleList.get(1).brf);
        standby_life_car.setText("洗车：" + weather.lifestyleList.get(6).brf);
    }
}