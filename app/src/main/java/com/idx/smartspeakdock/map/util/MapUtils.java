package com.idx.smartspeakdock.map.util;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.idx.smartspeakdock.utils.NetStatusUtils;

/**
 * Created by hayden on 18-2-1.
 */

public class MapUtils {
    private static final String TAG = "MapUtils";
    private String city_name;
    //定位的客户端
    private static AMapLocationClient mLocationClient;
    //定位参数
    private static AMapLocationClientOption mLocationOption;

    /**
     * 设置定位参数
     *
     * @return 定位参数类
     */
    private static AMapLocationClientOption getOption() {
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


    public static void getCity(final Context context) {
        //初始化client
        mLocationClient = new AMapLocationClient(context);
        //设置定位参数
        mLocationClient.setLocationOption(getOption());
        // 设置定位监听
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    Log.d(TAG, "onLocationChanged: 定位回调至ControllerService:"+aMapLocation.getCity());
                    callBack.call(aMapLocation.getCity());
                } else {
                    callBack.call("查询失败");
                }
            }
        });
        // 启动定位
        if (!mLocationClient.isStarted()) {
            //开启
            new Thread() {
                public void run() {
                    if (NetStatusUtils.isMobileConnected(context) || NetStatusUtils.isWifiConnected(context)) {
                        mLocationClient.startLocation();//开启定位比较耗时，在启动的时候就调用
                    } else {
//                        Toast.makeText(context, "你的网络有问题，请连接后重试", Toast.LENGTH_SHORT).show();
                        callBack.call("深圳市");
                    }
                }
            }.start();
        }
    }

    public static void setCallBack(CallBack cb){
        callBack = cb;
    }

    public interface CallBack{
        void call(String name);
    }

    private static CallBack callBack;
}
