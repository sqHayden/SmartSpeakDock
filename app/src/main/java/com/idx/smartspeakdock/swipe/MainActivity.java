package com.idx.smartspeakdock.swipe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.map.Bean.ReturnMapAnswerCallBack;
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.service.SpeakerService;
import com.idx.smartspeakdock.setting.SettingFragment;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.shopping.shoproom.entity.Shopping;
import com.idx.smartspeakdock.shopping.util.ParseXMLUtils;
import com.idx.smartspeakdock.standby.StandByFragment;
import com.idx.smartspeakdock.utils.ActivityStatusUtils;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.AppExecutors;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.PreUtils;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnAnswerCallback;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;
import com.lljjcoder.style.citypickerview.CityPickerView;

import java.util.List;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class MainActivity extends BaseActivity {
    private final String TAG = "MainActivity";
    public DrawerLayout mDrawerLayout;
    private Intent mControllerintent;
    private StandByFragment standByFragment;
    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private String websites_url;
    private String music_name;
    private String actionBar_title;
    private Resources mResources;
    private CoordinatorLayout right;
    private NavigationView left;
    //侧滑是否已开启
    private boolean isDrawer;
    //语音注册监听器service
    private MyServiceConnection myServiceConnection;
    private ControllerService.MyBinder mControllerBinder;
    private SharePrefrenceUtils mSharePrefrenceUtils;
    private String mCurr_Frag_Name;
    private AppExecutors mAppExecutors;
    List<Shopping> mShoppings;
    private Intent mShoppingBroadcastIntent;
    private Intent mWeatherBroadcastIntent;
    private Intent mMapBroadcastIntent;
    private WeatherFragment weatherFragment;
    private CalendarFragment calendarFragment;
    private MusicListFragment musicFragment;
    private ShoppingFragment shoppingFragment;
    private MapFragment mapFragment;
    private SettingFragment settingFragment;
    private String extraIntentId;
    //天气参数
    private int mWeather_voice_flag;
    private String mWeather_voice_city;
    private String mWeather_voice_time;
    private String mWeather_func_flag;
    private Intent mMusicBroadcastIntent;
    private ReturnVoice mWeather_return_voice;
    //地图参数
    private int mMap_voice_flag;
    private String mMap_voice_name;
    private String mMap_voice_address;
    private String mMap_voice_fromAddress;
    private String mMap_voice_toAddress;
    private String mMap_voice_pathWay;
    private ResultCallback mMap_result_callback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);
        initToolBar();
        //侧滑栏配置
        initDrawer();
        //启动语音唤醒识别service
        boolean isEnable = PreUtils.getItemObject(getBaseContext(), PreUtils.Items.SETTINGS,
                PreUtils.Settings.SPEAK_SERVICE_ENABLE_STATE, Boolean.class, true);
        if (isEnable && !isServiceRunning(this, SpeakerService.class.getName())) {
            startService(new Intent(this, SpeakerService.class));
            PreUtils.setItemObject(getBaseContext(), PreUtils.Items.SETTINGS,
                    PreUtils.Settings.SPEAK_SERVICE_ENABLE_STATE, isEnable);
        }
        //启动语音注册监听器service
        if (!isServiceRunning(this, ControllerService.class.getName())) {
            mControllerintent = new Intent(this, ControllerService.class);
            //启动service
            startService(mControllerintent);
            //绑定service
            myServiceConnection = new MyServiceConnection();
            bindService(mControllerintent, myServiceConnection, BIND_AUTO_CREATE);
        }

        //程序是否第一次启动
        isAppFirstStart();

        mAppExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                CityPickerView.getInstance().init(MainActivity.this);
            }
        });

        //对应fragment的id
        extraIntentId = getIntent().getStringExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT);
        //fragment切换
        if (extraIntentId != null) {
            if (mSharePrefrenceUtils.getFirstChange(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT)) {
                changeFragment(extraIntentId);
            }
        } else {
            //待机界面
            Fragment content_ragment = mFragmentManager.findFragmentById(R.id.contentFrame);
            if (content_ragment == null) {
                initStandBy();
            }
        }
    }
    private void changeFragment(String extraIntentId) {
        Logger.info(TAG, extraIntentId);
        switch (extraIntentId) {
            case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID:
                initWeather();
                break;
            case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID:
                initCalendar();
                break;
            case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID:
                initMusic();
                break;
            case GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID:
                initMap();
                break;
            case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_INTENT_ID:
                websites_url = getIntent().getStringExtra("weburl");
                initShopping(websites_url);
                break;
            case GlobalUtils.WhichFragment.SETTING_FRAGMENT_INTENT_ID:
                initSetting();
                break;
            default:
                break;
        }
        mActionBar.setTitle(actionBar_title);
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorSelfBlack);
        right = (CoordinatorLayout) findViewById(R.id.right);
        left = (NavigationView) findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isDrawer) {
                    return left.dispatchTouchEvent(motionEvent);
                } else {
                    return false;
                }
            }
        });
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                isDrawer = true;
                WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                right.layout(left.getRight(), 0, left.getRight() + display.getWidth(), display.getHeight());
            }
            @Override
            public void onDrawerOpened(View drawerView) {}

            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawer = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        //实例化SharePreferencesUtls
        mSharePrefrenceUtils = new SharePrefrenceUtils(this);
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
        //线程池
        mAppExecutors = new AppExecutors();
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.colorSelfBlack));
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.menu_left);
        mActionBar.setTitle("");
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mResources = getResources();
        //voice flag
        mWeather_voice_flag = -1;
        mMap_voice_flag = -1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_weather:
                                // TODO: 17-12-16  WeatherFragment
                                initWeather();
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CalendarFragment
                                initCalendar();
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MusicFragment
                                initMusic();
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 ShoppingFragment
                                initShopping("https://mall.flnet.com");
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MapFragemnt
                                initMap();
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SettingFargment
                                initSetting();
                                break;
                            default:
                                break;
                        }
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mActionBar.setTitle(actionBar_title);
                        return true;
                    }
                });
    }

    private void initStandBy(){
        Log.i(TAG, "initStandBy: ");
        if (!checkFragment(GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME)){
            actionBar_title = "";
            if (standByFragment == null){
                standByFragment = new StandByFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager,standByFragment, R.id.contentFrame);
        }
    }

    private void initSetting() {
        if (!checkFragment(GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.setting_title);
            if (settingFragment == null) {
                settingFragment = new SettingFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, settingFragment, R.id.contentFrame);
        }
    }

    private void initMap() {
        if (!checkFragment(GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME)) {
            Log.d("进来init()了","123456749");
            if (mMap_voice_flag == GlobalUtils.Map.MAP_VOICE_FLAG) {
                if (mapFragment == null) {
                    Log.d("进来语音传值了","12346");
                    mapFragment = MapFragment.newInstance(mMap_voice_name,mMap_voice_address,mMap_voice_fromAddress,mMap_voice_toAddress,mMap_voice_pathWay);
                }
            } else {
                if (mapFragment== null) {
                    mapFragment = new MapFragment();
                }
            }
            actionBar_title = mResources.getString(R.string.map_title);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, mapFragment, R.id.contentFrame);
        }else{
            Log.d("表示当前是","map");
        }
    }

    private void initShopping(String web_url) {
        if (!checkFragment(GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.shopping_title);
            if (!(web_url.equals("")) && !TextUtils.isEmpty(web_url)) {
                if (shoppingFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(web_url);
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME);
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, shoppingFragment, R.id.contentFrame);
            }
        }
    }

    private void initMusic() {
        if (!checkFragment(GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.music_title);
            Log.i(TAG, "initMusic: music_name = "+music_name);
            if (music_name != null) {
                if (musicFragment == null) {
                    musicFragment = MusicListFragment.newInstance(music_name);
                }
            }else {
                if (musicFragment == null) {
                    musicFragment = new MusicListFragment();
                }
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, musicFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME);
        }
    }

    private void initCalendar() {
        if (!checkFragment(GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.calendar_title);
            if (calendarFragment == null) {
                calendarFragment = new CalendarFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, calendarFragment, R.id.contentFrame);
        }
    }

    private void initWeather() {
        if (!checkFragment(GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME)) {
            if (mWeather_voice_flag == GlobalUtils.Weather.WEATHER_VOICE_FLAG) {
                if (weatherFragment == null) {
                    weatherFragment = WeatherFragment.newInstance(mWeather_voice_city, mWeather_voice_time, mWeather_func_flag, mWeather_voice_flag);
                }
            } else {
                if (weatherFragment == null) {
                    weatherFragment = new WeatherFragment();
                }
            }
            actionBar_title = mResources.getString(R.string.weather_title);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, weatherFragment, R.id.contentFrame);
        }
    }

    //判断当前哪个fragment
    public boolean checkFragment(String frag_name) {
        mCurr_Frag_Name = mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID);
        Log.i(TAG, "checkFragment: frag_name_curr = " + mCurr_Frag_Name + ",frag_name = " + frag_name);
        switch (frag_name) {
            case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:
                            break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)){return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }


    public void isAppFirstStart() {
        if (mSharePrefrenceUtils.getFirstAppStart(GlobalUtils.FirstSatrt.FIRST_APP_START)) {
            Log.i(TAG, "isAppFirstStart: isFirst = " + mSharePrefrenceUtils.getFirstAppStart(GlobalUtils.FirstSatrt.FIRST_APP_START));
            mSharePrefrenceUtils.saveFirstAppStart(GlobalUtils.FirstSatrt.FIRST_APP_START, false);
            mAppExecutors.getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mShoppings = ParseXMLUtils.readXMLPull(MainActivity.this);
                    for (int i = 0; i < mShoppings.size(); i++) {
                        Shopping shopping = mShoppings.get(i);
                        mSharePrefrenceUtils.insertWebUrl(shopping.getWebName(), shopping.getWebUrl());
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* isTopActivity();
        isTopFragment();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (standByFragment != null) { standByFragment = null;}
        isDrawer = false;
        if (mAppExecutors != null) { mAppExecutors = null;}
        if (mShoppings != null) {
            mShoppings.clear();
            mShoppings = null;
        }
        if (mControllerintent != null) { mControllerintent = null;}
        if (weatherFragment != null) { weatherFragment = null;}
        if (calendarFragment != null) { calendarFragment = null;}
        if (musicFragment != null) { musicFragment = null;}
        if (shoppingFragment != null) { shoppingFragment = null;}
        if (mapFragment != null) { mapFragment = null;}
        if (settingFragment != null) { settingFragment = null;}
        if (mSharePrefrenceUtils != null) {
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
            mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, false);
            mSharePrefrenceUtils = null;
        }
        if (mShoppingBroadcastIntent != null) { mShoppingBroadcastIntent = null;}
        if (mShoppingBroadcastIntent != null) { mWeatherBroadcastIntent = null; }
        mWeather_voice_flag = -1;
        mMap_voice_flag = -1;
        if (mWeather_return_voice != null) { mWeather_return_voice = null;}
        if (mMap_result_callback!=null) { mMap_result_callback = null;}
        music_name = null;
        ActivityStatusUtils.onDestroy();
        //解绑ControllerService
        unbindService(myServiceConnection);
//        stopService(mControllerintent);
    }
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (shoppingFragment != null){
                shoppingFragment.progDailog.dismiss();
            }
            mCurr_Frag_Name = mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID);
            if (!mCurr_Frag_Name.equals(GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME)){
                Log.i(TAG, "onBackPressed: not standy");
                initStandBy();
            }else{
                Log.i(TAG, "onBackPressed: standy");
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
                super.onBackPressed();
            }
        }
    }

    public class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: 主acitivy服务绑定");
            mControllerBinder = (ControllerService.MyBinder) iBinder;

            //shopping语音处理
            mControllerBinder.onReturnWeburl(new ShoppingCallBack() {
                @Override
                public void onShoppingCallback(String web_url) {
                    revokeMainShoppingVoice(web_url);
                }
            });

            //日历语音处理
            mControllerBinder.setCalendarControllerListener(new CalendarCallBack() {
                @Override
                public void onCalendarCallBack() {
                    revokeMainCalendarVoice();
                }
            });

            //音乐语音处理
            mControllerBinder.onGetMusicName(new MusicCallBack() {
                @Override
                public void onMusicCallBack(String music_name) {
                    revokeMainMusicVoice(music_name);
                }
            });
            //天气语音处理
            mControllerBinder.setWeatherControllerListener(new WeatherCallback() {
                @Override
                public void onWeatherCallback(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
                    mWeather_return_voice = returnVoice;
                    revokeMainWeatherVoice(cityName, time, returnVoice, func_flag, flag);
                }
            });

            //天气ReturnVoice注册
            if (mWeather_voice_flag == GlobalUtils.Weather.WEATHER_VOICE_FLAG){
                Log.i(TAG, "onServiceConnected: 天气ReturnVoice注册");
                mWeather_return_voice = mControllerBinder.getControlService().getReturnVoice();
                returnVoiceCallback();
            }

            //地图语音处理
            mControllerBinder.setMapControllerListener(new MapCallBack() {
                @Override
                public void onMapCallBack(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback result) {
                    revokeMainMapVoice(name,address,fromAddress,toAddress,pathWay,result);
                }
            });

            //地图ResultCallBack注册
            if (mMap_voice_flag == GlobalUtils.Map.MAP_VOICE_FLAG){
                Log.i(TAG, "onServiceConnected: 地图ResultCallBack注册");
                mMap_result_callback = mControllerBinder.getControlService().getResultCallBack();
                returnMapVoicecallBack();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mControllerBinder != null) {
                mControllerBinder = null;
            }
        }
    }

    //购物模块语音处理
    private void revokeMainShoppingVoice(String web_url) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("ShoppingFragment")) {
                    sendShoppingBroadcast(web_url);
                } else {
                    relaceShoppingFragment(web_url);
                }
            }
        }
    }

    //日历模块语音处理
    private void revokeMainCalendarVoice() {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("CalendarFragment")) {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment是CalendarFragment");
                } else {
                    replaceCalendarFragment();
                }
            }
        }
    }

    //天气模块语音处理
    private void revokeMainWeatherVoice(String cityName, String time, final ReturnVoice returnVoice, String func_flag, int flag) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("WeatherFragment")) {
                    sendWeatherBroadcast(cityName, time, returnVoice, func_flag);
                } else {
                    replaceWeatherBroadcast(cityName, time, returnVoice, func_flag, flag);
                }
            }
        }
    }

    //音乐模块语音处理
    private void revokeMainMusicVoice(String music_name) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("MusicFragment")) {
                    sendMusicBroadcast(music_name);
                } else {
                    replaceMusicFragment(music_name);
                }
            }
        }
    }

    //天气语音接口回调
    private void returnVoiceCallback() {
        if (weatherFragment != null) {
            weatherFragment.setReturnAnswerCallback(new ReturnAnswerCallback() {
                @Override
                public void onReturnAnswer(String voiceAnswer) {
                    Log.i(TAG, "onReturnAnswer: returnAnswer");
                    if (mWeather_return_voice != null) {
                        Log.i(TAG, "onReturnAnswer: voiceAnswer");
                        mWeather_return_voice.onReturnVoice(voiceAnswer);
                        mWeather_voice_flag = -1;
                    }
                }
            });
        }
    }

    //地图语音接口回调
    private void returnMapVoicecallBack() {
        if(mapFragment != null){
            mapFragment.setMapReturnAnswerCallback(new ReturnMapAnswerCallBack() {
                @Override
                public void onReturnAnswer(String mapAnswer) {
                    Log.d("地图语音答复回调","return Answer");
                    if (mMap_result_callback !=null){
                        Log.d("看到说明没办法了","146");
                        mMap_result_callback.onResult(mapAnswer);
                        mMap_voice_flag = -1;
                    }
                }
            });
        }
    }

    //地图模块处理
    private void revokeMainMapVoice(String name,String address,String fromAddress,String toAddress,String pathWay,ResultCallback resultCallback) {
        Log.d("isActivityTop:",""+isActivityTop);
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("MapFragment")) {
                    sendMapBroadcast(name, address, fromAddress, toAddress, pathWay, resultCallback);
                } else {
                    replaceMapFragment(name, address, fromAddress, toAddress, pathWay, resultCallback);
                }
            }
        }
    }

    private void relaceShoppingFragment(String web_url) {
        Log.i(TAG, "openSpecifyWebsites: 当前Fragment不是ShoppingFragment");
        initShopping(web_url);
        mActionBar.setTitle(actionBar_title);
    }

    private void sendShoppingBroadcast(String web_url) {
        Log.i(TAG, "openSpecifyWebsites: 当前Fragment是ShoppingFragment");
        mShoppingBroadcastIntent.putExtra("shoppings", web_url);
        sendBroadcast(mShoppingBroadcastIntent);
    }

    private void replaceCalendarFragment() {
        Log.i(TAG, "openSpecifyWebsites: 当前Fragment不是CalendarFragment");
        initCalendar();
        mActionBar.setTitle(actionBar_title);
    }

    private void replaceWeatherBroadcast(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
        Log.i(TAG, "revokeSwipeWeatherVoice: 当前Fragment不是WeatherFragment");
        mWeather_return_voice = returnVoice;
        mWeather_voice_city = cityName;
        mWeather_voice_time = time;
        mWeather_voice_flag = flag;
        mWeather_func_flag = func_flag;
        initWeather();
        mActionBar.setTitle(actionBar_title);
        returnVoiceCallback();
    }

    private void sendWeatherBroadcast(String cityName, String time, ReturnVoice returnVoice, String func_flag) {
        Log.i(TAG, "revokeSwipeWeatherVoice: 当前Fragment是WeatherFragment");
        mWeather_return_voice = returnVoice;
        returnVoiceCallback();
        mWeatherBroadcastIntent = new Intent(GlobalUtils.Weather.WEATHER_BROADCAST_ACTION);
        mWeatherBroadcastIntent.putExtra("cityname", cityName);
        mWeatherBroadcastIntent.putExtra("time", time);
        mWeatherBroadcastIntent.putExtra("flag", func_flag);
        sendBroadcast(mWeatherBroadcastIntent);
    }

    private void replaceMusicFragment(String music_name) {
        MainActivity.this.music_name = music_name;
        Log.i(TAG, "revokeSwipeMusicVoice: 当前Fragment不是MusicFragment");
        Log.d(TAG, "revokeSwipeMusicVoice music_name = "+MainActivity.this.music_name);
        initMusic();
        mActionBar.setTitle(actionBar_title);
    }

    private void sendMusicBroadcast(String music_name) {
        Log.i(TAG, "revokeSwipeMusicVoice: 当前Fragment是MusicFragment");
        mMusicBroadcastIntent.putExtra("music", music_name);
        sendBroadcast(mMusicBroadcastIntent);
    }

    private void replaceMapFragment(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback resultCallback) {
        Log.i(TAG, "revokeSwipeMapVoice: 当前Fragment不是MapFragment");
        mMap_result_callback = resultCallback;
        mMap_voice_name = name;
        mMap_voice_address = address;
        mMap_voice_fromAddress = fromAddress;
        mMap_voice_toAddress = toAddress;
        mMap_voice_pathWay = pathWay;
        mMap_voice_flag = 6;
        initMap();
        mActionBar.setTitle(actionBar_title);
        returnMapVoicecallBack();
    }

    private void sendMapBroadcast(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback resultCallback) {
        Log.i(TAG, "revokeSwipeMapVoice: 当前Fragment是MapFragment");
        mMap_result_callback = resultCallback;
        returnMapVoicecallBack();
        mMapBroadcastIntent = new Intent(GlobalUtils.Map.MAP_BROADCAST_ACTION);
        mMapBroadcastIntent.putExtra("name", name);
        mMapBroadcastIntent.putExtra("address", address);
        mMapBroadcastIntent.putExtra("fromAddress", fromAddress);
        mMapBroadcastIntent.putExtra("toAddress", toAddress);
        mMapBroadcastIntent.putExtra("pathWay", pathWay);
        Log.d("广播发出去的出行方式:", pathWay);
        sendBroadcast(mMapBroadcastIntent);

    }
}
