package com.idx.smartspeakdock.Swipe;

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
import com.idx.smartspeakdock.Setting.SettingFragment;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.event.ReturnVoiceEvent;
import com.idx.smartspeakdock.weather.presenter.ReturnAnswerCallback;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

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
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);
    private ControllerServiceConnection mServiceConnection;
    private ControllerService.MyBinder mControllerBinder;
    private Intent mShoppingBroadcastIntent;
    private Intent mWeatherBroadcastIntent;
    //天气参数
    private int mWeather_voice_flag;
    private String mWeather_voice_city;
    private String mWeather_voice_time;
    private String mWeather_func_flag;
    private ReturnVoice mWeather_return_voice;
    private Intent mMusicBroadcastIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("11111", "onCreate: ");
        //EventBus
        EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer_main);
        Log.d(TAG, "onCreate: swipeActivity创建");
        //对应fragment的id
        extraIntentId = getIntent().getStringExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT);
        initToolBar();
        //侧滑设置
        initDrawer();
        //fragment切换
        mWeather_voice_flag = -1;
        if (mSharePrefrenceUtils.getFirstChange(GlobalUtils.FIRST_CHANGE_FRAGMENT)) {
            changeFragment(extraIntentId);
        }

        //绑定语音注册监听器service
        Intent intent = new Intent(SwipeActivity.this, ControllerService.class);
        mServiceConnection = new ControllerServiceConnection();
        bindService(intent, mServiceConnection, 0);
    }

    //待机界面Touch
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyOnTouchListener listener : onTouchListeners) {
            if (listener != null) {
                listener.onTouch(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }

    public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener);
    }

    private void changeFragment(String extraIntentId) {
        Logger.info(TAG, extraIntentId);
        switch (extraIntentId) {
            case GlobalUtils.WEATHER_FRAGMENT_INTENT_ID:
                Bundle args = getIntent().getBundleExtra("weather");
                if (args != null) {
                    mWeather_voice_flag = args.getInt("voice_flag");
                    mWeather_voice_city = args.getString("cityname");
                    mWeather_voice_time = args.getString("time");
                    mWeather_func_flag = args.getString("fun_flag");
                    Log.i("11111", "changeFragment: mWeather_voice_flag = " + mWeather_voice_flag);
                }
                initWeather();
                if (mWeather_voice_flag == GlobalUtils.WEATHER_VOICE_FLAG) {
                    Log.i("11111", "changeFragment: voice_flag");
                    returnVoiceCallback();
                }
                break;
            case GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID:
                initCalendar();
                break;
            case GlobalUtils.MUSIC_FRAGMENT_INTENT_ID:
                music_name = getIntent().getStringExtra("music_name");
                initMusic(music_name);
                break;
            case GlobalUtils.MAP_FRAGMENT_INTENT_ID:
                initMap();
                break;
            case GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID:
                websites_url = getIntent().getStringExtra("weburl");
                initShopping(websites_url);
                break;
            case GlobalUtils.SETTING_FRAGMENT_INTENT_ID:
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
        //mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
    }

    private void initToolBar() {
        mResources = getResources();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mActionBar.setDisplayHomeAsUpEnabled(true);
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
                                initMusic("流水");
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
        mShoppingBroadcastIntent = new Intent(GlobalUtils.SHOPPING_BROADCAST_ACTION);

        //实例化music广播Intent
        mMusicBroadcastIntent = new Intent(GlobalUtils.MUSIC_BROADCAST_ACTION);
    }

    private void initSetting() {
        if (!checkFragment("setting")) {
            actionBar_title = mResources.getString(R.string.setting_title);
            if (settingFragment == null) {
                settingFragment = new SettingFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "setting");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, settingFragment, R.id.contentFrame);
        }
    }

    private void initMap() {
        if (!checkFragment("map")) {
            actionBar_title = mResources.getString(R.string.map_title);
            if (mapFragment == null) {
                mapFragment = new MapFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "map");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, mapFragment, R.id.contentFrame);
        }
    }

    private void initShopping(String web_url) {
        Log.i(TAG, "initShopping: ");
        if (!checkFragment("shopping")) {
            actionBar_title = mResources.getString(R.string.shopping_title);
            if (!(web_url.equals("")) && !TextUtils.isEmpty(web_url)) {
                if (shoppingFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(web_url);
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "shopping");
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, shoppingFragment, R.id.contentFrame);
            }
        }
    }

    private void initMusic(String music_name) {
        if (!checkFragment("music")) {
            actionBar_title = mResources.getString(R.string.music_title);
            if (music_name != null) {
                if (musicFragment == null) {
//                    musicFragment = new MusicListFragment();
                    musicFragment = MusicListFragment.newInstance(music_name);
                }
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, musicFragment, R.id.contentFrame);
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "music");
            }
        }
    }

    private void initCalendar() {
        Log.i(TAG, "initCalendar: ");
        if (!checkFragment("calendar")) {
            actionBar_title = mResources.getString(R.string.calendar_title);
            if (calendarFragment == null) {
                calendarFragment = new CalendarFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "calendar");
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, calendarFragment, R.id.contentFrame);
            Log.i(TAG, "initCalendar: mCurr_frag_name = " + mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID));
        }
    }

    private void initWeather() {
        if (mWeather_voice_flag == GlobalUtils.WEATHER_VOICE_FLAG) {
            Log.i("ryan", "initWeather: voice_flag");
            if (!checkFragment("weather")) {
                actionBar_title = mResources.getString(R.string.weather_title);
                if (weatherFragment == null) {
                    weatherFragment = WeatherFragment.newInstance(mWeather_voice_city, mWeather_voice_time, mWeather_func_flag, mWeather_voice_flag);
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "weather");
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, weatherFragment, R.id.contentFrame);
            }
        } else {
            Log.i("ryan", "initWeather: un_voice_flag");
            if (!checkFragment("weather")) {
                actionBar_title = mResources.getString(R.string.weather_title);
                if (weatherFragment == null) {
                    weatherFragment = new WeatherFragment();
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "weather");
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, weatherFragment, R.id.contentFrame);
            }
        }
    }

    //Music后台回退
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (musicFragment != null) {
                    if (musicFragment.isPlaying) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        AlertDialog dialog = builder.setTitle("音乐正在播放")
                                .setPositiveButton("后台播放", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SwipeActivity.this.finish();
                                    }
                                })
                                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        SwipeActivity.super.finish();
                                    }
                                }).create();

                        dialog.show();
                    } else {
                        super.finish();
                    }
                    break;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    //判断当前哪个fragment
    public boolean checkFragment(String frag_name) {
        mCurr_Frag_Name = mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID);
        Log.i(TAG, "checkFragment: frag_name_curr = " + mCurr_Frag_Name + ",frag_name = " + frag_name);
        switch (frag_name) {
            case "weather":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name) {
                        case "calendar":
                            calendarFragment = null;
                            break;
                        case "music":
                            musicFragment = null;
                            break;
                        case "shopping":
                            shoppingFragment = null;
                            break;
                        case "map":
                            mapFragment = null;
                            break;
                        case "setting":
                            settingFragment = null;
                            break;
                    }
                }
                break;
            case "calendar":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;
                            break;
                        case "music":
                            musicFragment = null;
                            break;
                        case "shopping":
                            shoppingFragment = null;
                            break;
                        case "map":
                            mapFragment = null;
                            break;
                        case "setting":
                            settingFragment = null;
                            break;
                    }
                }
                break;
            case "music":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;
                            break;
                        case "calendar":
                            calendarFragment = null;
                            break;
                        case "shopping":
                            shoppingFragment = null;
                            break;
                        case "map":
                            mapFragment = null;
                            break;
                        case "setting":
                            settingFragment = null;
                            break;
                    }
                }
                break;
            case "shopping":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;
                            break;
                        case "calendar":
                            calendarFragment = null;
                            break;
                        case "music":
                            musicFragment = null;
                            break;
                        case "map":
                            mapFragment = null;
                            break;
                        case "setting":
                            settingFragment = null;
                            break;
                    }
                }
                break;
            case "map":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;
                            break;
                        case "calendar":
                            calendarFragment = null;
                            break;
                        case "music":
                            musicFragment = null;
                            break;
                        case "shopping":
                            shoppingFragment = null;
                            break;
                        case "setting":
                            settingFragment = null;
                            break;
                    }
                }
                break;
            case "setting":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name) {
                        case "weather":
                            weatherFragment = null;
                            break;
                        case "calendar":
                            calendarFragment = null;
                            break;
                        case "music":
                            musicFragment = null;
                            break;
                        case "shopping":
                            shoppingFragment = null;
                            break;
                        case "map":
                            mapFragment = null;
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

    //购物语音处理
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

    //日历语音处理
    private void revokeSwipeCalendarVoice() {
        Log.d(TAG, "revokeSwipeCalendarVoice: 日历模块语音处理");
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

    //天气语音处理
    private void revokeSwipeWeatherVoice(String cityName, String time, final ReturnVoice returnVoice, String func_flag, int flag) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("WeatherFragment")) {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment是WeatherFragment");
                    mWeather_return_voice = returnVoice;
                    returnVoiceCallback();
                    mWeatherBroadcastIntent = new Intent(GlobalUtils.WEATHER_BROADCAST_ACTION);
                    mWeatherBroadcastIntent.putExtra("cityname", cityName);
                    mWeatherBroadcastIntent.putExtra("time", time);
                    mWeatherBroadcastIntent.putExtra("flag", func_flag);
                    sendBroadcast(mWeatherBroadcastIntent);
                } else {
                    Log.i(TAG, "openSpecifyWebsites: 当前Fragment不是WeatherFragment");
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
                    mMusicBroadcastIntent.putExtra("music", music_name);
                    sendBroadcast(mMusicBroadcastIntent);
                } else {
                    initMusic(music_name);
                    mActionBar.setTitle(actionBar_title);
                }
            }
        }
    }

    private void returnVoiceCallback() {
        if (weatherFragment != null) {
            weatherFragment.setReturnAnswerCallback(new ReturnAnswerCallback() {
                @Override
                public void onReturnAnswer(String voiceAnswer) {
                    if (mWeather_return_voice != null) {
                        Log.i("11111", "onReturnAnswer: ");
                        mWeather_return_voice.onReturnVoice(voiceAnswer);
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
            mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, false);
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
        mWeather_return_voice = null;
        EventBus.getDefault().unregister(this);
        unbindService(mServiceConnection);
    }

    @Subscribe
    public void onEvent(ReturnVoiceEvent returnVoiceEvent) {
        Log.i("11111", "onEvent: ");
        mWeather_return_voice = returnVoiceEvent.getReturnVoice();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
            mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.FIRST_CHANGE_FRAGMENT, false);
            super.onBackPressed();
        }
    }

    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
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
                    Log.i("ryan", "onShoppingCallback: " + web_url);
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

            //音乐
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
                    Log.i("ryan", "onWeatherCallback: 1111111");
                    revokeSwipeWeatherVoice(cityName, time, returnVoice, func_flag, flag);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mControllerBinder != null) {
                mControllerBinder = null;
            }
        }
    }
}
