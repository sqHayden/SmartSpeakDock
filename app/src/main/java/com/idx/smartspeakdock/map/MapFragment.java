package com.idx.smartspeakdock.map;

/**
 * Created by hayden on 18-1-15.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.map.adapter.SearchResultAdapter;
import com.idx.smartspeakdock.map.overlay.PoiOverlay;
import com.idx.smartspeakdock.map.util.Constants;
import com.idx.smartspeakdock.map.util.SensorEventHelper;
import com.idx.smartspeakdock.map.util.ToastUtil;
import java.util.ArrayList;
import java.util.List;

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
    //结果集数据对象
    private SearchResultAdapter searchResultAdapter;
    //搜索结果返回码
    public static final int REQUEST_CODE = 100;
    public static final int RESULT_CODE_INPUTTIPS = 101;
    public static final int RESULT_CODE_KEYWORDS = 102;
    //定位蓝点颜色参数
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

    //构造参数
    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mKeyWords = "";
        poiItems = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置全屏
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setAttributes(lp);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
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
                buffer.append(amapLocation.getCountry() + ""
                        + amapLocation.getProvince() + ""
                        + amapLocation.getCity() + ""
                        + amapLocation.getProvince() + ""
                        + amapLocation.getDistrict() + ""
                        + amapLocation.getStreet() + ""
                        + amapLocation.getStreetNum());
                currentCity = amapLocation.getCity();
                //获取marker内容
                LOCATION_MARKER_FLAG = buffer.toString();
                //创建我的方向位置marker
                addCircle(currentLocation, 100.0f);//添加定位精度圆
                addMarker(currentLocation);//添加定位图标
                mSensorHelper.setCurrentMarker(mSmallIcon);//定位图标旋转
                toMyLocation(currentLocation);
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
        switch (v.getId()) {
            case R.id.locbtn://定位按钮
                toMyLocation(currentLocation);
                break;
            case R.id.input_text://关键字跳转
                Log.d("EditText点击监听", "123456");
                Intent intent = new Intent(getActivity(), InputTipsActivity.class);
                intent.putExtra("city_name", currentCity);
                intent.putExtra("style","search");
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
                Intent intent1 = new Intent(this.getActivity(),CalculateRouteActivity.class);
                Gson gson = new Gson();
                //发送点击的模式
                intent1.putExtra("click_style","startClick");
                //发送位置信息
                intent1.putExtra("start_location",gson.toJson(currentLocation));
                //发送城市名字
                intent1.putExtra("city_name",currentCity);
                startActivity(intent1);
                break;
            default:
                break;
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
    private void addMarker(LatLng latlng) {
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
     * @param data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE_INPUTTIPS && data
                != null) {
            //结果返回清除以前标记
            clearMarkers();
            //得到Item返回的封装对象
            Tip tip = data.getParcelableExtra(Constants.EXTRA_TIP);
            Log.d("item点击接收", tip.getName());
            if (tip.getPoiID() == null || tip.getPoiID().equals("")) {
                doSearchQuery(tip.getName());
            } else {
                Log.d("直接进入了添加marker操作", "123456");
                addTipMarker(tip);
            }
            input_text.setText(tip.getName());
            mKeyWords = tip.getName();
            if (!tip.getName().equals("")) {
                clean_view.setVisibility(View.VISIBLE);
            }
        } else if (resultCode == RESULT_CODE_KEYWORDS && data != null) {
            //清除以前标记
            clearMarkers();
            String keywords = data.getStringExtra(Constants.KEY_WORDS_NAME);
            if (keywords != null && !keywords.equals("")) {
                doSearchQuery(keywords);
            }
            input_text.setText(keywords);
            if (!keywords.equals("")) {
                clean_view.setVisibility(View.VISIBLE);
            }
            Log.d("item结果接收", keywords);
        }
        //显示框框
        listView.setVisibility(View.VISIBLE);
        //隐藏button
        go_style.setVisibility(View.GONE);
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keywords) {
        //显示进度框
        showProgressDialog();
        //第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keywords, "", currentCity);
        //设置每页最多返回多少条poiItem
        query.setPageSize(3);
        //设置查第一页
        query.setPageNum(1);
        //Poi查询设置
        poiSearch = new PoiSearch(getContext(), query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
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
                    poiItems = poiResult.getPois();
                    //获取第一页的数据
                    searchResultAdapter.setData(poiItems);
                    listView.setAdapter(searchResultAdapter);
                    // 取得第一页的poiItem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();
                    // 当搜索不到poiItem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        zoom = poiOverlay.zoomToSpan();
                        Log.d("地图等级为：", "" + zoom);
                        //我的位置点显示
                        try {
                            if (zoom < 13) {
                                mSmallIcon.setVisible(false);
                                mBigIcon.setVisible(true);
                            } else {
                                mSmallIcon.setVisible(true);
                                mBigIcon.setVisible(false);
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(getContext(), R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(getContext(), R.string.no_result);
            }
        } else {
            ToastUtil.showerror(getContext(), rCode);
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
     * @param tip
     */
    private void addTipMarker(Tip tip) {
        if (tip == null) {
            return;
        }
        mPoiMarker = aMap.addMarker(new MarkerOptions());
        LatLonPoint point = tip.getPoint();
        //list更新操作
        poiItems.clear();
        poiItems.add(new PoiItem("id", point, tip.getName(), tip.getAddress()));
        searchResultAdapter.setData(poiItems);
        listView.setAdapter(searchResultAdapter);
        if (point != null) {
            LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
            mPoiMarker.setPosition(markerPosition);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 17));
        }
        mPoiMarker.setTitle(tip.getName());
        mPoiMarker.setSnippet(tip.getAddress());
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
        Log.d("map Markers:",""+mapScreenMarkers.size());
        for (int i = 0; i < mapScreenMarkers.size(); i++) {
            Marker marker = mapScreenMarkers.get(i);
            if(marker.getObject()!=null) {
                if (!marker.getObject().toString().equals("location")) {
                    //移除当前Marker
                    marker.remove();
                }
            }else{
                //移除当前Marker
                marker.remove();
            }
        }
        //地图刷新
        aMap.runOnDrawFrame();
    }
}
