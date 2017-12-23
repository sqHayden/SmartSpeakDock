package com.idx.smartspeakdock.map.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MyOrientationListener implements SensorEventListener {

    private static final String TAG = "sensor";

    //传感器管理者对象
    private SensorManager sensorManager;
    //上下文
    private Context context;
    //传感器对象
    private Sensor sensor;
    //X坐标
    private float lastX;

    public MyOrientationListener(Context context) {
        this.context = context;
    }

    //开始监听
    public void start(){
        Log.d(TAG, "start: ");
        //拿到系统传感器服务
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager!=null){
            Log.d("进入：","传感器管理对象获取成功");
            //获得方向传感器
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sensorManager.registerListener(this,sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    //结束监听
    public void stop(){
        sensorManager.unregisterListener(this);
    }

    //方向发生变化
    @Override
    public void onSensorChanged(SensorEvent event) {
        //传感器返回类型为方向传感器
       if(event.sensor.getType()== Sensor.TYPE_ORIENTATION){
           float x = event.values[SensorManager.DATA_X];
           //方向大于1度
           if(Math.abs(x-lastX)>1.0){
              if(onOrientationListener!=null){
                  onOrientationListener.onOrientationChanged(x);
              }
           }
           lastX = x;
       }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private OnOrientationListener onOrientationListener;

    //此方法是传感器大接口的方法，用来接收水平传感对象
    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        this.onOrientationListener = onOrientationListener;
    }

    public interface OnOrientationListener{
        void onOrientationChanged(float x);
    }
}
