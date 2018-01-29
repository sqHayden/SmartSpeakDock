package com.idx.smartspeakdock.swipe;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.setting.SettingFragment;
import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.map.Bean.ReturnMapAnswerCallBack;
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnAnswerCallback;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class SwipeActivity extends BaseActivity {
    private static final String TAG = SwipeActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private Resources mResources;
    private DrawerLayout mDrawerLayout;
    private WeatherFragment weatherFragment;
    private CalendarFragment calendarFragment;
    private MusicListFragment musicFragment;
    private ShoppingFragment shoppingFragment;
    private MapFragment mapFragment;
    private SettingFragment settingFragment;
    private CoordinatorLayout right;
    private NavigationView left;
    private boolean isDrawer;
    private String extraIntentId;
    private String websites_url;
    private String music_name;
    private String actionBar_title;
    private SharePrefrenceUtils mSharePrefrenceUtils;
    private String mCurr_Frag_Name;
    private ControllerServiceConnection mServiceConnection;
    private ControllerService.MyBinder mControllerBinder;
    private Intent mShoppingBroadcastIntent;
    private Intent mWeatherBroadcastIntent;
    private Intent mMapBroadcastIntent;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer_main);
        //绑定语音注册监听器service
        Intent intent = new Intent(SwipeActivity.this, ControllerService.class);
        mServiceConnection = new ControllerServiceConnection();
        bindService(intent, mServiceConnection, 0);
        //对应fragment的id
        extraIntentId = getIntent().getStringExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT);
        initToolBar();
        //侧滑设置
        initDrawer();

        //fragment切换
        mWeather_voice_flag = -1;
        mMap_voice_flag = -1;
        if (mSharePrefrenceUtils.getFirstChange(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT)) {
            changeFragment(extraIntentId);
        }
    }


    private void changeFragment(String extraIntentId) {
        Logger.info(TAG, extraIntentId);
        switch (extraIntentId) {
            case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID:
                Bundle args = getIntent().getBundleExtra("weather");
                if (args != null) {
                    mWeather_voice_flag = args.getInt("voice_flag");
                    mWeather_voice_city = args.getString("cityname");
                    mWeather_voice_time = args.getString("time");
                    mWeather_func_flag = args.getString("fun_flag");
                }
                initWeather();
                break;
            case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID:
                initCalendar();
                break;
            case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID:
                music_name = getIntent().getStringExtra("music_name");
                initMusic();
                break;
            case GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID:
                Log.d("进来Map了","哇哈哈");
                Bundle map_args = getIntent().getBundleExtra("map");
                if (map_args != null) {
                    mMap_voice_name = map_args.getString("name");
                    mMap_voice_address = map_args.getString("address");
                    mMap_voice_fromAddress = map_args.getString("fromAddress");
                    mMap_voice_toAddress = map_args.getString("toAddress");
                    mMap_voice_pathWay = map_args.getString("pathWay");
                }
                mMap_voice_flag = 6;
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
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawer = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        mSharePrefrenceUtils = new SharePrefrenceUtils(this);
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
    }

    private void initToolBar() {
        mResources = getResources();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setBackgroundColor(mResources.getColor(R.color.colorSelfBlack));
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.menu_left);
        mActionBar.setDisplayHomeAsUpEnabled(true);
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
                                // TODO: 17-12-16  WEATHER
                                initWeather();
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CALENDAR
                                initCalendar();
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MUSIC
                                initMusic();
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 SHOPPING
                                initShopping("https://mall.flnet.com");
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MAP
                                initMap();
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SETTING
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

        //实例化Shopping广播Intent
        mShoppingBroadcastIntent = new Intent(GlobalUtils.Shopping.SHOPPING_BROADCAST_ACTION);

        //实例化music广播Intent
        mMusicBroadcastIntent = new Intent(GlobalUtils.Music.MUSIC_BROADCAST_ACTION);
    }

    private void initSetting() {
        if (!checkFragment("setting")) {
            actionBar_title = mResources.getString(R.string.setting_title);
            if (settingFragment == null) {
                settingFragment = new SettingFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "setting");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, settingFragment, R.id.contentFrame);
        }
    }

    private void initMap() {
        if (!checkFragment("map")) {
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
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "map");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, mapFragment, R.id.contentFrame);
        }else{
            Log.d("表示当前是","map");
        }
    }

    private void initShopping(String web_url) {
        if (!checkFragment("shopping")) {
            actionBar_title = mResources.getString(R.string.shopping_title);
            if (!(web_url.equals("")) && !TextUtils.isEmpty(web_url)) {
                if (shoppingFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(web_url);
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "shopping");
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, shoppingFragment, R.id.contentFrame);
            }
        }
    }

    private void initMusic() {
        if (!checkFragment("music")) {
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
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "music");
        }
    }

    private void initCalendar() {
        if (!checkFragment("calendar")) {
            actionBar_title = mResources.getString(R.string.calendar_title);
            if (calendarFragment == null) {
                calendarFragment = new CalendarFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "calendar");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, calendarFragment, R.id.contentFrame);
        }
    }

    private void initWeather() {
        if (!checkFragment("weather")) {
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
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "weather");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, weatherFragment, R.id.contentFrame);
        }
    }

    //判断当前哪个fragment
    public boolean checkFragment(String frag_name) {
        mCurr_Frag_Name = mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID);
        Log.i(TAG, "checkFragment: frag_name_curr = " + mCurr_Frag_Name + ",frag_name = " + frag_name);
        switch (frag_name) {
            case "weather":
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                        default:break;
                    }
                }
                break;
            case "calendar":
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;break;
                        case "music":
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                        default:break;
                    }
                }
                break;
            case "music":
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                        default:break;
                    }
                }
                break;
            case "shopping":
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                        default:break;
                    }
                }
                break;
            case "map":
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                        default:break;
                    }
                }
                break;
            case "setting":
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            if (musicFragment.musicService.musicPlay.isPlaying()){
                                musicFragment.pause();
                            }
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
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
/*
    @Override
    public void finish() {
        moveTaskToBack(false);
    }*/

    //购物模块语音处理
    private void revokeSwipeShoppingVoice(String web_url) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("ShoppingFragment")) {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment是ShoppingFragment");
                    mShoppingBroadcastIntent.putExtra("shoppings", web_url);
                    sendBroadcast(mShoppingBroadcastIntent);
                } else {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment不是ShoppingFragment");
                    initShopping(web_url);
                    mActionBar.setTitle(actionBar_title);
                }
            }
        }
    }

    //日历模块语音处理
    private void revokeSwipeCalendarVoice() {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("CalendarFragment")) {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment是CalendarFragment");

                } else {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment不是CalendarFragment");
                    initCalendar();
                    mActionBar.setTitle(actionBar_title);
                }
            }
        }
    }

    //天气模块语音处理
    private void revokeSwipeWeatherVoice(String cityName, String time, final ReturnVoice returnVoice, String func_flag, int flag) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("WeatherFragment")) {
                    Log.i(TAG, "revokeSwipeWeatherVoice: 当前Fragment是WeatherFragment");
                    mWeather_return_voice = returnVoice;
                    returnVoiceCallback();
                    mWeatherBroadcastIntent = new Intent(GlobalUtils.Weather.WEATHER_BROADCAST_ACTION);
                    mWeatherBroadcastIntent.putExtra("cityname", cityName);
                    mWeatherBroadcastIntent.putExtra("time", time);
                    mWeatherBroadcastIntent.putExtra("flag", func_flag);
                    sendBroadcast(mWeatherBroadcastIntent);
                } else {
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
            }
        }
    }

    //音乐模块语音处理
    private void revokeSwipeMusicVoice(String music_name) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("MusicFragment")) {
                    Log.i(TAG, "revokeSwipeMusicVoice: 当前Fragment是MusicFragment");
                    mMusicBroadcastIntent.putExtra("music", music_name);
                    sendBroadcast(mMusicBroadcastIntent);
                } else {
                    SwipeActivity.this.music_name = music_name;
                    Log.i(TAG, "revokeSwipeMusicVoice: 当前Fragment不是MusicFragment");
                    Log.d(TAG, "revokeSwipeMusicVoice music_name = "+SwipeActivity.this.music_name);
                    initMusic();
                    mActionBar.setTitle(actionBar_title);
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
    private void revokeSwipeMapVoice(String name,String address,String fromAddress,String toAddress,String pathWay,ResultCallback resultCallback) {
        Log.d("isActivityTop:",""+isActivityTop);
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("MapFragment")) {
                    Log.i(TAG, "revokeSwipeMapVoice: 当前Fragment是MapFragment");
                    mMap_result_callback = resultCallback;
                    returnMapVoicecallBack();
                    mMapBroadcastIntent = new Intent(GlobalUtils.Map.MAP_BROADCAST_ACTION);
                    mMapBroadcastIntent.putExtra("name", name);
                    mMapBroadcastIntent.putExtra("address", address);
                    mMapBroadcastIntent.putExtra("fromAddress", fromAddress);
                    mMapBroadcastIntent.putExtra("toAddress",toAddress);
                    mMapBroadcastIntent.putExtra("pathWay",pathWay);
                    Log.d("广播发出去的出行方式:",pathWay);
                    sendBroadcast(mMapBroadcastIntent);
                } else {
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
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: swipe");
//        mSharePrefrenceUtils.saveBackgroudActivity(GlobalUtils.WhichActivity.BACKGROUND_WHICH_ACTIVITY,GlobalUtils.WhichActivity.SWIPE_ACTIVITY_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: swipe");
        if (weatherFragment != null) {
            weatherFragment = null;
        }
        if (calendarFragment != null) {
            calendarFragment = null;
        }
        if (musicFragment != null) {
            musicFragment = null;
        }
        if (shoppingFragment != null) {
            shoppingFragment = null;
        }
        if (mapFragment != null) {
            mapFragment = null;
        }
        if (settingFragment != null) {
            settingFragment = null;
        }
        if (mSharePrefrenceUtils != null) {
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
            mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, false);
//            mSharePrefrenceUtils.saveBackgroudActivity(GlobalUtils.WhichActivity.BACKGROUND_WHICH_ACTIVITY,"");
            mSharePrefrenceUtils = null;
        }
        if (mShoppingBroadcastIntent != null) {
            mShoppingBroadcastIntent = null;
        }
        if (mShoppingBroadcastIntent != null) {
            mWeatherBroadcastIntent = null;
        }
        isDrawer = false;
        mWeather_voice_flag = -1;
        if (mWeather_return_voice != null) {mWeather_return_voice = null;}
        if (mMap_result_callback!=null) {mMap_result_callback = null;}
        music_name = null;
        unbindService(mServiceConnection);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (shoppingFragment != null){
                shoppingFragment.progDailog.dismiss();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
            mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, false);
        }
        super.onBackPressed();
    }
    public class ControllerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: 连接到后台服务");
            mControllerBinder = (ControllerService.MyBinder) iBinder;

            //shopping语音处理
            mControllerBinder.onReturnWeburl(new ShoppingCallBack() {
                @Override
                public void onShoppingCallback(String web_url) {
                    Log.i(TAG, "onShoppingCallback: " + web_url);
                    revokeSwipeShoppingVoice(web_url);
                }
            });

            //日历语音处理
            mControllerBinder.setCalendarControllerListener(new CalendarCallBack() {
                @Override
                public void onCalendarCallBack() {
                    revokeSwipeCalendarVoice();
                }
            });

            //音乐语音处理
            mControllerBinder.onGetMusicName(new MusicCallBack() {
                @Override
                public void onMusicCallBack(String music_name) {
                    Log.d(TAG, "onMusicCallBack: " + music_name);
                    revokeSwipeMusicVoice(music_name);
                }
            });
            //天气语音处理
            mControllerBinder.setWeatherControllerListener(new WeatherCallback() {
                @Override
                public void onWeatherCallback(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
                    mWeather_return_voice = returnVoice;
                    revokeSwipeWeatherVoice(cityName, time, returnVoice, func_flag, flag);
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
                public void onMapCallBack(String name, String address, String fromAddress, String toAddress, PathWay pathWay, ResultCallback resultCallback) {
                    if(pathWay!=null) {
                        Log.d("pathWay","不是空的");
                        revokeSwipeMapVoice(name, address, fromAddress, toAddress, pathWay.getDesc(), resultCallback);
                    }else{
                        Log.d("pathWay","是空的");
                        revokeSwipeMapVoice(name, address, fromAddress, toAddress, "", resultCallback);
                    }
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
}
