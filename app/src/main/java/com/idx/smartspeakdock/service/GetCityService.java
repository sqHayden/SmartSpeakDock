package com.idx.smartspeakdock.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.weather.utils.UpdateWeatherUtil;

/**
 * Created by hayden on 18-1-6.
 */

public class GetCityService extends Service implements AMapLocationListener{
    private static final String TAG = "GetCityService";
    private String city_name;

    //定位的客户端
    private AMapLocationClient mLocationClient;
    //定位参数
    private AMapLocationClientOption mLocationOption;

    @Override
    public void onCreate() {
        Log.d("服务已启动，已连接","123456");
//        mLocationClient = new LocationClient(getApplicationContext());
//        init();
        super.onCreate();
    }

    /**
     * 设置定位参数
     * @return 定位参数类
     */
    private AMapLocationClientOption getOption() {
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
//        //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(0);
        //是否只定位一次
        mLocationOption.setOnceLocation(true);
        return mLocationOption;
    }

    /**
     * 检查网络连接状况
     */
    private boolean checkWifiSetting() {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("服务已启动，已连接","123456");
        //初始化client
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 设置定位监听
        mLocationClient.setLocationListener(this);
        if(checkWifiSetting()){
            //设置定位参数
            mLocationClient.setLocationOption(getOption());
            // 启动定位
            mLocationClient.startLocation();
        }else {
            Toast.makeText(this,"您的网络存在问题，请重试", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "onStartCommand: ");
        UpdateWeatherUtil.updateWeather();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        cycleTime(alarmManager);
        return super.onStartCommand(intent, flags, startId);
    }


    //每隔30分钟，更新一次天气
    private void cycleTime(AlarmManager manager){
        int anHour =  30 * 60 * 1000; // 这是30min的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, GetCityService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        mLocationClient.unRegisterLocationListener(this);
//        mLocationClient.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MyBinder extends Binder {

        public void getCity(CallBack call){
            callBack = call;
            Log.d("进入到调取城市名称的方法","123456");
            if(!mLocationClient.isStarted()) {
                //开启
                new Thread(){
                    public void run() {
                        if(NetStatusUtils.isMobileConnected(getApplicationContext())||NetStatusUtils.isWifiConnected(getApplicationContext())){
                            mLocationClient.startLocation();//开启定位比较耗时，在启动的时候就调用
                        }else{
                            Toast.makeText(getApplicationContext(),"你的网络有问题，请连接后重试",Toast.LENGTH_SHORT).show();
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                Log.d("定位回调被执行","123465");
                city_name = amapLocation.getCity();
        } else {
            String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
            Log.e("定位信息：", errText);
        }
    }

    public interface CallBack{
        void call(AMapLocation amapLocation);
    }

    public CallBack callBack;
}
