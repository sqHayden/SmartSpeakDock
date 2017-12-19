package com.idx.smartspeakdock.utils;

import android.content.Context;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by hayden on 17-12-18.
 */
/*
 * 操作步骤
 * 1.Activity实现BDLocationlistener接口,重写onReceiveLocation,得到BDLocation获取城市
 * 2.CityUtils.getInstance(context)获取CityUtils实例
 * 3.cityUtils.getLocationClient获取locationClient对象
 * 4.locationCLient.registerLocationListener(this)注册监听器
 * 5.locationClient.start()启动
 * 6.cityUtils.destory()执行销毁操作
 */
public class CityUtils {
    //定位的客户端
    private LocationClient mLocationClient;

    /*
    * 构造私有化
    * **/
    private CityUtils(Context context) {
        mLocationClient = new LocationClient(context);
        init();
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
    }

    /*
    * 提供定位客户端的实例化对象
    * **/
    public LocationClient getLocationClient(){
        return mLocationClient;
    }

    /*
    * 提供对象的get方法
    * **/
    public static CityUtils getInstance(Context context){
        return new CityUtils(context);
    }

    /*
    * 对象销毁
    * **/
    public void destory(BDLocationListener bdLocationListener){
        mLocationClient.unRegisterLocationListener(bdLocationListener);
        bdLocationListener = null;
        mLocationClient.stop();
    }
}
