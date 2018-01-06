package com.idx.smartspeakdock.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
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
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.vi.VDeviceAPI;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMapVoiceListener;
import com.idx.smartspeakdock.map.overlayutil.BikingRouteOverlay;
import com.idx.smartspeakdock.map.overlayutil.DrivingRouteOverlay;
import com.idx.smartspeakdock.map.overlayutil.MassTransitRouteOverlay;
import com.idx.smartspeakdock.map.overlayutil.OverlayManager;
import com.idx.smartspeakdock.map.overlayutil.TransitRouteOverlay;
import com.idx.smartspeakdock.map.overlayutil.WalkingRouteOverlay;
import com.idx.smartspeakdock.map.tools.BNDemoGuideActivity;
import com.idx.smartspeakdock.map.tools.BNEventHandler;
import com.idx.smartspeakdock.map.tools.MyOrientationListener;
import com.idx.smartspeakdock.map.tools.MyPoiOverlay;
import com.idx.smartspeakdock.map.tools.RouteLineAdapter;
import com.idx.smartspeakdock.service.SplachService;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MapFragment extends BaseFragment implements DialogInterface.OnClickListener, OnGetRoutePlanResultListener, BaiduMap.OnMapClickListener {
   public static final String TAG = MapFragment.class.getSimpleName();
    //fragment相关
    private View view;
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
    private LatLng my_reallatlng;
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
    //指定地点搜索框
    private LinearLayout search_layout;
    //点对点搜索框
    private LinearLayout ptp_layout;
    //缩放级别
    double now_zoom, next_zoom;
    //存储poi结果集
    private PoiResult poiResult;
    //传感器对象
    private MyOrientationListener myOrientationListener;
    //旋转角度值
    private float currentX;
    //范围距离值
    private float accu;
    //路线选择按钮
    private Button chooseRoute;
    //出行方式按钮
    private FloatingActionButton fab;
    private LinearLayout fab_parent;

    //导航起始点及终点定义
    private LatLng toLocationData;
    public static List<Activity> activityList = new LinkedList<Activity>();

    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";

    private Button mWgsNaviBtn = null;
    private Button mGcjNaviBtn = null;
    private Button mBdmcNaviBtn = null;
    private Button mDb06ll = null;
    private String mSDCardPath = null;

    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
    public static final String RESET_END_NODE = "resetEndNode";
    public static final String VOID_MODE = "voidMode";

    private final static String authBaseArr[] =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private final static String authComArr[] = {Manifest.permission.READ_PHONE_STATE};
    private final static int authBaseRequestCode = 1;
    private final static int authComRequestCode = 2;

    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;

    // 搜索相关
    // 浏览路线节点相关
//    Button mBtnPre = null; // 上一个节点
//    Button mBtnNext = null; // 下一个节点
//    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;
    MassTransitRouteLine massroute = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    private TextView popupText = null; // 泡泡view

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    // 搜索相关
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    WalkingRouteResult nowResultwalk = null;
    BikingRouteResult nowResultbike = null;
    TransitRouteResult nowResultransit = null;
    DrivingRouteResult nowResultdrive = null;
    MassTransitRouteResult nowResultmass = null;
    //    private LatLng start = new LatLng(22.671406,114.052511);
//    private LatLng end = new LatLng(22.660850629358585,114.04567852765966);
    int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。
    String startNodeStr = "西二旗地铁站";
    String endNodeStr = "百度科技园";
    boolean hasShownDialogue = false;

    //路径选择相关
    private EditText route_start, route_end;
    private LatLng start_llg, end_llg;
    private LinearLayout go_style;
    private Button point_start, point_end;
    private LinearLayout map_go;
    //是否是路径位置地点查询
    private boolean start_flag;
    //是否是起点查询
    private boolean isstart;
    //显示查询等待
    private ProgressDialog mDialog;

    //路线相关信息
    private TextView spend_time,distance_count,traffic_count;
    private SwipeActivity.MyOnTouchListener onTouchListener;

    //语音交互模块参数
    String my_address = "未查询到地址";

    public static MapFragment newInstance(){return new MapFragment();}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        activityList.add(getActivity());
        Handler h = new Handler();
        h.postDelayed(new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                Log.i("crug", name);
                delayTest();
            }
        }, 500);
        BNOuterLogUtil.setLogSwitcher(true);
        //初始化导航相关
        if (initDirs()) {
            initNavi();
        }
        getContext().startService(new Intent(getContext().getApplicationContext(), SplachService.class));

        UnitManager.getInstance().setMapVoiceListener(new IMapVoiceListener() {
            //语句：打開地图/我在哪兒/這是哪裡/這是哪兒/我現在在哪裡  测试完成 可以实现
            @Override
            public String onLocationInfo() {
                //执行地点查询方案
                Log.d(TAG, "onLocationInfo: ");
                return my_address;
            }
            //语句：搜索(市/县/区)  /   搜索附近[NAME]   需要修改skill
            @Override
            public String onSearchInfo(String name, SearchArea searchArea) {
                Log.d(TAG, "onSearchInfo: ");
                Log.d("key:"+name,"area"+searchArea.getDesc());
                return null;
            }
            //语句：我要搜索[NAME]
            @Override
            public String onSearchAddress(String address) {
                return null;
            }
            //语句：我要从哪儿到哪儿  可以了
            //语句：我要从哪儿驾车去哪儿 可以了
            //语句：我要去哪儿   语音代码修改中
            @Override
            public String onPathInfo(String fromAddress, String toAddress, PathWay pathWay) {
                Log.d("出行方式:",pathWay.toString());
                Log.d(TAG, "onPathInfo:");
                //调用我的位置
                pointToLocation();
                //隐藏路径选择视图
                fab_parent.setVisibility(View.GONE);
                //显示点对点视图
                ptp_layout.setVisibility(View.VISIBLE);
                //设置起点名字
                if(fromAddress!=null){//说了出发点
                    route_start.setText(fromAddress);
                }else{//没说按照“我的位置处理”
                    route_start.setText("我的位置");
                    start_llg = my_latlng;
                }
                //设置终点名字
                route_end.setText(toAddress);
                return "已为您进行查询，请在地图中选择精确的始末地点，启动路径导航操作";
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_main,container,false);
        //初始化地图资源
        init(view);
        //初始化位置
        initLocation();
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
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
                    //非空状态下的搜索
                    if(!edit_key.getText().toString().trim().equals("")) {
                        //关闭路径标志
                        start_flag = false;
                        //隐藏出行图标
                        fab_parent.setVisibility(View.GONE);
                        //失去焦点
                        close_point(edit_key);
                        //设置缩放
                        set_map_size(my_latlng, 17);
                        //清空覆盖物
                        clear_overlays();
                        //附近搜索方法
                        nearbySearch();
                    }else{
                        Toast.makeText(getContext(),"关键字不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
        //实现edit_key的点击监听
        edit_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取焦点
                get_point(edit_key);
            }
        });
        //点击地图隐藏marker属性
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //点击地图失去edit_key的焦点
                close_point(edit_key);
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
                Log.d("item列表项被点击了", "Test Demo");
                //失去焦点
                route_end.clearFocus();
                route_end.setFocusable(false);
                //显示点击时的中心点切换
                goto_item(poiResult, position);
            }
        });
        //监听路径选择事件
        chooseRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "正在为您启用导航，请稍后", Toast.LENGTH_SHORT).show();
                mDialog = null;
                //显示等待
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = ProgressDialog.show(getContext(), "", "正在获取中...");
                        Log.d("Test Demo", "进入缓冲开启方法");
                    }
                });
                //启用导航功能
                if (BaiduNaviManager.isNaviInited()) {
                    Log.d("准备进入导航方法", "Test Demo");
                    routeplanToNavi(BNRoutePlanNode.CoordinateType.WGS84);
                } else {
                    Log.d("地图功能没有准备好", "Test Demo");
                }
            }
        });
        //监听出行方式点击事件
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏关键搜索视图
                search_layout.setVisibility(View.GONE);
                //导航隐藏
                map_go.setVisibility(View.GONE);
                //自身显示隐藏
                fab_parent.setVisibility(View.GONE);
                //显示路径搜索视图
                ptp_layout.setVisibility(View.VISIBLE);
            }
        });
        //实现起点键盘搜索监听事件
        route_start.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    isstart = true;
                    //失去焦点
                    close_point(route_start);
                    if (route_start.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "起始点不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (route_start.getText().toString().equals("我的位置")) {
                            start_llg = my_latlng;
                        } else {
                            choose_location(route_start, v);
                        }
                    }
                }
                return false;
            }
        });
        //实现起点的焦点获取点击监听
        route_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取焦点
                get_point(route_start);
            }
        });
        //设置起点选择按钮监听
        point_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isstart = true;
                if (route_start.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "起始点不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (route_start.getText().toString().equals("我的位置")) {
                        start_llg = my_latlng;
                    } else {
                        choose_location(route_start, v);
                    }
                }
            }
        });
        //实现终点选择监听事件
        route_end.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    isstart = false;
                    //实现终点的焦点失去
                    close_point(route_end);
                    //开始规划路径
                    //出发点处理：如果没动第一项，设置为我的位置
                    if (route_start.getText().toString().equals("我的位置")) {
                        start_llg = my_latlng;
                        if (route_end.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "终点不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            choose_location(route_end, v);
                        }
                    } else {//如果动了查看是否是空的
                        if (route_start.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "起始点不能为空", Toast.LENGTH_SHORT).show();
                        } else {//如果动了不是空的看是否定位了
                            if (start_llg == null) {
                                Toast.makeText(getContext(), "您还未选择出发点位置", Toast.LENGTH_SHORT).show();
                            } else {//如果起始点定位了处理目标点
                                //目标点处理：如果为空提示
                                if (route_end.getText().toString().isEmpty()) {
                                    Toast.makeText(getContext(), "终点不能为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    choose_location(route_end, v);
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });
        //设置终点选择按钮监听
        point_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!route_end.getText().toString().isEmpty()) {
                    isstart = false;
                    //终点的
                    //开始规划路径
                    //出发点处理：如果没动第一项，设置为我的位置
                    if (route_start.getText().toString().equals("我的位置")) {
                        start_llg = my_latlng;
                        choose_location(route_end, v);
                    } else {//如果动了查看是否是空的
                        if (route_start.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "起始点不能为空", Toast.LENGTH_SHORT).show();
                        } else {//如果动了不是空的看是否定位了
                            if (start_llg == null) {
                                Toast.makeText(getContext(), "您还未选择出发点位置", Toast.LENGTH_SHORT).show();
                            } else {//如果起始点定位了处理目标点
                                //目标点处理：如果为空提示
                                if (route_end.getText().toString().isEmpty()) {
                                    Toast.makeText(getContext(), "终点不能为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    choose_location(route_end, v);
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "请输入终点位置", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //实现终点的焦点获取点击监听
        route_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取焦点
                get_point(route_end);
            }
        });
        onTouchListener = new SwipeActivity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        getContext().stopService(new Intent(getContext().getApplicationContext(), SplachService.class));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        getContext().startService(new Intent(getContext().getApplicationContext(), SplachService.class));
                        break;
                }
                return false;
            }
        };
        ((SwipeActivity) getActivity()).registerMyOnTouchListener(onTouchListener);
        return view;
    }

    //初始化地图初始资源
    private void init(View view) {
        //获取地图控件引用
        mMapView = view.findViewById(R.id.bmapView);
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
        edit_key = view.findViewById(R.id.edit_key);
        //获取搜索附近按钮
        nearby_search = view.findViewById(R.id.nearby_search);
        //获取搜索全市按钮
        city_search = view.findViewById(R.id.city_search);
        //获取定位按钮
        my_location = view.findViewById(R.id.my_location);
        //获取路径搜索按钮
        fab = view.findViewById(R.id.fab);
        fab_parent = view.findViewById(R.id.fab_parent);
        //获取起点终点位置定位按钮
        point_start = view.findViewById(R.id.route_start_location);
        point_end = view.findViewById(R.id.route_end_location);
        //获取搜索框布局
        search_layout = view.findViewById(R.id.search_edit);
        //获取listView对象
        listView = view.findViewById(R.id.list_view);
        //获取路线选择按钮对象
        chooseRoute = view.findViewById(R.id.choose_route);
        //获取出行方式相关
        route_start = view.findViewById(R.id.route_start);
        route_end = view.findViewById(R.id.route_end);
        ptp_layout = view.findViewById(R.id.ptp_layout);
        go_style = view.findViewById(R.id.go_style);
        map_go = view.findViewById(R.id.map_go);
        //获取导航按钮中相关信息设置文本框
        spend_time = view.findViewById(R.id.spend_time);
        distance_count = view.findViewById(R.id.distance_count);
        traffic_count = view.findViewById(R.id.traffic_count);
        //设置首次进入
        state = true;
        start_flag = false;
        mDialog = null;
    }

    //位置初始化功能
    private void initLocation() {
        //定位初始化
        mLocationClient = new LocationClient(getContext());
        mLocationListener = new MyLocationListener();
        //设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        //打开GPS
        option.setOpenGps(true);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(3000);
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
        //注册回调监听器到定位客户端
        mLocationClient.registerLocationListener(mLocationListener);
        //自定义图标对象创建
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map_gpslocked);
        //传感器对象创建
        myOrientationListener = new MyOrientationListener(getContext());
        //传感器改变方向回调
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                currentX = x;
                if (my_latlng != null) {
                    //动态改变地图的图标方向
                    MyLocationData data = new MyLocationData
                            .Builder()
                            .accuracy(accu)
                            .direction(currentX)
                            .latitude(my_latlng.latitude)
                            .longitude(my_latlng.longitude).build();
                    mBaiduMap.setMyLocationData(data);
                }
            }
        });
    }

    //初始化适配器功能
    private void initAdapter() {
        //定义适配器
        mSimpleAdapter = new MySimpleAdapter(getContext(), list, R.layout.map_item,
                new String[]{"place_name", "place_distance", "place_location"},
                new int[]{R.id.place_name, R.id.place_distance, R.id.place_location});
        listView.setAdapter(mSimpleAdapter);
        //显示视图
        listView.setVisibility(View.VISIBLE);
    }

    //定位到当前位置
    private void pointToLocation() {
        //清除绘制物
        //设置缩放
        set_map_size(my_reallatlng, 17);
        //应用自定义图标显示
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, bitmapDescriptor);
        mBaiduMap.setMyLocationConfiguration(config);
        //显示当前位置图标
        MyLocationData data = new MyLocationData
                .Builder()
                .accuracy(400)
                .latitude(my_reallatlng.latitude)
                .longitude(my_reallatlng.longitude).build();
        mBaiduMap.setMyLocationData(data);
        //搜索框显示
        search_layout.setVisibility(View.VISIBLE);
        //路径框隐藏
        ptp_layout.setVisibility(View.GONE);
        //导航隐藏
        map_go.setVisibility(View.GONE);
        if (listView.getVisibility() == View.GONE) {
            //浮动图标显示
            fab_parent.setVisibility(View.VISIBLE);
        }
        //关闭首次进入
        state = false;
    }

    //Poi-->搜索附近 点击事件
    private View.OnClickListener nearby_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //附近搜索方法
            if(!edit_key.getText().toString().trim().equals("")) {
                //关闭路径标志
                start_flag = false;
                //隐藏出行图标
                fab_parent.setVisibility(View.GONE);
                //失去焦点
                close_point(edit_key);
                //设置缩放
                set_map_size(my_latlng, 17);
                //清空覆盖物
                clear_overlays();
                //执行搜索动作
                nearbySearch();
            }else{
                Toast.makeText(getContext(),"关键字不能为空", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //Poi-->全市搜索 点击事件
    private View.OnClickListener city_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!edit_key.getText().toString().trim().equals("")) {
                //关闭路径标志
                start_flag = false;
                //隐藏出行图标
                fab_parent.setVisibility(View.GONE);
                //失去焦点
                close_point(edit_key);
                //清空覆盖物
                clear_overlays();
                //全市搜索方法
                citySearch();
            }else{
                Toast.makeText(getContext(),"关键字不能为空", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:            //点击“确定”进入登录界面
                Toast.makeText(getContext(), "您点击了确定", Toast.LENGTH_SHORT).show();
                //设置列表隐藏
                listView.setVisibility(View.GONE);
                //重新规划线路
                clear_overlays();
                PlanNode stNode = PlanNode.withLocation(my_latlng);
                PlanNode enNode = PlanNode.withLocation(toLocationData);

                //进入驾车路径规划生成
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(stNode).to(enNode));
                nowSearchType = 1;
                //显示开始导航按钮
                map_go.setVisibility(View.VISIBLE);
                break;
            case DialogInterface.BUTTON_NEUTRAL:             //点击“取消”
                Toast.makeText(getContext(), "您点击了取消", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        //开启传感器
        myOrientationListener.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        getContext().startService(new Intent(getContext().getApplicationContext(), SplachService.class));

    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        Log.d("Test Demo()", "执行了onPause()方法");
        if (mDialog != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        Log.d("Test Demo:", "悬浮窗取消操作");
                    }
                }
            });
            //隐藏导航按钮
            map_go.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //停止方向传感器
        myOrientationListener.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        //释放Poi实例
        mPoiSearch.destroy();
        //释放路径搜索实例
        if (mSearch != null) {
            mSearch.destroy();
        }
        VDeviceAPI.unsetNetworkChangedCallback();
        getContext().stopService(new Intent(getContext().getApplicationContext(), SplachService.class));
        ((SwipeActivity) getActivity()).unregisterMyOnTouchListener(onTouchListener);
    }


    /**
     * 城市内搜索
     */
    private void citySearch() {
        // 设置检索参数
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
        citySearchOption.city(city_name);// 城市
        citySearchOption.keyword(edit_key.getText().toString());// 关键字
        Log.d("城市为:" + city_name, "关键字是：" + edit_key.getText().toString());
        if (start_flag) {
            citySearchOption.pageCapacity(5);// 默认10条
        } else {
            citySearchOption.pageCapacity(10);// 默认10条
        }
        // 发起检索请求
        mPoiSearch.searchInCity(citySearchOption);
    }

    /**
     * 附近检索
     */
    private void nearbySearch() {
        Log.d("进入了附近检索方法","Test Demo");
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
            Log.d("Test Demo:", "进入了回调");
            //保存poi结果集
            poiResult = poi;
            //如果搜索到的结果不为空，并且没有错误
            if (poiResult != null && poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
                Log.d("Test Demo:", "进入检索方法");
                //清空list
                list.clear();
                //将结果传入到详细方法中进行处理
                resultCheck(poiResult);
                //listView赋值操作
                initAdapter();
                //这传入search对象，因为一般搜索到后，点击时方便发出详细搜索
                MyPoiOverlay overlay = new MyPoiOverlay(mBaiduMap, mPoiSearch);
                //设置数据,这里只需要一\步，
                overlay.setData(poiResult);
                //添加到地图
                overlay.addToMap();
                //将显示视图拉倒正好可以看到所有POI兴趣点的缩放等级
                overlay.zoomToSpan();//计算工具
                //将中心点设在第一个位置
//                set_map_size(poiResult.getAllPoi().get(0).location, (int) mBaiduMap.getMapStatus().zoom);
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
                Toast.makeText(getContext(), "搜索不到你需要的信息！", Toast.LENGTH_SHORT).show();
            }
        }

        //获得POI的详细检索结果，如果发起的是详细检索，这个方法会得到回调(需要uid)
        //详细检索一般用于单个地点的搜索，比如搜索一大堆信息后，选择其中一个地点再使用详细检索
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getContext(), "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            } else {// 正常返回结果的时候，此处可以获得很多相关信息
                Toast.makeText(getContext(), poiDetailResult.getName() + ": "
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
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                my_latlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                StringBuffer sb = new StringBuffer(256);
                if (state) {
                    sb.append(bdLocation.getProvince());    //获取省份
                    sb.append(bdLocation.getCity());   //获取城市
                    sb.append(bdLocation.getDistrict());//获取区县
                    sb.append(bdLocation.getStreet()); //获取街道信息
                    Log.d("街道信息:",bdLocation.getStreet());
                    sb.append(bdLocation.getLocationDescribe());//位置描述信息
                    Log.d("位置描述信息:",bdLocation.getLocationDescribe());

                    Log.d("我的位置为:",sb.toString());
                    if(my_address.equals("未查询到地址")) {
                        my_address = sb.toString();
                    }
                    Log.d("定位方式:",""+bdLocation.getNetworkLocationType());
                    if(bdLocation.hasAddr()){
                        Log.d("有地址信息","abdce");
                    }
                    my_reallatlng = my_latlng;
                    accu = bdLocation.getRadius();
                    //显示当前位置图标
                    MyLocationData data = new MyLocationData
                            .Builder()
                            .accuracy(accu)
                            .latitude(my_reallatlng.latitude)
                            .longitude(my_reallatlng.longitude).build();
                    mBaiduMap.setMyLocationData(data);
                    //调整地图显示尺寸
                    set_map_size(my_latlng, 15);
                    //获取当前城市名称
                    city_name = bdLocation.getCity();
//                Log.d("当前城市为：", city_name);
                }
            }
        }
    }

    //listItem点击转换坐标方法
    private void goto_item(PoiResult poiResult, int index) {
        //设置起点坐标值
        if (start_flag) {
            if (isstart) {
                start_llg = poiResult.getAllPoi().get(index).location;
            } else {
                end_llg = poiResult.getAllPoi().get(index).location;
                //设置列表隐藏
                listView.setVisibility(View.GONE);
                Toast.makeText(getContext(), "正在为您规划路径，请稍后", Toast.LENGTH_SHORT).show();
                //重新规划线路
                clear_overlays();
                PlanNode stNode = PlanNode.withLocation(start_llg);
                PlanNode enNode = PlanNode.withLocation(end_llg);
                //进入驾车路径规划生成
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(stNode).to(enNode));
                nowSearchType = 1;
                //显示开始导航按钮
                map_go.setVisibility(View.VISIBLE);
            }
            listView.setVisibility(View.GONE);
        } else {
            //将中心点设在第index个位置
            set_map_size(poiResult.getAllPoi().get(index).location, 19);
        }
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
            map.put("place_distance", distanceUtils(DistanceUtil.getDistance(my_latlng, poiInfo.location)));
            map.put("place_location", poiInfo.address);
            list.add(map);
        }
        return true;
    }

    /**
     * 两点间坐标单位转换
     *
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
        private LinearLayout goto_there;

        public MySimpleAdapter(Context context, ArrayList<Map<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.listitem = data;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            //获取导航图片对象
            img_point = view.findViewById(R.id.img_point);
            goto_there = view.findViewById(R.id.goto_there);
            if (start_flag) {
                //隐藏图片
                goto_there.setVisibility(View.GONE);
            } else {
                img_point.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //保存查看的地点当做预备的目的地
                        toLocationData = poiResult.getAllPoi().get(position).location;
                        int index = position + 1;
                        //创建一个AlertDialog对话框
                        Dialog dialog = new AlertDialog.Builder(getContext())
                                .setTitle("导航")
                                .setMessage("是否前往" + index + "号标记点？")
                                .setPositiveButton("确定", MapFragment.this)
                                .setNegativeButton("取消", MapFragment.this)
                                .create();
                        dialog.show();
                    }
                });
            }
            return view;
        }
    }

    //导航相关

    public void delayTest() {

    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    public void showToastMsg(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasBasePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getActivity().getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getActivity().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getActivity().getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getActivity().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }

        BaiduNaviManager.getInstance().init(getActivity(), mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getContext(), authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(getContext(), "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                Toast.makeText(getContext(), "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(getContext(), "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private BNRoutePlanNode.CoordinateType mCoordinateType = null;

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        Log.d("进入导航方法了", "Test Demo");
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(getContext(), "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(getContext(), "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
//        BNRoutePlanNode sNode = new BNRoutePlanNode(114.052481,22.671418, "我的起点", null, coType);
//        BNRoutePlanNode eNode = new BNRoutePlanNode(114.04567852765966,22.660850629358585, "我的终点", null, coType);
        if (start_flag) {
            //点对点语音
            sNode = new BNRoutePlanNode(start_llg.longitude, start_llg.latitude, "我的起点", null, coType);
            eNode = new BNRoutePlanNode(end_llg.longitude, end_llg.latitude, "我的终点", null, coType);
        } else {
            //默认语音
            sNode = new BNRoutePlanNode(my_latlng.longitude, my_latlng.latitude, "我的起点", null, coType);
            eNode = new BNRoutePlanNode(toLocationData.longitude, toLocationData.latitude, "我的终点", null, coType);
        }

//        switch (coType) {
//            case GCJ02: {
//                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
//                break;
//            }
//            case WGS84: {
//                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
//                break;
//            }
//            case BD09_MC: {
//                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
//                break;
//            }
//            case BD09LL: {
//                sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
//                eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
//                break;
//            }
//            default:
//                ;
//        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            // 开发者可以使用旧的算路接口，也可以使用新的算路接口,可以接收诱导信息等
            // BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
            BaiduNaviManager.getInstance().launchNavigator(getActivity(), list, 1, false, new DemoRoutePlanListener(sNode),
                    eventListerner);
        }
    }

    BaiduNaviManager.NavEventListener eventListerner = new BaiduNaviManager.NavEventListener() {

        @Override
        public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
            BNEventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
        }
    };

    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

                    return;
                }
            }
            Intent intent = new Intent(getContext(), BNDemoGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(getContext(), "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSetting() {
        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        BNaviSettingManager.setIsAutoQuitWhenArrived(true);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "10636332");
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

        @Override
        public void stopTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "stopTTS");
        }

        @Override
        public void resumeTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "resumeTTS");
        }

        @Override
        public void releaseTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "releaseTTSPlayer");
        }

        @Override
        public int playTTSText(String speech, int bPreempt) {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

            return 1;
        }

        @Override
        public void phoneHangUp() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneHangUp");
        }

        @Override
        public void phoneCalling() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneCalling");
        }

        @Override
        public void pauseTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "pauseTTS");
        }

        @Override
        public void initTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "initTTSPlayer");
        }

        @Override
        public int getTTSState() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "getTTSState");
            return 1;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(getContext(), "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            routeplanToNavi(mCoordinateType);
        }
    }


    //路径规划开始

    /**
     * 发起路线规划搜索示例
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        route = null;
//        mBtnPre.setVisibility(View.INVISIBLE);
//        mBtnNext.setVisibility(View.INVISIBLE);
        mBaiduMap.clear();
        // 处理搜索按钮响应
        // 设置起终点信息，对于tranist search 来说，城市名无意义
//        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", startNodeStr);
//        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", endNodeStr);
        PlanNode stNode = PlanNode.withLocation(my_latlng);
        PlanNode enNode = PlanNode.withLocation(toLocationData);
        // 实际使用中请对起点终点城市进行正确的设定

        if (v.getId() == R.id.mass) {
            PlanNode stMassNode = PlanNode.withCityNameAndPlaceName("北京", "天安门");
            PlanNode enMassNode = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");

            mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stMassNode).to(enMassNode));
            nowSearchType = 0;
        } else if (v.getId() == R.id.drive) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode).to(enNode));
            nowSearchType = 1;
        } else if (v.getId() == R.id.transit) {
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode).city("北京").to(enNode));
            nowSearchType = 2;
        } else if (v.getId() == R.id.walk) {
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode).to(enNode));
            nowSearchType = 3;
        } else if (v.getId() == R.id.bike) {
            mSearch.bikingSearch((new BikingRoutePlanOption())
                    .from(stNode).to(enNode));
            nowSearchType = 4;
        }
    }

    /**
     * 节点浏览示例
     *
     * @param v
     */
