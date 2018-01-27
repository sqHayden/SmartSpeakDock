package com.idx.smartspeakdock.map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviException;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.map.Bean.StrategyBean;
import com.idx.smartspeakdock.map.adapter.BusResultListAdapter;
import com.idx.smartspeakdock.map.util.Constants;
import com.idx.smartspeakdock.map.util.ToastUtil;
import com.idx.smartspeakdock.map.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 驾车路径规划并展示对应的路线标签
 */
public class CalculateRouteActivity extends Activity implements AMapNaviListener, View.OnClickListener,RouteSearch.OnRouteSearchListener{
    private StrategyBean mStrategyBean;
    private static final float ROUTE_UNSELECTED_TRANSPARENCY = 0.3F;
    private static final float ROUTE_SELECTED_TRANSPARENCY = 1F;

    /**
     * 导航对象(单例)
     */
    private AMapNavi mAMapNavi;

    private MapView mMapView;
    private AMap mAMap;
   // 22.665669,114.045702
    private NaviLatLng startLatlng = null;
    //22.538667,113.944173
    private NaviLatLng endLatlng = null;
    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    /**
     * 途径点坐标集合
     */
    private List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();
    /**
     * 终点坐标集合［建议就一个终点］
     */
    private List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();
    /*
            * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
            * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
            */
    int strategyFlag = 0;
    private Button mStartNaviButton;
    private LinearLayout mRouteLineLayoutOne, mRouteLinelayoutTwo, mRouteLineLayoutThree;
    private View mRouteViewOne, mRouteViewTwo, mRouteViewThree;
    private TextView mRouteTextStrategyOne, mRouteTextStrategyTwo, mRouteTextStrategyThree;
    private TextView mRouteTextTimeOne, mRouteTextTimeTwo, mRouteTextTimeThree;
    private TextView mRouteTextDistanceOne, mRouteTextDistanceTwo, mRouteTextDistanceThree;
    private TextView mCalculateRouteOverView;
    private ImageView mImageTraffic;

    //出行方式按钮
    private RelativeLayout walkButton,driveButton,bikeButton,busButton;
    private ImageView route_walk,route_drive,route_bike,route_bus;
    // 规划线路
    private int routeID = -1;
    private static String way = "";

    //公交线路规划
    RouteSearch mRouteSearch = null;
    // 搜索时进度条
    private ProgressDialog progDialog = null;
    //公交路线
    private ListView mBusResultList;
    private BusRouteResult mBusRouteResult;
    private String mCurrentCityName="北京";
    private LinearLayout mBusResultLayout;
    private RelativeLayout map_function;
    private LinearLayout threeMessage;

    //回退按钮
    private ImageView back;

    //判断驾车结果集
    private boolean isFirst;
    private boolean isreturn = false;

