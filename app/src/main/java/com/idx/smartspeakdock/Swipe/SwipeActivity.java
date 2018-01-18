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
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

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
    private String actionBar_title;
    private SharePrefrenceUtils mSharePrefrenceUtils;
    private String mCurr_Frag_Name;
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);
    private ControllerServiceConnection mServiceConnection;
    private ControllerService.MyBinder mControllerBinder;
    private Intent mShoppingBroadcastIntent;

    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer_main);
        //对应fragment的id
        extraIntentId = getIntent().getStringExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT);
        initToolBar();
        //侧滑设置
        initDrawer();
        //fragment切换
        changeFragment(extraIntentId);
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
                initWeather();
                break;
            case GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID:
                initCalendar();
                break;
            case GlobalUtils.MUSIC_FRAGMENT_INTENT_ID:
                initMusic();
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
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
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
        mShoppingBroadcastIntent = new Intent(GlobalUtils.SHOPPING_BROADCAST_ACTION);
    }

    private void initSetting() {
        if (!checkFragment("setting")) {
            actionBar_title = mResources.getString(R.string.setting_title);
            if (settingFragment == null) {
                settingFragment = new SettingFragment();
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, settingFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "setting");
        }
    }

    private void initMap() {
        if (!checkFragment("map")) {
            actionBar_title = mResources.getString(R.string.map_title);
            if (mapFragment == null) {
                mapFragment = new MapFragment();
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, mapFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "map");
        }
    }

    private void initShopping(String web_url) {
        if (!checkFragment("shopping")) {
            actionBar_title = mResources.getString(R.string.shopping_title);
            if (!(web_url.equals("")) && !TextUtils.isEmpty(web_url)){
                if (shoppingFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(web_url);
                }
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, shoppingFragment, R.id.contentFrame);
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "shopping");
            }
        }
    }

    private void initMusic() {
        if (!checkFragment("music")) {
            actionBar_title = mResources.getString(R.string.music_title);
            if (musicFragment == null) {
                musicFragment = new MusicListFragment();
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, musicFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "music");
        }
    }

    private void initCalendar() {
        if (!checkFragment("calendar")) {
            actionBar_title = mResources.getString(R.string.calendar_title);
            if (calendarFragment == null) {
                calendarFragment = new CalendarFragment();
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, calendarFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "calendar");
        }
    }

    private void initWeather() {
        if (!checkFragment("weather")) {
            actionBar_title = mResources.getString(R.string.weather_title);
            if (weatherFragment == null) {
                weatherFragment = new WeatherFragment();
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, weatherFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "weather");
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
/*
    @Override
    public void finish() {
        moveTaskToBack(false);
    }*/

    //判断当前哪个fragment
    public boolean checkFragment(String frag_name) {
        mCurr_Frag_Name = mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID);
        Log.i(TAG, "checkFragment: frag_name_curr = " + mCurr_Frag_Name + ",frag_name = " + frag_name);
        switch (frag_name) {
            case "weather":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                    }
                }
                break;
            case "calendar":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
                        case "weather":
                            weatherFragment = null;break;
                        case "music":
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                    }
                }
                break;
            case "music":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
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
                    }
                }
                break;
            case "shopping":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            musicFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                    }
                }
                break;
            case "map":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "setting":
                            settingFragment = null;break;
                    }
                }
                break;
            case "setting":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
                        case "weather":
                            weatherFragment = null;break;
                        case "calendar":
                            calendarFragment = null;break;
                        case "music":
                            musicFragment = null;break;
                        case "shopping":
                            shoppingFragment = null;break;
                        case "map":
                            mapFragment = null;break;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

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
            mSharePrefrenceUtils = null;
        }
        if (mShoppingBroadcastIntent != null) {
            mShoppingBroadcastIntent = null;
        }
        isDrawer = false;
        unbindService(mServiceConnection);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "");
            super.onBackPressed();
        }
    }

    public class ControllerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mControllerBinder = (ControllerService.MyBinder) iBinder;

            //shopping语音处理
            mControllerBinder.onReturnWeburl(new ShoppingCallBack() {
                @Override
                public void onShoppingCallback(String web_url) {
                    Log.i("ryan", "onShoppingCallback: " + web_url);
                    revokeSwipeShoppingVoice(web_url);
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