//    public void nodeClick(View v) {
//        LatLng nodeLocation = null;
//        String nodeTitle = null;
//        Object step = null;
//
//        if (nowSearchType != 0 && nowSearchType != -1) {
//            // 非跨城综合交通
//            if (route == null || route.getAllStep() == null) {
//                return;
//            }
//            if (nodeIndex == -1 && v.getId() == R.id.pre) {
//                return;
//            }
//            // 设置节点索引
//            if (v.getId() == R.id.next) {
//                if (nodeIndex < route.getAllStep().size() - 1) {
//                    nodeIndex++;
//                } else {
//                    return;
//                }
//            } else if (v.getId() == R.id.pre) {
//                if (nodeIndex > 0) {
//                    nodeIndex--;
//                } else {
//                    return;
//                }
//            }
//            // 获取节结果信息
//            step = route.getAllStep().get(nodeIndex);
//            if (step instanceof DrivingRouteLine.DrivingStep) {
//                nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
//                nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
//            } else if (step instanceof WalkingRouteLine.WalkingStep) {
//                nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
//                nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
//            } else if (step instanceof TransitRouteLine.TransitStep) {
//                nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
//                nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
//            } else if (step instanceof BikingRouteLine.BikingStep) {
//                nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
//                nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
//            }
//        } else if (nowSearchType == 0) {
//            // 跨城综合交通  综合跨城公交的结果判断方式不一样
//
//
//            if (massroute == null || massroute.getNewSteps() == null) {
//                return;
//            }
//            if (nodeIndex == -1 && v.getId() == R.id.pre) {
//                return;
//            }
//            boolean isSamecity = nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId();
//            int size = 0;
//            if (isSamecity) {
//                size = massroute.getNewSteps().size();
//            } else {
//                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
//                    size += massroute.getNewSteps().get(i).size();
//                }
//            }
//
//            // 设置节点索引
//            if (v.getId() == R.id.next) {
//                if (nodeIndex < size - 1) {
//                    nodeIndex++;
//                } else {
//                    return;
//                }
//            } else if (v.getId() == R.id.pre) {
//                if (nodeIndex > 0) {
//                    nodeIndex--;
//                } else {
//                    return;
//                }
//            }
//            if (isSamecity) {
//                // 同城
//                step = massroute.getNewSteps().get(nodeIndex).get(0);
//            } else {
//                // 跨城
//                int num = 0;
//                for (int j = 0; j < massroute.getNewSteps().size(); j++) {
//                    num += massroute.getNewSteps().get(j).size();
//                    if (nodeIndex - num < 0) {
//                        int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
//                        step = massroute.getNewSteps().get(j).get(k);
//                        break;
//                    }
//                }
//            }
//
//            nodeLocation = ((MassTransitRouteLine.TransitStep) step).getStartLocation();
//            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
//        }
//
//        if (nodeLocation == null || nodeTitle == null) {
//            return;
//        }
//
//        // 移动节点至中心
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
//        // show popup
//        popupText = new TextView(MainActivity.this);
//        popupText.setBackgroundResource(R.drawable.popup);
//        popupText.setTextColor(0xFF000000);
//        popupText.setText(nodeTitle);
//        mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
//    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (routeOverlay == null) {
            return;
        }
        if (useDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(getContext(),
                    "将使用系统起终点图标",
                    Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(getContext(),
                    "将使用自定义起终点图标",
                    Toast.LENGTH_SHORT).show();

        }
        useDefaultIcon = !useDefaultIcon;
        routeOverlay.removeFromMap();
        routeOverlay.addToMap();
    }
    
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);
            if (result.getRouteLines().size() > 1) {
                nowResultwalk = result;
                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(getContext(),
                            result.getRouteLines(),
                            RouteLineAdapter.Type.WALKING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            route = nowResultwalk.getRouteLines().get(position);
                            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                            mBaiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultwalk.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                route = result.getRouteLines().get(0);
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);
            if (result.getRouteLines().size() > 1) {
                nowResultransit = result;
                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(getContext(),
                            result.getRouteLines(),
                            RouteLineAdapter.Type.TRANSIT_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {

                            route = nowResultransit.getRouteLines().get(position);
                            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
                            mBaiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultransit.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                route = result.getRouteLines().get(0);
                TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.d("route result", "结果数<0");
                return;
            }
        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点模糊，获取建议列表
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nowResultmass = result;
//
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            if (!hasShownDialogue) {
                // 列表选择
                MyTransitDlg myTransitDlg = new MyTransitDlg(getContext(),
                        result.getRouteLines(),
                        RouteLineAdapter.Type.MASS_TRANSIT_ROUTE);
                nowResultmass = result;
                myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        hasShownDialogue = false;
                    }
                });
                myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                    public void onItemClick(int position) {

                        MyMassTransitRouteOverlay overlay = new MyMassTransitRouteOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay);
                        routeOverlay = overlay;
                        massroute = nowResultmass.getRouteLines().get(position);
                        overlay.setData(nowResultmass.getRouteLines().get(position));

                        MassTransitRouteLine line = nowResultmass.getRouteLines().get(position);
                        overlay.setData(line);
                        if (nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId()) {
                            // 同城
                            overlay.setSameCity(true);
                        } else {
                            // 跨城
                            overlay.setSameCity(false);
                        }
                        mBaiduMap.clear();
                        overlay.addToMap();
                        overlay.zoomToSpan();
                    }

                });
                myTransitDlg.show();
                hasShownDialogue = true;
            }
        }
    }


    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            if (result.getRouteLines().size() > 1) {
//                nowResultdrive = result;
//                if (!hasShownDialogue) {
//                    MyTransitDlg myTransitDlg = new MyTransitDlg(MainActivity.this,
//                            result.getRouteLines(),
//                            RouteLineAdapter.Type.DRIVING_ROUTE);
//                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            hasShownDialogue = false;
//                        }
//                    });
//                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
//                        public void onItemClick(int position) {
//                            route = nowResultdrive.getRouteLines().get(position);
//                            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
//                            mBaiduMap.setOnMarkerClickListener(overlay);
//                            routeOverlay = overlay;
//                            overlay.setData(nowResultdrive.getRouteLines().get(position));
//                            overlay.addToMap();
//                            overlay.zoomToSpan();
//                        }
//
//                    });
//                    myTransitDlg.show();
//                    hasShownDialogue = true;
//                }
//            } else if (result.getRouteLines().size() == 1) {
            DrivingRouteLine dr = result.getRouteLines().get(0);
            //获取规划路径距离
            int distance = dr.getDistance();
            Log.d("距离为：",""+distance);
            if(distance>1000){
                double dis = (double)dr.getDistance()/1000;
                BigDecimal b = new BigDecimal(dis);
                double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                distance_count.setText("路线总长度："+f1+"公里");
            }else {
                distance_count.setText("路线总长度：" + dr.getDistance() + "米");
            }
            //获取规划路径红绿灯数量
            traffic_count.setText( "红绿灯数：" +dr.getLightNum()+"个");
            //所需时间
            int time = dr.getDuration();
            if ( time / 3600 == 0 ) {
                spend_time.setText( "大约需要：" + time / 60 + "分钟" );
            } else {
                spend_time.setText( "大约需要：" + time / 3600 + "小时" + (time % 3600) / 60 + "分钟" );
            }
            RouteNode start = dr.getStarting();
            RouteNode end = dr.getTerminal();
            if (!start.equals("我的位置")) {
                my_latlng = start.getLocation();
            }
            toLocationData = end.getLocation();
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
//                mBtnPre.setVisibility(View.VISIBLE);
//                mBtnNext.setVisibility(View.VISIBLE);
        } else {
            Log.d("route result", "结果数<0");
            return;
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 1) {
                nowResultbike = result;
                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(getContext(),
                            result.getRouteLines(),
                            RouteLineAdapter.Type.DRIVING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            route = nowResultbike.getRouteLines().get(position);
                            BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaiduMap);
                            mBaiduMap.setOnMarkerClickListener(overlay);
                            routeOverlay = overlay;
                            overlay.setData(nowResultbike.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                route = result.getRouteLines().get(0);
                BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaiduMap);
                routeOverlay = overlay;
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
//                mBtnPre.setVisibility(View.VISIBLE);
//                mBtnNext.setVisibility(View.VISIBLE);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    // 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_en);
            }
            return null;
        }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_en);
            }
            return null;
        }


    }

    private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {
        public MyMassTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.map_icon_en);
            }
            return null;
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaiduMap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
                type) {
            this(context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        public void setOnDismissListener(OnDismissListener listener) {
            super.setOnDismissListener(listener);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.map_activity_transit_dialog);

            transitRouteList = (ListView) view.findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick(position);
//                    mBtnPre.setVisibility(View.VISIBLE);
//                    mBtnNext.setVisibility(View.VISIBLE);
                    dismiss();
                    hasShownDialogue = false;
                }
            });
        }

        public void setOnItemInDlgClickLinster(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }
    }

    //供路径选择的方法
    private void choose_location(EditText route, View v) {
        //设置标量
        start_flag = true;
        //隐藏导航的
        map_go.setVisibility(View.GONE);
        //获取文本点输入
        String place_name = route.getText().toString();
        String place_list[] = null;
        if (place_name.contains("市")) {
            //以“市”作为分割
            place_list = place_name.split("市");
            city_name = place_list[0];
            //重新赋值关键字
            edit_key.setText(place_list[1]);
        } else {
            edit_key.setText(place_name);
        }
        citySearch();
    }

    //供editview失去焦点的方法
    private void close_point(EditText editText) {
        editText.clearFocus();
        editText.setFocusable(false);
        //关闭键盘
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); //强制隐藏键盘
        }
    }

    //供editview重新获取焦点的方法
    private void get_point(EditText editText) {
        //为edit_key重新获取焦点
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.findFocus();
        //开启键盘
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, 0);
        }
    }
}
