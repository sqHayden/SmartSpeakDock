package com.idx.smartspeakdock.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.idx.smartspeakdock.map.tools.MyPoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.idx.smartspeakdock.R;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends Activity implements DialogInterface.OnClickListener {

    //定义适配器
    private MySimpleAdapter mSimpleAdapter;
    //获取listview
    private ListView listView;
    //获取数据列表
    private ArrayList<Map<String, String>> list = new ArrayList<>();
    //利用Map存储各项数据
    private Map<String, String> map;
    //地图控件
    private MapView mMapView;
    //地图控制对象
    private BaiduMap mBaiduMap;
    //定位的监听器
    private MyLocationListener mLocationListener;
    //定位的客户端
    private LocationClient mLocationClient;
    //自定义当前位置图标
    private BitmapDescriptor bitmapDescriptor;
    //我的位置
    private LatLng my_latlng;
    //定位图标
    private ImageView my_location;
    //Poi搜索对象
    private PoiSearch mPoiSearch;
    //资源对象
    private EditText edit_key;
    private Button nearby_search, city_search;
    //当前城市名称
    private String city_name = "";
    //是否首次进入地图
    private boolean state;
    //搜索框
    private LinearLayout search_layout;
    //缩放级别
    double now_zoom, next_zoom;
    //搜索种类
    private int search_mode = 0;
    //存储poi结果集
    private PoiResult poiResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map_main);
        //初始化地图资源
        init();
        //初始化位置
        initLocation();
        //设置Poi检索监听
        mPoiSearch.setOnGetPoiSearchResultListener(resultListener);
        //设置button监听
        city_search.setOnClickListener(city_listener);
        nearby_search.setOnClickListener(nearby_listener);
        //给我的位置图标添加监听实现定位当前位置
        my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pointToLocation();
            }
        });
        //实现edit_key监听事件
        edit_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //设置缩放
                    set_map_size(my_latlng, 17);
                    //表示此时是附近搜索
                    search_mode = 1;
                    //清空覆盖物
                    clear_overlays();
                    //附近搜索方法
                    nearbySearch();
                    //关闭小键盘方法
                    closeKeyBoard(v);
                }
                return false;
            }
        });
        //点击地图隐藏marker属性
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                listView.setVisibility(View.GONE);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        //监听listView点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //显示点击时的中心点切换
                goto_item(poiResult, position);
            }
        });
    }

    //初始化地图初始资源
    private void init() {
        //获取地图控件引用
        mMapView = findViewById(R.id.bmapView);
        //获取BaiduMap对象
        mBaiduMap = mMapView.getMap();
        //隐藏百度地图下角标
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        //设置地图放大比例
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
        mBaiduMap.setMapStatus(msu);
        //创建Poi实例
        mPoiSearch = PoiSearch.newInstance();
        //获取Edit关键字
        edit_key = findViewById(R.id.edit_key);
        //获取搜索附近按钮
        nearby_search = findViewById(R.id.nearby_search);
        //获取搜索全市按钮
        city_search = findViewById(R.id.city_search);
        //获取定位按钮
        my_location = findViewById(R.id.my_location);
        //获取搜索框布局
        search_layout = findViewById(R.id.search_edit);
        //获取listView对象
        listView = findViewById(R.id.list_view);
        //设置首次进入
        state = true;
    }

    //位置初始化功能
    private void initLocation() {
        //定位初始化
        mLocationClient = new LocationClient(this);
        mLocationListener = new MyLocationListener();
        //设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        //设置坐标类型
        option.setCoorType("bd09ll");
        //打开GPS
        option.setOpenGps(true);
        //每隔一秒进行一次定位
        option.setScanSpan(1000);
        //应用
        mLocationClient.setLocOption(option);
        //注册回调监听器到定位客户端
        mLocationClient.registerLocationListener(mLocationListener);
        //自定义图标对象创建
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map_gpslocked);
    }

    //初始化适配器功能
    private void initAdapter() {
        //定义适配器
        mSimpleAdapter = new MySimpleAdapter(this, list, R.layout.map_item,
                new String[]{"place_name", "place_distance", "place_location"},
                new int[]{R.id.place_name, R.id.place_distance, R.id.place_location});
        listView.setAdapter(mSimpleAdapter);
        //显示视图
        listView.setVisibility(View.VISIBLE);
    }

    //定位到当前位置
    private void pointToLocation() {
        //设置缩放
        set_map_size(my_latlng, 17);
        //应用自定义图标显示
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, bitmapDescriptor);
        mBaiduMap.setMyLocationConfiguration(config);
        //搜索框显示
        search_layout.setVisibility(View.VISIBLE);
        //关闭首次进入
        state = false;
    }

    //Poi-->搜索附近 点击事件
    private View.OnClickListener nearby_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //关闭小键盘
            closeKeyBoard(v);
            //设置缩放
            set_map_size(my_latlng, 17);
            //表示此时是附近搜索
            search_mode = 1;
            //清空覆盖物
            clear_overlays();
            //附近搜索方法
            nearbySearch();
        }
    };

    //Poi-->全市搜索 点击事件
    private View.OnClickListener city_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //表示此时是全市搜索
            search_mode = 2;
            //清空覆盖物
            clear_overlays();
            //全市搜索方法
            citySearch();
        }
    };

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:            //点击“确定”进入登录界面
                Toast.makeText(MapActivity.this, "您点击了确定", Toast.LENGTH_SHORT).show();
                break;
            case DialogInterface.BUTTON_NEUTRAL:             //点击“取消”
                Toast.makeText(MapActivity.this, "您点击了取消", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        //释放Poi实例
        mPoiSearch.destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    /**
     * 城市内搜索
     */
    private void citySearch() {
        // 设置检索参数
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
        citySearchOption.city(city_name);// 城市
        citySearchOption.keyword(edit_key.getText().toString());// 关键字
        citySearchOption.pageCapacity(10);// 默认10条
        // 发起检索请求
        mPoiSearch.searchInCity(citySearchOption);
    }

    /**
     * 附近检索
     */
    private void nearbySearch() {
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.location(my_latlng);
        nearbySearchOption.keyword(edit_key.getText().toString());
        nearbySearchOption.radius(1000);// 检索半径，单位是米
        nearbySearchOption.pageCapacity(4);//附近4条
        mPoiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
    }

    //POI检索的监听对象
    OnGetPoiSearchResultListener resultListener = new OnGetPoiSearchResultListener() {
        //获得POI的检索结果，一般检索数据都是在这里获取
        @Override
        public void onGetPoiResult(PoiResult poi) {
            //保存poi结果集
            poiResult = poi;
            //如果搜索到的结果不为空，并且没有错误
            if (poiResult != null && poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
                //清空list
                list.clear();
                //将结果传入到详细方法中进行处理
                resultCheck(poiResult);
                //listView赋值操作
                initAdapter();
                //这传入search对象，因为一般搜索到后，点击时方便发出详细搜索
                MyPoiOverlay overlay = new MyPoiOverlay(mBaiduMap, mPoiSearch);
                //设置数据,这里只需要一步，
                overlay.setData(poiResult);
                //添加到地图
                overlay.addToMap();
                //将显示视图拉倒正好可以看到所有POI兴趣点的缩放等级
                overlay.zoomToSpan();//计算工具
                //将中心点设在第一个位置
                set_map_size(poiResult.getAllPoi().get(0).location, (int) mBaiduMap.getMapStatus().zoom);
//                if (search_mode == 2) {
//                    overlay.zoomToSpan();//计算工具
//                } else {
//                    //将中心点设在第一个位置
//                    set_map_size(poiResult.getAllPoi().get(0).location, 17);
//                }
                //设置标记物的点击监听事件
                mBaiduMap.setOnMarkerClickListener(overlay);
            } else {
                //清空list
                list.clear();
                Toast.makeText(getApplication(), "搜索不到你需要的信息！", Toast.LENGTH_SHORT).show();
            }
        }

        //获得POI的详细检索结果，如果发起的是详细检索，这个方法会得到回调(需要uid)
        //详细检索一般用于单个地点的搜索，比如搜索一大堆信息后，选择其中一个地点再使用详细检索
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getApplication(), "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            } else {// 正常返回结果的时候，此处可以获得很多相关信息
                Toast.makeText(getApplication(), poiDetailResult.getName() + ": "
                                + poiDetailResult.getAddress(),
                        Toast.LENGTH_LONG).show();
            }
        }

        //获得POI室内检索结果
        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    };

    //请求定位回调监听
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
//            Log.d("进入监听", "Test Log");
            my_latlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            //显示当前位置图标
            MyLocationData data = new MyLocationData
                    .Builder()
                    .accuracy(bdLocation.getRadius())
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(data);
            if (state) {
                //调整地图显示尺寸
                set_map_size(my_latlng, 15);
                //获取当前城市名称
                city_name = bdLocation.getCity();
                Log.d("当前城市为：", city_name);
            }
        }
    }

    //listItem点击转换坐标方法
    private void goto_item(PoiResult poiResult, int index) {
        //将中心点设在第index个位置
        set_map_size(poiResult.getAllPoi().get(index).location, (int) mBaiduMap.getMapStatus().zoom);
    }

    //清空所有覆盖物方法
    private void clear_overlays() {
        mBaiduMap.clear();
    }

    //地图尺寸调整方法
    /*
     *  p1:当前中心点位置
     *  p2:当前缩放级别
     * **/
    private void set_map_size(LatLng latLng, int size) {
        //调整地图显示尺寸
        /*
        * 尺寸示例：
        *    百度地图对应缩放级别
        *    int[] zoomLevel = { 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6,5, 4, 3 };
        *    对应级别单位
        *    String[] zoomLevelStr = { “10”, “20”, “50”, “100”, “200”, “500”, “1000”,
        *    “2000”, “5000”, “10000”, “20000”, “25000”, “50000”, “100000”,
        *    “200000”, “500000”, “1000000”, “2000000” }; 单位/m
        * **/
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(latLng)
                .zoom(size)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    //发起详细检索
    public boolean resultCheck(PoiResult poiResult) {
        //获取点击的标记物的数据,对于每一条发起一个详细检索
        for (int i = 0; i < poiResult.getAllPoi().size(); i++) {
            PoiInfo poiInfo = poiResult.getAllPoi().get(i);
            map = new HashMap<>();
            String name = "";
            if (poiInfo.address.contains("有轨电车")) {
                name = poiInfo.name + "(有轨电车)";
            } else if (poiInfo.address.contains("路;") && !poiInfo.name.contains("(公交站)")) {
                name = poiInfo.name + "(公交站)";
            } else {
                name = poiInfo.name;
            }
            //添加详情到list集合中
            map.put("place_name", name);
            //获取两点之间的距离
            map.put("place_distance", distanceUtils(DistanceUtil.getDistance(my_latlng,poiInfo.location)));
            map.put("place_location", poiInfo.address);
            list.add(map);
        }
        return true;
    }

    //隐藏键盘方法
    private void closeKeyBoard(View v){
        //关闭键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
    }

    /**
     * 两点间坐标单位转换
     * @param distance
     * @return 米
     */
    public String distanceUtils(double distance) {
        //大于1000m转换为km
        if (distance > 1000) {
            distance = distance / 1000;
            BigDecimal b = new BigDecimal(distance);
            double distance1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            return distance1 + "km";
        } else {
            return (int) distance + "m";
        }
    }

    class MySimpleAdapter extends SimpleAdapter {

        private ArrayList<Map<String, String>> listitem;
        //导航图标对象
        private ImageView img_point;

        public MySimpleAdapter(Context context, ArrayList<Map<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.listitem = data;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            //获取导航图片对象
            img_point = view.findViewById(R.id.img_point);
            img_point.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = position+1;
                    //创建一个AlertDialog对话框
                    Dialog dialog = new AlertDialog.Builder(MapActivity.this)
                            .setTitle("导航")
                            .setMessage("是否前往" + index+ "号标记点？")
                            .setPositiveButton("确定", MapActivity.this)
                            .setNegativeButton("取消", MapActivity.this)
                            .create();
                    dialog.show();
                }
            });
            return view;
        }
    }
}
