package com.idx.smartspeakdock.map;

/**
 * Created by hayden on 18-1-15.
 */

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.SpeakerApplication;
import com.idx.smartspeakdock.map.Bean.CityCallBack;
import com.idx.smartspeakdock.map.Bean.ReturnMapAnswerCallBack;
import com.idx.smartspeakdock.map.adapter.SearchResultAdapter;
import com.idx.smartspeakdock.map.overlay.PoiOverlay;
import com.idx.smartspeakdock.map.util.Constants;
import com.idx.smartspeakdock.map.util.SensorEventHelper;
import com.idx.smartspeakdock.map.util.ToastUtil;
import com.idx.smartspeakdock.utils.GlobalUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * MapFragment
 */
public class MapFragment extends BaseFragment implements
        AMapLocationListener, AMap.OnMarkerClickListener,
        AMap.OnCameraChangeListener, View.OnClickListener,
        AMap.OnMapClickListener, PoiSearch.OnPoiSearchListener {
    //地图视图对象
    private MapView mapView;
    //地图控制对象
    private AMap aMap;
    //地图缩放级别
    private float zoom;
    //定位按钮
    private Button locbtn;
    //出行按钮
    private Button go_style;
    //地图标志物清除控件
    private ImageView clean_view;
    //地图搜索框控件
    private TextView input_text;
    //ListView控件定义
    private ListView listView;
    //地图定位控制对象
    private AMapLocationClient mLocationClient;
    //位置标记marker
    private Marker mBigIcon, mSmallIcon;
    //传感器对象
    private SensorEventHelper mSensorHelper;
    //定位InfoWindow内容
    private String LOCATION_MARKER_FLAG = "mylocation";
    //是否首次定位标识
    private boolean isFirstLoc = true;
    //我的位置对象
    private LatLng currentLocation = null;
    //所在城市名称
    private String currentCity = "";
    //传输对象
    private Gson gson;
    //搜索时进度条
    private ProgressDialog progDialog = null;
    //搜索Pio的返回结果集
    private PoiResult poiResult;
    //Poi查询条件类
    private PoiSearch.Query query;
    //Poi搜索对象
    private PoiSearch poiSearch;
    //Poi返回对象Marker标识定义
    private Marker mPoiMarker;
    //搜索关键字
    private String mKeyWords;
    //List结果集
    private List<PoiItem> poiItems;
    //List结果集
    private List<Tip> tipList;
    //用来转换的结果集
    private List<PoiItem> is = new ArrayList<>();
    //单项结果
    private PoiItem poiItem;
    private Tip tip;
    private Marker mMarker;
    //结果集数据对象
    private SearchResultAdapter searchResultAdapter;
    //定义全局的marker管理
    private PoiOverlay poiOverlay;
    //搜索结果返回码
    public static final int REQUEST_CODE = 100;
    public static final int RESULT_CODE_INPUTTIPS = 101;
    public static final int RESULT_CODE_KEYWORDS = 102;
    //定位蓝点颜色参数
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    //语音回调相关参数传递
    private String name;
    private String address;
    private String fromAddress;
    private String toAddress;
    private String pathWay;
    private MapBroadcastReceiver mMapBroadcastReceiver;
    private Context context;
    private PoiItem firstPoiItem, secondPoiItem;
    private boolean isFirstLocation;
    private boolean isVoice;
    private int map_voice;
    private boolean isPoiSearch;
    private static final String TAG = "MapFragment";


    //无参构造
    public MapFragment() {
    }

    public static MapFragment newInstance(String name1, String address1, String fromAddress1, String toAddress1, String pathWay1, int map_voice_flag) {
        Log.d("newInstance地图信息查看：", "name:" + name1 + "---" + "address:" + address1 + "---" + "fromAddress:" + fromAddress1 + "---" + "toAddress:" + toAddress1 + "---" + "pathWay:" + pathWay1 + "flag" + map_voice_flag);
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(GlobalUtils.Map.MAP_NAME, name1);
        args.putString(GlobalUtils.Map.MAP_ADDRESS, address1);
        args.putString(GlobalUtils.Map.MAP_FROM_ADDRESS, fromAddress1);
        args.putString(GlobalUtils.Map.MAP_TO_ADDRESS, toAddress1);
        args.putString(GlobalUtils.Map.MAP_PATH_WAY, pathWay1);
        args.putInt(GlobalUtils.Map.MAP_VOICE, map_voice_flag);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            Bundle args = getArguments();
            name = args.getString(GlobalUtils.Map.MAP_NAME);
            Log.d("11111", "onAttach:name=" + name);
            address = args.getString(GlobalUtils.Map.MAP_ADDRESS);
            fromAddress = args.getString(GlobalUtils.Map.MAP_FROM_ADDRESS);
            toAddress = args.getString(GlobalUtils.Map.MAP_TO_ADDRESS);
            pathWay = args.getString(GlobalUtils.Map.MAP_PATH_WAY);
            map_voice = args.getInt(GlobalUtils.Map.MAP_VOICE);
            Log.d("onAttach地图信息查看：", "name:" + name + "---" + "address:" + address + "---" + "fromAddress:" + fromAddress + "---" + "toAddress:" + toAddress + "---" + "pathWay:" + pathWay + "flag" + map_voice);
        }
        if (name == null) {
            Log.d("11111", "onAttach: null");
        } else {
            Log.d("11111", "name=" + name);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mKeyWords = "";
        poiItems = new ArrayList<>();
        super.onCreate(savedInstanceState);
        //注册地图语音广播
        registerMapVoiceBroadcast();
        isFirstLocation = true;
        gson = new Gson();
    }

    private void registerMapVoiceBroadcast() {
        mMapBroadcastReceiver = new MapBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalUtils.Map.MAP_BROADCAST_ACTION);
        context = getContext();
        context.registerReceiver(mMapBroadcastReceiver, intentFilter);
    }

    /**
     * 注册广播
     **/
    public class MapBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("地图广播", "onReceive: actionId = " + intent.getAction());
            switch (intent.getAction()) {
                case GlobalUtils.Map.MAP_BROADCAST_ACTION:
                    name = intent.getStringExtra("name");
                    address = intent.getStringExtra("address");
                    fromAddress = intent.getStringExtra("fromAddress");
                    toAddress = intent.getStringExtra("toAddress");
                    pathWay = intent.getStringExtra("pathWay");
                    Log.d("广播中的出行方式:", pathWay);
                    speakMethod();
                    break;
                default:
                    break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //获取视图对象
        View view = inflater.inflate(R.layout.map_main, container, false);
        //资源初始化
        init(view, savedInstanceState);
        //初始化定位
        initLocation();
        //开始定位
        startLocation();
        return view;
    }

    /**
     * 资源控件初始化
     */
    private void init(View view, Bundle savedInstanceState) {
        //获取mapView
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        //定位按钮
        locbtn = (Button) view.findViewById(R.id.locbtn);
        locbtn.setOnClickListener(this);
        //出行按钮
        go_style = (Button) view.findViewById(R.id.go_style);
        go_style.setOnClickListener(this);
        //搜索框
        input_text = view.findViewById(R.id.input_text);
        input_text.setOnClickListener(this);
        //清除图片获取
        clean_view = view.findViewById(R.id.clean_keywords);
        clean_view.setOnClickListener(this);
        //拿到listView
        listView = view.findViewById(R.id.listview);
        listView.setOnItemClickListener(onItemClickListener);
        //方向传感器类
        mSensorHelper = new SensorEventHelper(getContext());
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        //获取UI设置对象
        UiSettings settings = aMap.getUiSettings();
        //设置默认定位按钮是否显示
        settings.setMyLocationButtonEnabled(false);
        //获取缩放按钮位置
        Log.d("" + settings.getZoomPosition(), "123");
        //设置缩放按钮位置
        settings.setZoomPosition(0);
        //设置标记物点击监听
        aMap.setOnMarkerClickListener(this);
        //设置地图缩放监听
        aMap.setOnCameraChangeListener(this);
        //设置地图点击事件监听
        aMap.setOnMapClickListener(this);
        //设置显示地图比例尺
        settings.setScaleControlsEnabled(true);
        //初始化交通信息
        initTraffic();
    }

    /**
     * 初始化交通信息
     **/
    private void initTraffic() {
        //自定义实时交通信息的颜色样式
        MyTrafficStyle myTrafficStyle = new MyTrafficStyle();
        myTrafficStyle.setSeriousCongestedColor(0xff92000a);
        myTrafficStyle.setCongestedColor(0xffea0312);
        myTrafficStyle.setSlowColor(0xffff7508);
        myTrafficStyle.setSmoothColor(0xff00a209);
        aMap.setMyTrafficStyle(myTrafficStyle);
        //显示实时交通状况
        aMap.setTrafficEnabled(true);
        //显示3D楼块
        aMap.showBuildings(true);
        //显示底图文字
        aMap.showMapText(true);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 处理语音执行及回调
     **/
    private void speakMethod() {
        Log.d("speakMethod地图信息查看：", "name:" + name + "---" + "address:" + address + "---" + "fromAddress:" + fromAddress + "---" + "toAddress:" + toAddress + "---" + "pathWay:" + pathWay + "flag:" + map_voice);
        if (name.equals("") && address.equals("") && fromAddress.equals("") && toAddress.equals("") && pathWay.equals("") && map_voice == 6) {//全空说明查位置，直接进行回调
            //隐藏list
            listView.setVisibility(View.GONE);
            //设置定位在 我的位置
            toMyLocation(currentLocation);
            //返回语音
            returnMapAnswerCallBack.onReturnAnswer("您当前位于" + LOCATION_MARKER_FLAG);
        } else if (name.equals("") && !address.equals("") && fromAddress.equals("") && toAddress.equals("") && pathWay.equals("") && map_voice == 6) {//说明搜索某地
            //设置文本框
            input_text.setText(address);
            //显示清除符号
            clean_view.setVisibility(View.VISIBLE);
            //清除标记
            clearMarkers();
            //定为语音的
            isVoice = true;
            //搜索
            doSearchQuery(address, 1);
            //显示
            listView.setVisibility(View.VISIBLE);
            //返回语音
            returnMapAnswerCallBack.onReturnAnswer("已为您搜索到该位置");
        } else if (name.equals("") && address.equals("") && !toAddress.equals("") && !pathWay.equals("") && map_voice == 6) {//说明要去某地
            if (!fromAddress.equals("")) {//处理从哪儿去哪儿
                Log.d("处理从哪儿", "去哪儿");
                setCityCallBack(new CityCallBack() {
                    @Override
                    public void getCityPoint(PoiItem poiItem) {
                        Log.d("poiItem", poiItem.getLatLonPoint().getLatitude() + "---" + poiItem.getLatLonPoint().getLongitude());
                        if (isFirstLocation) {
                            Log.d("起点位置回调被执行", "1");
                            firstPoiItem = poiItem;
                            isFirstLocation = false;
                            isVoice = true;
                            doSearchQuery(toAddress, 1);
                        } else {
                            Log.d("终点位置回调被执行", "2");
                            secondPoiItem = poiItem;
                            LatLng startLatlng = new LatLng(firstPoiItem.getLatLonPoint().getLatitude(), firstPoiItem.getLatLonPoint().getLongitude());
                            Log.d("起点坐标：", firstPoiItem.getLatLonPoint().getLatitude() + "," + firstPoiItem.getLatLonPoint().getLongitude());
                            LatLng endLatlng = new LatLng(secondPoiItem.getLatLonPoint().getLatitude(), secondPoiItem.getLatLonPoint().getLongitude());
                            Log.d("终点坐标:", secondPoiItem.getLatLonPoint().getLatitude() + "," + secondPoiItem.getLatLonPoint().getLongitude());
                            isFirstLocation = true;
                            int dis = 0;
                            if (!pathWay.equals("驾车")) {
                                //判断距离及出行方式(超过100公里会失败)
                                float distance = AMapUtils.calculateLineDistance(startLatlng, endLatlng);
                                dis = (int) distance / 1000;
                                Log.d("dis:", dis + "");
                            }
                            if (dis > 100) {
                                returnMapAnswerCallBack.onReturnAnswer("您要去的地方太远了，建议选择驾车模式");
                            } else {
                                //启动导航activity
                                Intent intent = new Intent(SpeakerApplication.getContext(), CalculateRouteActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                //添加起点及终点信息
                                Gson gson = new Gson();
                                intent.putExtra("start_location", gson.toJson(startLatlng));
                                intent.putExtra("end_location", gson.toJson(endLatlng));
                                intent.putExtra("start_name", fromAddress);
                                intent.putExtra("end_name", toAddress);
                                intent.putExtra("pathWay", pathWay);
                                SpeakerApplication.getContext().startActivity(intent);
                            }
                        }
                    }
                });
                if (goStyle()) {//如果是的话
                    isVoice = true;
                    doSearchQuery(fromAddress, 1);
                }
            } else {//处理从我的位置去哪里
                Log.d("处理从我的位置", "去哪儿");
                setCityCallBack(new CityCallBack() {
                    @Override
                    public void getCityPoint(PoiItem poiItem) {
                        Log.d("终点位置回调被执行", "123456");
                        firstPoiItem = poiItem;
                        LatLng endLatlng = new LatLng(firstPoiItem.getLatLonPoint().getLatitude(), firstPoiItem.getLatLonPoint().getLongitude());
                        int dis = 0;
                        if (!pathWay.equals("驾车")) {
                            //判断距离及出行方式(超过100公里会失败)
                            float distance = AMapUtils.calculateLineDistance(currentLocation, endLatlng);
                            dis = (int) distance / 1000;
                            Log.d("dis:", dis + "");
                        }
                        if (dis > 100) {
                            returnMapAnswerCallBack.onReturnAnswer("您要去的地方太远了，建议选择驾车模式");
                        } else {
                            //启动导航activity
                            Intent intent = new Intent(SpeakerApplication.getContext(), CalculateRouteActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            //添加起点及终点信息
                            Gson gson = new Gson();
                            intent.putExtra("start_location", gson.toJson(currentLocation));
                            intent.putExtra("end_location", gson.toJson(endLatlng));
                            intent.putExtra("end_name", toAddress);
                            intent.putExtra("pathWay", pathWay);
                            SpeakerApplication.getContext().startActivity(intent);
                        }
                    }
                });
                if (goStyle()) {//如果是的话
                    isVoice = true;
                    doSearchQuery(toAddress, 1);
                }
            }
        } else if (!name.equals("")) {//搜索指定词汇(小吃街、公交站)
            doSearchQuery(name, 1);
            //设置文本为关键字
            input_text.setText(name);
            //显示清除图标
            clean_view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 出行方式判断
     **/
    private boolean goStyle() {
        //先判断是不是已有的
        if (pathWay.equals("步行") || pathWay.equals("走着") || pathWay.equals("走路") || pathWay.equals("不行")) {
            pathWay = "步行";
            isVoice = true;
            doSearchQuery(toAddress, 1);
        } else if (pathWay.equals("骑自行车") || pathWay.equals("骑电动车") || pathWay.equals("骑车")) {
            pathWay = "骑车";
            isVoice = true;
            doSearchQuery(toAddress, 1);
        } else if (pathWay.equals("驾车") || pathWay.equals("开车")) {
            pathWay = "驾车";
            isVoice = true;
            doSearchQuery(toAddress, 1);
        } else if (pathWay.equals("坐公交") || pathWay.equals("公交")) {
            pathWay = "公交";
            isVoice = true;
            doSearchQuery(toAddress, 1);
        } else if (pathWay.equals("飞机") || pathWay.equals("坐飞机") || pathWay.equals("飞")) {
            returnMapAnswerCallBack.onReturnAnswer("坐飞机,你咋不上天呢");
            return false;
        } else {
            returnMapAnswerCallBack.onReturnAnswer("对不起，暂不支持此方式出行");
            return false;
        }
        return true;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        isFirstLoc = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //解绑广播注册
        context.unregisterReceiver(mMapBroadcastReceiver);
        mMapBroadcastReceiver = null;
        if (mBigIcon != null) {
            mBigIcon.destroy();
        }
        if (mSmallIcon != null) {
            mSmallIcon.destroy();
        }
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        mapView.onDestroy();
        mapView = null;
        aMap.clear();
        aMap = null;
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        if (currentLocation != null) {
            currentLocation = null;
        }
        if (poiResult != null) {
            poiResult = null;
        }
        if (poiSearch != null) {
            poiSearch = null;
        }
        if (mPoiMarker != null) {
            mPoiMarker.destroy();
        }
        if (poiItems != null) {
            poiItems.clear();
            poiItems = null;
        }
        if (searchResultAdapter != null) {
            searchResultAdapter = null;
        }
    }

    /**
     * 初始化定位，设置回调监听
     */
    private void initLocation() {
        //初始化client
        mLocationClient = new AMapLocationClient(getActivity().getApplicationContext());
        // 设置定位监听
        mLocationClient.setLocationListener(this);
    }

    /**
     * 设置定位参数
     *
     * @return 定位参数类
     */
    private AMapLocationClientOption getOption() {
        //初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
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
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        return mLocationOption;
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        if (checkWifiSetting()) {
            //设置定位参数
            mLocationClient.setLocationOption(getOption());
            // 启动定位
            mLocationClient.startLocation();
        } else {
            Toast.makeText(getContext(), "您的网络存在问题，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查网络连接状况
     */
    private boolean checkWifiSetting() {
        return true;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            currentLocation = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
            if (isFirstLoc) {
                Log.d("定位回调被执行", "123465");
                isFirstLoc = false;
                searchResultAdapter = new SearchResultAdapter(getActivity(), amapLocation);
                //获取定位信息
                StringBuffer buffer = new StringBuffer();
                buffer.append(amapLocation.getProvince() + ""
                        + amapLocation.getCity() + ""
                        + amapLocation.getDistrict() + ""
                        + amapLocation.getStreet() + ""
                        + amapLocation.getStreetNum());
                currentCity = amapLocation.getCity();
                //获取marker内容
                LOCATION_MARKER_FLAG = buffer.toString();
                //创建我的方向位置marker
                addCircle(currentLocation, 100.0f);//添加定位精度圆
                addLocationMarker(currentLocation);//添加定位图标
                mSensorHelper.setCurrentMarker(mSmallIcon);//定位图标旋转
                toMyLocation(currentLocation);
                //判断是否有语音
                speakMethod();
            }
        } else {
            String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
            Log.e("定位信息：", errText);
        }
    }

    /**
     * 点击事件回调方法
     */
    @Override
    public void onClick(View v) {
        if (currentLocation == null) {
            Toast.makeText(getContext(), "定位失败，请检查您的网络设置", Toast.LENGTH_LONG).show();
        } else {
            switch (v.getId()) {
                case R.id.locbtn://定位按钮
                    toMyLocation(currentLocation);
                    break;
                case R.id.input_text://跳转提示Activity
                    Log.d("EditText点击监听", "123456");
                    Intent intent = new Intent(getActivity(), InputTipsActivity.class);
                    //传递请求源
                    intent.putExtra("from", "MapFragment");
                    //传递当前城市
                    intent.putExtra("current_city", currentCity);
                    //传递当前位置
                    intent.putExtra("current_location", gson.toJson(currentLocation));
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.id.clean_keywords://关键字清除
                    input_text.setText("");
                    clearMarkers();
                    clean_view.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    go_style.setVisibility(View.VISIBLE);
                    break;
                case R.id.go_style://出行
                    Intent intent1 = new Intent(this.getActivity(), CalculateRouteActivity.class);
                    //发送点击的模式
                    intent1.putExtra("click_style", "startClick");
                    //发送位置信息
                    intent1.putExtra("start_location", gson.toJson(currentLocation));
                    //发送城市名字
                    intent1.putExtra("city_name", currentCity);
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 地图点击事件监听
     **/
    @Override
    public void onMapClick(LatLng latLng) {

    }

    /**
     * 我的位置小蓝点属性初始化
     **/
    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        aMap.addCircle(options);
    }

    /**
     * 添加我的位置覆盖物及浮动窗
     **/
    private void addLocationMarker(LatLng latlng) {
        if (mSmallIcon != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.map_navi_map_gps_locked)));
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mSmallIcon = aMap.addMarker(options);
        mSmallIcon.setObject("location");
        mSmallIcon.setTitle(LOCATION_MARKER_FLAG);
        mSmallIcon.setPerspective(true);
    }

    /**
     * marker标记点击事件
     **/
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        } else {
            marker.showInfoWindow();
            int i = (int) marker.getObject();
            //设置为第i个
            is.clear();
            poiItem = poiItems.get(i);
            //设置文本
            input_text.setText(poiItem.getTitle());
            clean_view.setVisibility(View.VISIBLE);
            searchResultAdapter.setPoiItem(poiItem);
            listView.setAdapter(searchResultAdapter);
            listView.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    /**
     * 地图放大缩小事件监听
     **/
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if (mSmallIcon != null) {
            //更大的地图状态
            if (cameraPosition.zoom < 13) {
                //关闭实时交通状况
                if (aMap.isTrafficEnabled()) {
                    aMap.setTrafficEnabled(false);
                }
                //隐藏方向marker
                if (mSmallIcon.isVisible()) {
                    mSmallIcon.setVisible(false);
                    //显示小蓝点
                    setMCLocationMarker();
                }
            } else {//更小的地图状态
                //开启实时交通状况
                if (!aMap.isTrafficEnabled()) {
                    aMap.setTrafficEnabled(true);
                }
                //隐藏小蓝点
                if (mBigIcon != null && mSmallIcon != null) {
                    if (mBigIcon.isVisible()) {
                        mBigIcon.setVisible(false);
                        //显示方向marker
                        mSmallIcon.setVisible(true);
                    }
                }
            }
        }
    }

    /**
     * 设置蓝点marker
     */
    private void setMCLocationMarker() {
        if (mBigIcon == null) {
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                    R.mipmap.map_gps_point)));
            options.anchor(0.5f, 0.5f);
            options.position(currentLocation);
            mBigIcon = aMap.addMarker(options);
            mBigIcon.setObject("location");
        }
        mBigIcon.setVisible(true);
    }

    /**
     * 输入提示activity选择结果后的处理逻辑
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CODE_INPUTTIPS && intent != null) {//单项点击
            //结果返回清除以前标记
            clearMarkers();
            //得到搜索方式
            isPoiSearch = intent.getBooleanExtra("isPoiSearch", true);
            if (isPoiSearch) {
                //得到Item返回的封装对象
                String value = intent.getStringExtra("poiItem");
                poiItem = gson.fromJson(value, new TypeToken<PoiItem>() {
                }.getType());
                input_text.setText(poiItem.getTitle());
                Log.d("直接进入了poiItem添加marker操作", "123456");
                addMarker(poiItem);
                if (!poiItem.getTitle().equals("")) {
                    clean_view.setVisibility(View.VISIBLE);
                }
            } else {
                //得到Tip的封装对象
                Tip tip = intent.getParcelableExtra(Constants.EXTRA_TIP);
                input_text.setText(tip.getName());
                Log.d("直接进入了Tip添加marker操作", "123456");
                addMarker(tip);
                input_text.setText(tip.getName());
                if (!tip.getName().equals("")) {
                    clean_view.setVisibility(View.VISIBLE);
                }
            }
            //显示框框
            listView.setVisibility(View.VISIBLE);
            //隐藏button
            go_style.setVisibility(View.GONE);
        } else if (resultCode == RESULT_CODE_KEYWORDS && intent != null) {//搜素返回
            //清除以前标记
            clearMarkers();
            //得到关键字
            input_text.setText(intent.getStringExtra("key_name"));
            //得到数据封装对象
            String value = intent.getStringExtra("poiItems");
            poiItems = gson.fromJson(value, new TypeToken<List<PoiItem>>() {
            }.getType());
            addPoiItemsMarker(poiItems);
            //隐藏button
            go_style.setVisibility(View.GONE);
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String s, int page) {
        isPoiSearch = true;
        //显示进度框
        showProgressDialog();
        //先判断一下
        if (s.equals("酒店") || s.equals("宾馆") || s.equals("旅社")) {
            Log.d("123456", "进入酒店查询了");
            query = new PoiSearch.Query("", "100105", "");
        } else if (s.equals("美食") || s.equals("小吃街")) {
            query = new PoiSearch.Query("", "050400", "");
        } else if (s.equals("商场") || s.equals("购物广场")) {
            query = new PoiSearch.Query("", "060101", "");
        } else if (s.equals("地铁") || s.equals("地铁站")) {
            query = new PoiSearch.Query("", "150500", "");
        } else if (s.equals("公交") || s.equals("公交站")) {
            query = new PoiSearch.Query("", "150700", "");
        } else if (s.equals("火车") || s.equals("火车站")) {
            query = new PoiSearch.Query("", "150200", "");
        } else {
            //直接查
            //第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            query = new PoiSearch.Query(s, "", currentCity);
            isPoiSearch = false;
        }
        if (isPoiSearch) {//周边搜索
            Log.d("123456", "进入周边搜索了");
            query.setPageSize(20);// 设置每页最多返回多少条poiitem
            query.setPageNum(0);//设置查询页码
            //构造对象并发送检索
            if (currentLocation != null) {
                poiSearch = new PoiSearch(getContext(), query);
                poiSearch.setOnPoiSearchListener(this);
                if (isPoiSearch) {
                    //设置搜索区域为以lp点为圆心，其周围5000米范围
                    LatLonPoint latLonPoint = new LatLonPoint(currentLocation.latitude, currentLocation.longitude);
                    poiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 5000, true));
                }
                //异步搜索
                poiSearch.searchPOIAsyn();
            } else {
                Log.d("a", "searchSurrend: 传过来的对象是空的");
            }
        } else {
            //设置每页最多返回多少条poiItem
            query.setPageSize(page);
            //设置查第一页
            query.setPageNum(1);
            //Poi查询设置
            poiSearch = new PoiSearch(getContext(), query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(getContext());
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + mKeyWords);
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiItems有多少页
                    Log.d("Poi搜索被执行", "获取坐标值");
                    poiItems = poiResult.getPois();
                    Log.d("布尔值监控：", isVoice + "");
                    if (cityCallBack != null && isVoice) {
                        isVoice = false;
                        cityCallBack.getCityPoint(poiItems.get(0));
                    }
                    if (isPoiSearch) {
                        Log.d("123456", "进来周边添加了");
                        addPoiItemsMarker(poiItems);
                        returnMapAnswerCallBack.onReturnAnswer("已为您搜索到附近" + name + "相关地点,请查看");
                        //隐藏button
                        go_style.setVisibility(View.GONE);
                    } else {
                        //获取第一页的数据
                        searchResultAdapter.setPoiItem(poiItems.get(0));
                        listView.setAdapter(searchResultAdapter);
                    }
                    // 取得第一页的poiItem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();
                    // 当搜索不到poiItem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        zoom = poiOverlay.zoomToSpan();
                        //我的位置点显示
                        if (zoom < 13) {
                            mSmallIcon.setVisible(false);
                            mBigIcon.setVisible(true);
                        } else if (mBigIcon != null) {
                            mSmallIcon.setVisible(true);
                            mBigIcon.setVisible(false);
                        }
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(getContext(), R.string.no_result);
                    }
                }
            } else {
                returnMapAnswerCallBack.onReturnAnswer("对不起,没有找到相关地点");
                ToastUtil.show(getContext(), R.string.no_result);
            }
        } else {
            returnMapAnswerCallBack.onReturnAnswer("哎呀，网络不给力，请重试！");
            ToastUtil.show(getContext(), R.string.net_error);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem item, int rCode) {
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(getContext(), infomation);
    }

    /**
     * 用marker展示输入提示list选中数据
     *
     * @param object
     */
    private void addMarker(Object object) {
        if (isPoiSearch) {//处理周边
            poiItem = (PoiItem) object;
            if (poiItem == null) {
                return;
            }
            mPoiMarker = aMap.addMarker(new MarkerOptions());
            LatLonPoint point = poiItem.getLatLonPoint();
            //list更新操作
            poiItems.clear();
            poiItems.add(poiItem);
            searchResultAdapter.setPoiItem(poiItems.get(0));
            listView.setAdapter(searchResultAdapter);
            if (point != null) {
                LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
                mPoiMarker.setPosition(markerPosition);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 20));
            }
            mPoiMarker.setTitle(poiItem.getTitle());
        } else {
            tip = (Tip) object;
            if (tip == null) {
                return;
            }
            mPoiMarker = aMap.addMarker(new MarkerOptions());
            LatLonPoint point = tip.getPoint();
            //list更新操作
            poiItems.clear();
            poiItems.add(new PoiItem("id", point, tip.getName(), tip.getAddress()));
            searchResultAdapter.setPoiItem(poiItems.get(0));
            listView.setAdapter(searchResultAdapter);
            if (point != null) {
                LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
                mPoiMarker.setPosition(markerPosition);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 20));
            }
            mPoiMarker.setTitle(tip.getName());
            mPoiMarker.setSnippet(tip.getAddress());
        }
    }

    /**
     * 用多Marker展示周边多地搜索
     **/
    private void addPoiItemsMarker(List<PoiItem> poiItems) {
        poiOverlay = new PoiOverlay(aMap, poiItems);
        poiOverlay.removeFromMap();
        poiOverlay.addToMap();
        zoom = poiOverlay.zoomToSpan();
    }


    /**
     * 列表点击监听
     */
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position != searchResultAdapter.getSelectedPosition()) {
                PoiItem poiItem = (PoiItem) searchResultAdapter.getItem(position);
                LatLng curLatlng = new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude());
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(curLatlng));
                searchResultAdapter.setSelectedPosition(position);
                searchResultAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * 我的位置
     **/
    private void toMyLocation(LatLng currentLocation) {
        //设置缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        //将地图移动到定位点
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(currentLocation));
    }

    //删除位置以外Marker
    private void clearMarkers() {
        //获取地图上所有Marker
        List<Marker> mapScreenMarkers = aMap.getMapScreenMarkers();
        Log.d("map Markers:", "" + mapScreenMarkers.size());
        for (int i = 0; i < mapScreenMarkers.size(); i++) {
            Marker marker = mapScreenMarkers.get(i);
            if (marker.getObject() != null) {
                if (!marker.getObject().toString().equals("location")) {
                    //移除当前Marker
                    marker.remove();
                }
            } else {
                //移除当前Marker
                marker.remove();
            }
        }
        //地图刷新
        aMap.runOnDrawFrame();
    }

    private ReturnMapAnswerCallBack returnMapAnswerCallBack;

    public void setMapReturnAnswerCallback(ReturnMapAnswerCallBack returnMapAnswerCallback) {
        returnMapAnswerCallBack = returnMapAnswerCallback;
    }

    private CityCallBack cityCallBack;

    public void setCityCallBack(CityCallBack cityCallBack1) {
        cityCallBack = cityCallBack1;
    }
}
