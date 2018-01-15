package com.idx.smartspeakdock.Swipe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
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
    private Fragment mCurrentFragment;
    private CoordinatorLayout right;
    private NavigationView left;
    private boolean isDrawer;
    private String extraIntentId;
    private String websites_url;
    private String actionBar_title;
    private SharePrefrenceUtils mSharePrefrenceUtils;
    private String mCurr_Frag_Name;
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SwipeActivity", "onCreate()...");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer_main);
        Logger.setEnable(true);
        //对应fragment的id
        extraIntentId = getIntent().getStringExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT);
        initToolBar();
        //侧滑设置
        initDrawer();
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        //fragment切换
        changeFragment(extraIntentId);
    }

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
                if (mCurrentFragment == null) {
                    weatherFragment = WeatherFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof WeatherFragment) {
                        weatherFragment = (WeatherFragment) mCurrentFragment;
                    }
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "weather");
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), weatherFragment, R.id.contentFrame);
                break;
            case GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    calendarFragment = CalendarFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof CalendarFragment)
                        calendarFragment = (CalendarFragment) mCurrentFragment;
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "calendar");
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), calendarFragment, R.id.contentFrame);
                break;
            case GlobalUtils.MUSIC_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    musicFragment = MusicListFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof MusicListFragment)
                        musicFragment = (MusicListFragment) mCurrentFragment;
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "music");
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), musicFragment, R.id.contentFrame);
                break;
            case GlobalUtils.MAP_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    mapFragment = new MapFragment();
                } else {
                    if (mCurrentFragment instanceof MapFragment)
                        mapFragment = (MapFragment) mCurrentFragment;
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "map");
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), mapFragment, R.id.contentFrame);
                break;
            case GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(websites_url);
                } else {
                    if (mCurrentFragment instanceof ShoppingFragment)
                        shoppingFragment = (ShoppingFragment) mCurrentFragment;
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "shopping");
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
                break;
            case GlobalUtils.SETTING_FRAGMENT_INTENT_ID:
                if (settingFragment == null) {
                    settingFragment = SettingFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof SettingFragment)
                        settingFragment = (SettingFragment) mCurrentFragment;
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID, "setting");
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), settingFragment, R.id.contentFrame);
                break;
            default:
                break;
        }
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
    }

    private void initToolBar() {
        mResources = getResources();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        //切换ActionBar title
        setActionBarTitle();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        websites_url = "https://mall.flnet.com";
    }

    //切换ActionBar的Title
    private void setActionBarTitle() {
        switch (extraIntentId) {
            case GlobalUtils.WEATHER_FRAGMENT_INTENT_ID:
                actionBar_title = mResources.getString(R.string.weather_title);
                break;
            case GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID:
                actionBar_title = mResources.getString(R.string.calendar_title);
                break;
            case GlobalUtils.MUSIC_FRAGMENT_INTENT_ID:
                actionBar_title = mResources.getString(R.string.music_title);
                break;
            case GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID:
                actionBar_title = mResources.getString(R.string.shopping_title);
                break;
            case GlobalUtils.MAP_FRAGMENT_INTENT_ID:
                actionBar_title = mResources.getString(R.string.map_title);
                break;
            case GlobalUtils.SETTING_FRAGMENT_INTENT_ID:
                actionBar_title = mResources.getString(R.string.setting_title);
                break;
            default:
                break;
        }
        mActionBar.setTitle(actionBar_title);
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
                                if (!checkFragment("weather")){
                                    actionBar_title = mResources.getString(R.string.weather_title);
                                    if (weatherFragment == null) {
                                        weatherFragment = WeatherFragment.newInstance();
                                    }
                                    ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), weatherFragment, R.id.contentFrame);
                                }
                                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID,"weather");
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CALENDAR
                                if (!checkFragment("calendar")) {
                                    actionBar_title = mResources.getString(R.string.calendar_title);
                                    if (calendarFragment == null) {
                                        calendarFragment = CalendarFragment.newInstance();
                                    }
                                    ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), calendarFragment, R.id.contentFrame);
                                }
                                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID,"calendar");
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MUSIC
                                if (!checkFragment("music")) {
                                    actionBar_title = mResources.getString(R.string.music_title);
                                    if (musicFragment == null) {
                                        musicFragment = MusicListFragment.newInstance();
                                    }
                                    ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), musicFragment, R.id.contentFrame);
                                }
                                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID,"music");
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 SHOPPING
                                if (!checkFragment("shopping")) {
                                    actionBar_title = mResources.getString(R.string.shopping_title);
                                    if (shoppingFragment == null) {
                                        shoppingFragment = ShoppingFragment.newInstance(websites_url);
                                    }
                                    ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
                                }
                                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID,"shopping");
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MAP
                                if (!checkFragment("map")) {
                                    actionBar_title = mResources.getString(R.string.map_title);
                                    if (mapFragment == null) {
                                        mapFragment = new MapFragment();
                                    }
                                    ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), mapFragment, R.id.contentFrame);
                                }
                                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID,"map");
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SETTING
                                if (!checkFragment("setting")) {
                                    actionBar_title = mResources.getString(R.string.setting_title);
                                    if (settingFragment == null) {
                                        settingFragment = SettingFragment.newInstance();
                                    }
                                    ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), settingFragment, R.id.contentFrame);
                                }
                                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.CURRENT_FRAGMENT_ID,"setting");
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
                                        SwipeActivity.super.finish();
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
        Log.i(TAG, "checkFragment: frag_name_curr = " + mCurr_Frag_Name+",frag_name = "+frag_name);
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
                            calendarFragment = null;break;
                        case "calendar":
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
            case "shopping":
                if (mCurr_Frag_Name.equals(frag_name)) {
                    return true;
                } else {
                    switch (mCurr_Frag_Name){
                        case "":
                            calendarFragment = null;break;
                        case "calendar":
                            musicFragment = null;break;
                        case "music":
                            shoppingFragment = null;break;
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
                            calendarFragment = null;break;
                        case "calendar":
                            musicFragment = null;break;
                        case "music":
                            shoppingFragment = null;break;
                        case "shopping":
                            mapFragment = null;break;
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
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weatherFragment != null) { weatherFragment = null;}
        if (calendarFragment != null) { calendarFragment = null;}
        if (musicFragment != null) { musicFragment = null;}
        if (shoppingFragment != null) { shoppingFragment = null;}
        if (mapFragment != null) { mapFragment = null;}
        if (settingFragment != null) { settingFragment = null;}
        if (mCurrentFragment != null) { mCurrentFragment = null;}
        if (mSharePrefrenceUtils != null) { mSharePrefrenceUtils = null;}
        isDrawer = false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else super.onBackPressed();
    }

    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }
}
