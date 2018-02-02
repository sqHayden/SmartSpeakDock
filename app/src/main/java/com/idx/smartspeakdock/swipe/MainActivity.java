package com.idx.smartspeakdock.swipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.service.SpeakerService;
import com.idx.smartspeakdock.shopping.shoproom.entity.Shopping;
import com.idx.smartspeakdock.shopping.util.ParseXMLUtils;
import com.idx.smartspeakdock.utils.ActivityStatusUtils;
import com.idx.smartspeakdock.utils.AppExecutors;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.utils.PreUtils;
import com.lljjcoder.style.citypickerview.CityPickerView;

import java.util.List;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class MainActivity extends BaseActivity {
    private final String TAG = "MainActivity";
    public DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private CoordinatorLayout right;
    private NavigationView left;
    //侧滑是否已开启
    private boolean isDrawer;
    private AppExecutors mAppExecutors;
    List<Shopping> mShoppings;
    private String extraIntentId;

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
            //启动service
            startService(mControllerintent);
            //绑定service
            bindService(mControllerintent, myServiceConnection, Context.BIND_AUTO_CREATE);
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
            boolean isFirstChange = mSharePrefrenceUtils.getFirstChange(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT);
            Log.i("ryan", "onCreate: main:isFirstChange = "+isFirstChange);
            if (isFirstChange) {
                changeFragment(extraIntentId);
            }
            mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, false);
        } else {
            //待机界面
            Fragment content_ragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
            if (content_ragment == null) {
                initStandBy();
                //voice flag
                mMap_voice_flag = -1;
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
                mMap_voice_flag = 6;
                Bundle bundle = getIntent().getBundleExtra("map");
                //拿到
                if(bundle!=null){
                    mMap_voice_name = bundle.getString("name");
                    mMap_voice_address = bundle.getString("address");
                    mMap_voice_fromAddress = bundle.getString("fromAddress");
                    mMap_voice_toAddress = bundle.getString("toAddress");
                    mMap_voice_pathWay = bundle.getString("pathWay");
                }
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
        mShoppingBroadcastIntent = new Intent(GlobalUtils.Shopping.SHOPPING_BROADCAST_ACTION);
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
                                mMap_voice_flag = -1;
                                Log.d("11111","drawer:weather"+mMap_voice_flag);
                                initWeather();
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CalendarFragment
                                mMap_voice_flag = -1;
                                initCalendar();
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MusicFragment
                                mMap_voice_flag = -1;
                                initMusic();
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 ShoppingFragment
                                mMap_voice_flag = -1;
                                initShopping("https://mall.flnet.com");
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MapFragemnt
                                Log.d("11111","mMapFlag:drawer"+mMap_voice_flag);
                                mMap_voice_flag = -1;
                                initMap();
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SettingFargment
                                mMap_voice_flag = -1;
                                initSetting();
                                break;
                            default:
                                break;
                        }
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mActionBar.setTitle(actionBar_title);
                        mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, false);
                        return true;
                    }
                });
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
    protected void onDestroy() {
        super.onDestroy();
        isDrawer = false;
        if (mAppExecutors != null) { mAppExecutors = null;}
        if (mShoppings != null) {
            mShoppings.clear();
            mShoppings = null;
        }
        ActivityStatusUtils.onDestroy();
        if (standByFragment != null) { standByFragment = null;}
        if (mControllerintent != null) { mControllerintent = null;}
        if (weatherFragment != null) { weatherFragment = null;}
        if (calendarFragment != null) { calendarFragment = null;}
        if (musicFragment != null) { musicFragment = null;}
        if (shoppingFragment != null) { shoppingFragment = null;}
        if (mapFragment != null) { mapFragment = null;}
        if (settingFragment != null) { settingFragment = null;}
        if (mSharePrefrenceUtils != null) {
            mSharePrefrenceUtils = null;
        }
        if (mShoppingBroadcastIntent != null) { mShoppingBroadcastIntent = null;}
        if (mShoppingBroadcastIntent != null) { mWeatherBroadcastIntent = null; }
        mWeather_voice_flag = -1;
//        mMap_voice_flag = -1;
        if (mWeather_return_voice != null) { mWeather_return_voice = null;}
        if (mMap_result_callback!=null) { mMap_result_callback = null;}
        music_name = null;
        //解绑ControllerService
//        unbindService(myServiceConnection);
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
                mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, false);
                initStandBy();
                mActionBar.setTitle(actionBar_title);
            }else{
                Log.i(TAG, "onBackPressed: standy");
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
//                finish();
                super.onBackPressed();
            }
        }
    }
}
