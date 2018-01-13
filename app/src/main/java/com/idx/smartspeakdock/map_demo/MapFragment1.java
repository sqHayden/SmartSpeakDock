package com.idx.smartspeakdock.map_demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;


public class MapFragment1 extends BaseFragment {

    //地图控件
    private MapView mMapView;
    //地图控制对象
    private BaiduMap mBaiduMap;
    public static MapFragment1 newInstance(){return new MapFragment1();}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_demo_main,container,false);
    }
}
