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
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.weather.utils.UpdateWeatherUtil;

/**
 * Created by hayden on 18-1-6.
 */

public class GetCityService extends Service implements BDLocationListener{
    private static final String TAG = "GetCityService";
    private String city_name;

    //定位的客户端
    private LocationClient mLocationClient;
    @Override
    public void onCreate() {
        Log.d("服务已启动，已连接","123456");
        mLocationClient = new LocationClient(getApplicationContext());
        init();
        super.onCreate();
    }

    /*
    * 初始化定位信息
    * **/
    private void init(){
        //设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        //打开GPS
        option.setOpenGps(true);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(0);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        option.setIsNeedLocationDescribe(true);
        // 可选，设置是否需要设备方向结果
        option.setNeedDeviceDirect(false);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        //应用
        mLocationClient.setLocOption(option);
        //注册
        mLocationClient.registerLocationListener(this);
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        UpdateWeatherUtil.updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour =  30 * 60 * 1000; // 这是30min的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, GetCityService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.unRegisterLocationListener(this);
        mLocationClient.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
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
                            mLocationClient.start();//开启定位比较耗时，在启动的时候就调用
                        }else{
                            Toast.makeText(getApplicationContext(),"你的网络有问题，请连接后重试",Toast.LENGTH_SHORT).show();
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        city_name = bdLocation.getCity();
        Log.d("已经进入地点获取监听,地名为:",city_name);
        //回调
        if(callBack!=null) {
            callBack.call(bdLocation);
        }
        if(mLocationClient.isStarted()){
            Log.d("client","定位对象关闭");
            mLocationClient.stop();
        }
    }

    public interface CallBack{
        void call(BDLocation location);
    }

    public CallBack callBack;
}
