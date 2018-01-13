package com.idx.smartspeakdock.Swipe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.idx.smartspeakdock.music.activity.ListFragment;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

import java.util.ArrayList;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class SwipeActivity extends BaseActivity {
    private static final String TAG = SwipeActivity.class.getSimpleName();
    Toolbar mToolbar;
    ActionBar mActionBar;
    Resources mResources;
    private DrawerLayout mDrawerLayout;
    private Intent intent;
    private WeatherFragment weatherFragment;
    private CalendarFragment calendarFragment;
    private ListFragment musicFragment;
    private ShoppingFragment shoppingFragment;
    private MapFragment mapFragment;
    private SettingFragment settingFragment;
    private CoordinatorLayout right;
    private NavigationView left;
    private boolean isDrawer = false;
    private String extraIntentId;
    private Fragment mCurrentFragment;
    private String websites_url;
    private String actionBar_title;
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SwipeActivity", "onCreate()...");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer_main);
        Logger.setEnable(true);
        extraIntentId = getIntent().getStringExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT);
        initToolBar();
        initDrawer();
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
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
                    if (mCurrentFragment instanceof WeatherFragment)
                        weatherFragment = (WeatherFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), weatherFragment, R.id.contentFrame);
                break;
            case GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    calendarFragment = CalendarFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof CalendarFragment)
                        calendarFragment = (CalendarFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), calendarFragment, R.id.contentFrame);
                break;
            case GlobalUtils.MUSIC_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    musicFragment = ListFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof ListFragment)
                        musicFragment = (ListFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), musicFragment, R.id.contentFrame);
                break;
            case GlobalUtils.MAP_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    mapFragment = new MapFragment();
                } else {
                    if (mCurrentFragment instanceof MapFragment)
                        mapFragment = (MapFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), mapFragment, R.id.contentFrame);
                break;
            case GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(websites_url);
                } else {
                    if (mCurrentFragment instanceof ShoppingFragment)
                        shoppingFragment = (ShoppingFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
                break;
            case GlobalUtils.SETTING_FRAGMENT_INTENT_ID:
                if (settingFragment == null) {
                    settingFragment = SettingFragment.newInstance();
                } else {
                    if (mCurrentFragment instanceof SettingFragment)
                        settingFragment = (SettingFragment) mCurrentFragment;
                }
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
    }

    private void initToolBar() {
        mResources = getResources();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        setActionBarTitle();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        websites_url = "https://mall.flnet.com";
    }

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
                                actionBar_title = mResources.getString(R.string.weather_title);
                                if (weatherFragment == null) {
                                    weatherFragment = WeatherFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), weatherFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CALENDAR
                                actionBar_title = mResources.getString(R.string.calendar_title);
                                if (calendarFragment == null) {
                                    calendarFragment = CalendarFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), calendarFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MUSIC
                                actionBar_title = mResources.getString(R.string.music_title);
                                if (musicFragment == null) {
                                    musicFragment = ListFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), musicFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 SHOPPING
                                actionBar_title = mResources.getString(R.string.shopping_title);
                                if (shoppingFragment == null){
                                    shoppingFragment = ShoppingFragment.newInstance(websites_url);
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MAP
                                actionBar_title = mResources.getString(R.string.map_title);
                                if (mapFragment == null) {
                                    mapFragment = new MapFragment();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), mapFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SETTING
                                actionBar_title = mResources.getString(R.string.setting_title);
                                if (settingFragment == null) {
                                    settingFragment = SettingFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), settingFragment, R.id.contentFrame);
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
                    if (musicFragment.mIsPlaying) {
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