    //出行地点选择
    private LinearLayout startLocationChoose,endLocationChoose;
    //搜索结果返回码
    public static final int REQUEST_CODE = 100;
    public static final int RESULT_CODE_INPUTTIPS = 101;
    //文本框
    private TextView myLocationName,endLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_calculate_route);
        mMapView = (MapView) findViewById(R.id.navi_view);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        initView();
        init();
        initNavi();
        initOtherActivity();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.calculate_route_start_navi://点击导航按钮
                startNavi();
                break;
            case R.id.walk_click://点击步行按钮
                if(way.equals("Bus")){
                    //上次是公交则打开视图
                    openView();
                }
                way = "Walk";
                isFirst = false;
                startWalkNavi();
                break;
            case R.id.drive_click://点击驾车按钮
                if(way.equals("Bus")){
                    //上次是公交则打开视图
                    openView();
                }
                mBusResultList.setVisibility(View.GONE);
                way = "Drive";
                startDriveNavi();
                break;
            case R.id.bike_click://点击骑行按钮
                if(way.equals("Bus")){
                    //上次是公交则打开视图
                    openView();
                }
                isFirst = false;
                way = "Bike";
                startBikeNavi();
                break;
            case R.id.bus_click://点击公交按钮
                way = "Bus";
                startBusNavi();
                break;
            case R.id.route_line_one:
                focuseRouteLine(true, false, false);
                break;
            case R.id.route_line_two:
                focuseRouteLine(false, true, false);
                break;
            case R.id.route_line_three:
                focuseRouteLine(false, false, true);
                break;
            case R.id.map_traffic:
                setTraffic();
                break;
            case R.id.back:
                backToStart();
                break;
            case R.id.start_location_choose://出发点选择
                getLocationMessage(0);
                break;
            case R.id.end_location_choose://终点选择
                getLocationMessage(1);
                break;
            default:
                break;
        }

    }
    /**
     * 处理出发点及终点点击事件
     * **/
    private void getLocationMessage(int i){
        Log.d("去获取了", "123456");
        Intent intent = new Intent(this, InputTipsActivity.class);
        intent.putExtra("city_name", mCurrentCityName);
        intent.putExtra("style","point_to_point");
        if(i==0){
            intent.putExtra("what_search","start");

        }else{
            intent.putExtra("what_search","end");
        }
        startActivityForResult(intent, REQUEST_CODE);
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
            //得到Item返回的封装对象
            Tip tip = data.getParcelableExtra(Constants.EXTRA_TIP);
            Log.d("item点击接收", tip.getName());
            //设置给对应的框
            String searchResult = data.getStringExtra("what_search_result");
            if(searchResult!=null) {
                if (searchResult.equals("start")) {
                    //起始点赋值
                    myLocationName.setText(tip.getName());
                    //起始点坐标赋值
                    Log.d("坐标为：",""+tip.getPoint().getLatitude()+","+tip.getPoint().getLongitude());
                    startLatlng = new NaviLatLng(tip.getPoint().getLatitude(), tip.getPoint().getLongitude());
                    startList.clear();
                    startList.add(startLatlng);
                } else {
                    //目标点赋值
                    endLocationName.setText(tip.getName());
                    //目标点坐标赋值
                    Log.d("坐标为：",""+tip.getPoint().getLatitude()+","+tip.getPoint().getLongitude());
                    endLatlng = new NaviLatLng(tip.getPoint().getLatitude(), tip.getPoint().getLongitude());
                    endList.clear();
                    endList.add(endLatlng);
                    //直接参数去调用驾车方法
                    isreturn = true;
                }
            }
        }
    }

    /**
     * 处理回退事件点击操作
     * **/
    private void backToStart() {
       this.finish();
    }

    /**
     * 步行路径规划
     * */
    private void startWalkNavi(){
//        NaviLatLng mNaviStart = new NaviLatLng(114.036538,22.664085);
//        NaviLatLng mNaviEnd = new NaviLatLng(113.923596,22.524022);
//        mAMapNavi.calculateWalkRoute(mNaviStart, mNaviEnd);
        showProgressDialog();
        mAMapNavi.calculateWalkRoute(startLatlng, endLatlng);
        Toast.makeText(this,"进入步行方法", Toast.LENGTH_SHORT).show();
        route_walk.setImageResource(R.drawable.route_walk_select);
        route_drive.setImageResource(R.drawable.route_drive_normal);
        route_bike.setImageResource(R.drawable.route_bike_normal);
        route_bus.setImageResource(R.drawable.route_bus_normal);
    }
    /**
     * 驾车路径规划
     */
    private void startDriveNavi() {
       showProgressDialog();
        try {
            strategyFlag = mAMapNavi.strategyConvert(mStrategyBean.isCongestion(), mStrategyBean.isCost(), mStrategyBean.isAvoidhightspeed(), mStrategyBean.isHightspeed(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mAMapNavi!=null&&startList!=null&&endList!=null&&wayList!=null){
            Log.d("都不是","空得");
        }
        mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
        Toast.makeText(this,"进入驾车方法", Toast.LENGTH_SHORT).show();
        route_walk.setImageResource(R.drawable.route_walk_normal);
        route_drive.setImageResource(R.drawable.route_drive_select);
        route_bike.setImageResource(R.drawable.route_bike_normal);
        route_bus.setImageResource(R.drawable.route_bus_normal);
    }
    /**
     *  骑行路径规划
     * */
    private void startBikeNavi(){
        showProgressDialog();
        mAMapNavi.calculateRideRoute(startLatlng,endLatlng);
        Toast.makeText(this,"进入骑行方法", Toast.LENGTH_SHORT).show();
        route_walk.setImageResource(R.drawable.route_walk_normal);
        route_drive.setImageResource(R.drawable.route_drive_normal);
        route_bike.setImageResource(R.drawable.route_bike_select);
        route_bus.setImageResource(R.drawable.route_bus_normal);
    }
    /**
     * 公交路径规划
     * */
    private void startBusNavi(){
        mRouteSearch.setRouteSearchListener(this);
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(startLatlng.getLatitude(),startLatlng.getLongitude()),
               new LatLonPoint(endLatlng.getLatitude(),endLatlng.getLongitude()));
        RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT,
                mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
        mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        Toast.makeText(this,"进入公交方法", Toast.LENGTH_SHORT).show();
        route_walk.setImageResource(R.drawable.route_walk_normal);
        route_drive.setImageResource(R.drawable.route_drive_normal);
        route_bike.setImageResource(R.drawable.route_bike_normal);
        route_bus.setImageResource(R.drawable.route_bus_select);
        mBusResultLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 导航初始化
     */
    private void initNavi() {
        mStrategyBean = new StrategyBean(false, false, false, false);
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
    }

    private void initView() {
        mStartNaviButton = (Button) findViewById(R.id.calculate_route_start_navi);
        mStartNaviButton.setOnClickListener(this);

        //回退按钮
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);

        mImageTraffic = (ImageView) findViewById(R.id.map_traffic);
        mImageTraffic.setOnClickListener(this);

        mCalculateRouteOverView = (TextView) findViewById(R.id.calculate_route_navi_overview);

        mRouteLineLayoutOne = (LinearLayout) findViewById(R.id.route_line_one);
        mRouteLineLayoutOne.setOnClickListener(this);
        mRouteLinelayoutTwo = (LinearLayout) findViewById(R.id.route_line_two);
        mRouteLinelayoutTwo.setOnClickListener(this);
        mRouteLineLayoutThree = (LinearLayout) findViewById(R.id.route_line_three);
        mRouteLineLayoutThree.setOnClickListener(this);

        mRouteViewOne = (View) findViewById(R.id.route_line_one_view);
        mRouteViewTwo = (View) findViewById(R.id.route_line_two_view);
        mRouteViewThree = (View) findViewById(R.id.route_line_three_view);

        mRouteTextStrategyOne = (TextView) findViewById(R.id.route_line_one_strategy);
        mRouteTextStrategyTwo = (TextView) findViewById(R.id.route_line_two_strategy);
        mRouteTextStrategyThree = (TextView) findViewById(R.id.route_line_three_strategy);

        mRouteTextTimeOne = (TextView) findViewById(R.id.route_line_one_time);
        mRouteTextTimeTwo = (TextView) findViewById(R.id.route_line_two_time);
        mRouteTextTimeThree = (TextView) findViewById(R.id.route_line_three_time);

        mRouteTextDistanceOne = (TextView) findViewById(R.id.route_line_one_distance);
        mRouteTextDistanceTwo = (TextView) findViewById(R.id.route_line_two_distance);
        mRouteTextDistanceThree = (TextView) findViewById(R.id.route_line_three_distance);

        //获取出行方式视图
        walkButton = (RelativeLayout)findViewById(R.id.walk_click);
        driveButton = (RelativeLayout)findViewById(R.id.drive_click);
        bikeButton = (RelativeLayout)findViewById(R.id.bike_click);
        busButton = (RelativeLayout)findViewById(R.id.bus_click);
        walkButton.setOnClickListener(this);
        driveButton.setOnClickListener(this);
        bikeButton.setOnClickListener(this);
        busButton.setOnClickListener(this);

        //获取出行方式图片视图
        route_walk = (ImageView) findViewById(R.id.route_walk);
        route_drive = (ImageView)findViewById(R.id.route_drive);
        route_bike = (ImageView)findViewById(R.id.route_bike);
        route_bus = (ImageView)findViewById(R.id.route_bus);

        //公交list
        mBusResultList = (ListView) findViewById(R.id.bus_result_list);
        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        map_function = (RelativeLayout)findViewById(R.id.map_function);
        threeMessage = (LinearLayout)findViewById(R.id.calculate_route_strategy_tab);

        //出行
        startLocationChoose = (LinearLayout)findViewById(R.id.start_location_choose);
        endLocationChoose = (LinearLayout)findViewById(R.id.end_location_choose);
        startLocationChoose.setOnClickListener(this);
        endLocationChoose.setOnClickListener(this);
        myLocationName = (TextView) findViewById(R.id.my_location_name);
        endLocationName = (TextView) findViewById(R.id.end_location_name);
    }


    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mAMap.setTrafficEnabled(false);
            mImageTraffic.setImageResource(R.drawable.map_traffic_white);
            UiSettings uiSettings = mAMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(false);
        }
        mRouteSearch = new RouteSearch(this);
        isFirst = true;
        isreturn = false;
    }

    /**
     * 处理其他Activity传递过来的信息
     * **/
    private void initOtherActivity(){
        //拿到传过来的Intent
        LatLng myStartLocation;
        Intent intent = getIntent();
        Gson gson = new Gson();
        String value = "";
        //判断传送的方式
        String style = intent.getStringExtra("click_style");
        if("startClick".equals(style)){//接收到用户初始界面点击事件
            //拿值
            value = intent.getStringExtra("start_location");
            //拿用户所在城市的名称
            mCurrentCityName = intent.getStringExtra("city_name");
            //获取用户当前位置
            myStartLocation = gson.fromJson(value,new TypeToken<LatLng>(){}.getType());
            //转换
            startLatlng = new NaviLatLng(myStartLocation.latitude,myStartLocation.longitude);
            Log.d("坐标为：",""+startLatlng.getLatitude()+","+startLatlng.getLongitude());
            startList.clear();
            startList.add(startLatlng);
            //显示公交的 空白页面
            closeView();
            mBusResultList.setVisibility(View.VISIBLE);
        }else {//接收用户地点查询出行
            //拿值
            value = intent.getStringExtra("start_location");
            //获取用户当前位置
            myStartLocation = gson.fromJson(value, new TypeToken<LatLng>(){}.getType());
            //转换
            startLatlng = new NaviLatLng(myStartLocation.latitude, myStartLocation.longitude);
            startList.clear();
            startList.add(startLatlng);
            Log.d("起始点坐标为：",""+startLatlng.getLatitude()+","+startLatlng.getLongitude());
            //获取目的地
            value = intent.getStringExtra("end_location");
            //获取用户目的地位置
            myStartLocation = gson.fromJson(value,new TypeToken<LatLng>(){}.getType());
            //转换
            endLatlng = new NaviLatLng(myStartLocation.latitude, myStartLocation.longitude);
            endList.clear();
            endList.add(endLatlng);
            Log.d("目的点坐标为：",""+endLatlng.getLatitude()+","+endLatlng.getLongitude());
            //目的地设置文本
            endLocationName.setText(intent.getStringExtra("end_name"));
            value = intent.getStringExtra("start_name");
            if(value!=null){
                myLocationName.setText(intent.getStringExtra("start_name"));
            }
            //获取用户的出行方式
            value = intent.getStringExtra("pathWay");
            switch (value) {
                case "步行":
                    way = "Walk";
                    startWalkNavi();
                    break;
                case "骑车":
                    way = "Bike";
                    startBikeNavi();
                    break;
                case "坐公交":
                    way = "Bus";
                    startBusNavi();
                    break;
                case "驾车":
                    way = "Drive";
                    startDriveNavi();
                default:
                    break;
            }
        }
    }

    /**
     * 绘制返回的驾车路径
     *
     * @param routeId 路径规划线路ID
     * @param path    AMapNaviPath
     */
    private void drawDriveRoutes(int routeId, AMapNaviPath path) {
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, this);
        try {
            routeOverLay.setWidth(60f);
        } catch (AMapNaviException e) {
            e.printStackTrace();
        }
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
    }

    /**
     * 绘制返回的步行路径
     *
     * @param routeId 路径规划线路ID
     * @param path    AMapNaviPath
     */
    private void drawWalkRoute(int routeId, AMapNaviPath path) {
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, this);
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
        routeOverLay.zoomToSpan();
    }

    /**
     * 绘制返回的骑行路径
     *
     * @param routeId 路径规划线路ID
     * @param path    AMapNaviPath
     */
    private void drawBikeRoute(int routeId, AMapNaviPath path){
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, this);
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
        routeOverLay.zoomToSpan();
    }

    /**
     * 返回公交查询结果
     * **/
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        mAMap.clear();// 清理地图上的所有覆盖物
        closeView();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(getApplicationContext(), mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                    mBusResultList.setVisibility(View.VISIBLE);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(getBaseContext(), R.string.no_result);
                }
            } else {
                ToastUtil.show(getBaseContext(), R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
    }
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
    }
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
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
     * 开始导航
     */
    private void startNavi() {
        if (routeID != -1){
            mAMapNavi.selectRouteId(routeID);
            Intent gpsintent = new Intent(getApplicationContext(), RouteNaviActivity.class);
            gpsintent.putExtra("gps", false); // gps 为true为真实导航，为false为模拟导航
            startActivity(gpsintent);
        }
    }

    /**
     * 路线tag选中设置
     *
     * @param lineOne
     * @param lineTwo
     * @param lineThree
     */
    private void focuseRouteLine(boolean lineOne, boolean lineTwo, boolean lineThree) {
        Log.d("LG", "lineOne:" + lineOne + " lineTwo:" + lineTwo + " lineThree:" + lineThree);
        setLinelayoutOne(lineOne);
        setLinelayoutTwo(lineTwo);
        setLinelayoutThree(lineThree);
    }

    /**
     * 地图实时交通开关
     */
    private void setTraffic() {
        if (mAMap.isTrafficEnabled()) {
            mImageTraffic.setImageResource(R.drawable.map_traffic_white);
            mAMap.setTrafficEnabled(false);
        } else {
            mImageTraffic.setImageResource(R.drawable.map_traffic_hl_white);
            mAMap.setTrafficEnabled(true);
        }
    }

    private void cleanRouteOverlay() {
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            RouteOverLay overlay = routeOverlays.get(key);
            overlay.removeFromMap();
            overlay.destroy();
        }
        routeOverlays.clear();
    }


    /**
     * @param paths 多路线回调路线
     * @param ints  多路线回调路线ID
     */
    private void setDriveRouteLineTag(HashMap<Integer, AMapNaviPath> paths, int[] ints) {
        if (ints.length < 1) {
            visiableRouteLine(false, false, false);
            return;
        }
        int indexOne = 0;
        String stragegyTagOne = paths.get(ints[indexOne]).getLabels();
        setLinelayoutOneContent(ints[indexOne], stragegyTagOne);
        if (ints.length == 1) {
            visiableRouteLine(true, false, false);
            focuseRouteLine(true, false, false);
            return;
        }

        int indexTwo = 1;
        String stragegyTagTwo = paths.get(ints[indexTwo]).getLabels();
        setLinelayoutTwoContent(ints[indexTwo], stragegyTagTwo);
        if (ints.length == 2) {
            visiableRouteLine(true, true, false);
            focuseRouteLine(true, false, false);
            return;
        }

        int indexThree = 2;
        String stragegyTagThree = paths.get(ints[indexThree]).getLabels();
        setLinelayoutThreeContent(ints[indexThree], stragegyTagThree);
        if (ints.length >= 3) {
            visiableRouteLine(true, true, true);
            focuseRouteLine(true, false, false);
        }

    }


    private void visiableRouteLine(boolean lineOne, boolean lineTwo, boolean lineThree) {
        setLinelayoutOneVisiable(lineOne);
        setLinelayoutTwoVisiable(lineTwo);
        setLinelayoutThreeVisiable(lineThree);
    }

    private void setLinelayoutOneVisiable(boolean visiable) {
        if (visiable) {
            mRouteLineLayoutOne.setVisibility(View.VISIBLE);
        } else {
            mRouteLineLayoutOne.setVisibility(View.GONE);
        }
    }

    private void setLinelayoutTwoVisiable(boolean visiable) {
        if (visiable) {
            mRouteLinelayoutTwo.setVisibility(View.VISIBLE);
        } else {
            mRouteLinelayoutTwo.setVisibility(View.GONE);
        }
    }

    private void setLinelayoutThreeVisiable(boolean visiable) {
        if (visiable) {
            mRouteLineLayoutThree.setVisibility(View.VISIBLE);
        } else {
            mRouteLineLayoutThree.setVisibility(View.GONE);
        }
    }

    /**
     * 设置第一条线路Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutOneContent(int routeID, String strategy) {
        mRouteLineLayoutOne.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        overlay.zoomToSpan();
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyOne.setText(strategy);
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeOne.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceOne.setText(disDes);
    }

    /**
     * 设置第二条路线Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutTwoContent(int routeID, String strategy) {
        mRouteLinelayoutTwo.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyTwo.setText(strategy);
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeTwo.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceTwo.setText(disDes);
    }

    /**
     * 设置第三条路线Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutThreeContent(int routeID, String strategy) {
        mRouteLineLayoutThree.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyThree.setText(strategy);
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeThree.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceThree.setText(disDes);
    }

    /**
     * 设置步行的内容填充
     * **/
    private void setWalkContent(int id){
        Log.d("步行内容填充","123");
        routeID = routeOverlays.keyAt(0);
        RouteOverLay overlay = routeOverlays.get(id);
        overlay.zoomToSpan();
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyOne.setText("");
        mRouteTextTimeOne.setText("");
        mRouteTextDistanceOne.setText("");
        mRouteTextStrategyTwo.setText("步行方案");
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeTwo.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceTwo.setText(disDes);
        //显示中间的
        mRouteLinelayoutTwo.setVisibility(View.VISIBLE);
        mRouteTextStrategyThree.setText("");
        mRouteTextTimeThree.setText("");
        mRouteTextDistanceThree.setText("");
        mCalculateRouteOverView.setText(Utils.getRouteOverView(path));
        mCalculateRouteOverView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置骑行的内容填充
     * **/
    private void setBikeContent(int id){
        routeID = routeOverlays.keyAt(0);
        RouteOverLay overlay = routeOverlays.get(id);
        overlay.zoomToSpan();
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyOne.setText("");
        mRouteTextTimeOne.setText("");
        mRouteTextDistanceOne.setText("");
        mRouteTextStrategyTwo.setText("骑行方案");
        String timeDes = Utils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeTwo.setText(timeDes);
        String disDes = Utils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceTwo.setText(disDes);
        //骑行显示
        mRouteLinelayoutTwo.setVisibility(View.VISIBLE);
        mRouteTextStrategyThree.setText("");
        mRouteTextTimeThree.setText("");
        mRouteTextDistanceThree.setText("");
        mCalculateRouteOverView.setText(Utils.getRouteOverView(path));
        mCalculateRouteOverView.setVisibility(View.VISIBLE);
    }

    /**
     * 第一条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutOne(boolean focus) {
        if (mRouteLineLayoutOne.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            RouteOverLay overlay = routeOverlays.get((int)mRouteLineLayoutOne.getTag());
            if (focus) {
                routeID = (int) mRouteLineLayoutOne.getTag();
                mCalculateRouteOverView.setText(Utils.getRouteOverView(overlay.getAMapNaviPath()));
                mCalculateRouteOverView.setVisibility(View.VISIBLE);
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewOne.setVisibility(View.VISIBLE);
                mRouteTextStrategyOne.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeOne.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceOne.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewOne.setVisibility(View.INVISIBLE);
                mRouteTextStrategyOne.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeOne.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceOne.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第二条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutTwo(boolean focus) {
        if (mRouteLinelayoutTwo.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            RouteOverLay overlay = routeOverlays.get((int) mRouteLinelayoutTwo.getTag());
            if (focus) {
                routeID = (int) mRouteLinelayoutTwo.getTag();
                mCalculateRouteOverView.setText(Utils.getRouteOverView(overlay.getAMapNaviPath()));
                mCalculateRouteOverView.setVisibility(View.VISIBLE);
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewTwo.setVisibility(View.VISIBLE);
                mRouteTextStrategyTwo.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeTwo.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceTwo.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewTwo.setVisibility(View.INVISIBLE);
                mRouteTextStrategyTwo.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeTwo.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceTwo.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第三条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutThree(boolean focus) {
        if (mRouteLineLayoutThree.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            RouteOverLay overlay = routeOverlays.get((int) mRouteLineLayoutThree.getTag());
            if (overlay == null) {
                return;
            }
            if (focus) {
                routeID = (int) mRouteLineLayoutThree.getTag();
                mCalculateRouteOverView.setText(Utils.getRouteOverView(overlay.getAMapNaviPath()));
                mCalculateRouteOverView.setVisibility(View.VISIBLE);
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewThree.setVisibility(View.VISIBLE);
                mRouteTextStrategyThree.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeThree.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceThree.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewThree.setVisibility(View.INVISIBLE);
                mRouteTextStrategyThree.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeThree.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceThree.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if(isreturn){
            //打开视图
            openView();
            way = "Drive";
            startDriveNavi();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMapView!=null) {
            mMapView.onDestroy();
        }
        if (mAMapNavi != null) {
            mAMapNavi.destroy();
        }
        if(mAMap!=null){
            mAMap.clear();
            mAMap = null;
        }
        if(startLatlng!=null||endLatlng!=null){
            startLatlng = null;
            endLatlng = null;
        }
        if(startList!=null||endList!=null){
            startList.clear();
            startList = null;
            endList.clear();
            endList = null;
            wayList.clear();
            wayList = null;
        }
        if(routeOverlays!=null){
            routeOverlays.clear();
            routeOverlays = null;
        }
        if(mRouteSearch!=null){
            mRouteSearch = null;
        }
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Toast.makeText(this.getApplicationContext(),"错误码"+i, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    /**
     * 路径规划成功回调
     ***/
    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        cleanRouteOverlay();
        Log.d("go to paint","demo11");
        if(way.equals("Drive")) {
            HashMap<Integer, AMapNaviPath> paths = mAMapNavi.getNaviPaths();
            Log.d("paths:",""+paths.size());
            if(!isFirst){
                isFirst = true;
                startDriveNavi();
            }else {
                Log.d("进入到了", "驾车模式");
                for (int i = 0; i < ints.length; i++) {
                    AMapNaviPath path = paths.get(ints[i]);
                    if (path != null) {
                        Log.d("start paint", "123456");
                        drawDriveRoutes(ints[i], path);
                    }
                }
            }
            setDriveRouteLineTag(paths, ints);
            dissmissProgressDialog();
        }else if(way.equals("Walk")) {
            dissmissProgressDialog();
            Log.d("进入到了", "步行模式");
            AMapNaviPath naviPath = mAMapNavi.getNaviPath();
            if (naviPath != null) {
                drawWalkRoute(ints[0], naviPath);
                setWalkContent(ints[0]);
            }
        }else if(way.equals("Bike")) {
            dissmissProgressDialog();
            Log.d("进入到了","骑行模式");
            AMapNaviPath naviPath = mAMapNavi.getNaviPath();
            if (naviPath != null) {
                drawBikeRoute(ints[0], naviPath);
                setBikeContent(ints[0]);
            }
        }
    }
    /**
     * 公交关闭相关视图
     * **/
    private void closeView(){
        //隐藏相关的视图
        map_function.setVisibility(View.GONE);
        threeMessage.setVisibility(View.GONE);
        mCalculateRouteOverView.setVisibility(View.GONE);
        mStartNaviButton.setVisibility(View.GONE);
    }

    /**
     * 公交打开相关视图
     * **/
    private void openView(){
        //打开相关的视图
        //隐藏listView
        mBusResultList.setVisibility(View.GONE);
        map_function.setVisibility(View.VISIBLE);
        threeMessage.setVisibility(View.VISIBLE);
        mCalculateRouteOverView.setVisibility(View.VISIBLE);
        mStartNaviButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

}
