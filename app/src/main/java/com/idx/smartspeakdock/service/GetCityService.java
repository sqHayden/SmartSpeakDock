package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.idx.smartspeakdock.utils.NetStatusUtils;

/**
 * Created by hayden on 18-1-6.
 */

public class GetCityService extends Service implements BDLocationListener{
    private String city_name;

    //定位的客户端
    private LocationClient mLocationClient;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /*
    * 初始化定位信息
    * **/
    private void init(){
        //设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        //设置坐标类型
        option.setCoorType("bd09ll");
        //打开GPS
        option.setOpenGps(true);
        //进行一次定位
        option.setScanSpan(0);
        //应用
        mLocationClient.setLocOption(option);
        //注册
        mLocationClient.registerLocationListener(this);
        //开启
        new Thread(){
            public void run() {
                if(NetStatusUtils.isMobileConnected(getApplicationContext())||NetStatusUtils.isWifiConnected(getApplicationContext())){
                    mLocationClient.start();//开启定位比较耗时，在启动的时候就调用
                }else{
                    Toast.makeText(getApplicationContext(),"你的网络有问题，请连接后重试",Toast.LENGTH_SHORT).show();
                }
            };
        }.start();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("my service start","123456");
        mLocationClient = new LocationClient(this);
        init();
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
        public String getCity(){
            Log.d("get city service run","123456");
            mLocationClient.stop();
            //设置定位的相关配置
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(0);
            //应用
            mLocationClient.setLocOption(option);
            Log.d("准备重新启动客户端服务","获取刷新地名");
            mLocationClient.start();
            return city_name;
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        city_name = bdLocation.getCity();
        Log.d("已经进入地点获取监听,地名为:",city_name);
    }
}
