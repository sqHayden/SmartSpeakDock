package com.idx.smartspeakdock.Swipe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.music.activity.ListFragment;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.presenter.OnWeatherListener;
import com.idx.smartspeakdock.weather.ui.ChooseCityDialogFragment;
import com.idx.smartspeakdock.weather.utils.WeatherUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class SwipeActivity extends BaseActivity {
    private static final String TAG = SwipeActivity.class.getSimpleName();
    Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    //    Timer timer;
    private Intent intent;
    private SwipeFragment swipeFragment;
    private CalendarFragment calendarFragment;
    private ListFragment musicFragment;
    private ShoppingFragment shoppingFragment;
    private CoordinatorLayout right;
    private NavigationView left;
    private boolean isDrawer = false;
    private String extraIntentId;
    private Fragment mCurrentFragment;
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.drawer_main);
        Logger.setEnable(true);
        initToolBar();
        initDrawer();
        extraIntentId = getIntent().getStringExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT);
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        changeFragment(extraIntentId);
    }
    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyOnTouchListener listener : onTouchListeners) {
            if(listener != null) {
                listener.onTouch(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    public void registerMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }
    public void unregisterMyOnTouchListener(MyOnTouchListener myOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener) ;
    }

    private void changeFragment(String extraIntentId) {
        Logger.info(TAG, extraIntentId);
        switch (extraIntentId) {
            case GlobalUtils.WEATHER_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) swipeFragment = SwipeFragment.newInstance();
                else {
                    if (mCurrentFragment instanceof SwipeFragment)
                        swipeFragment = (SwipeFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), swipeFragment, R.id.contentFrame);
                break;
            case GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) calendarFragment = CalendarFragment.newInstance();
                else {
                    if (mCurrentFragment instanceof CalendarFragment)
                        calendarFragment = (CalendarFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), calendarFragment, R.id.contentFrame);
                break;
            case GlobalUtils.MUSIC_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) musicFragment = ListFragment.newInstance();
                else{
                    if (mCurrentFragment instanceof ListFragment)
                        musicFragment = (ListFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),musicFragment,R.id.contentFrame);
                break;
            case GlobalUtils.MAP_FRAGMENT_INTENT_ID:
                break;
            case GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID:
                if (mCurrentFragment == null) shoppingFragment = ShoppingFragment.newInstance();
                else {
                    if (mCurrentFragment instanceof ShoppingFragment)
                        shoppingFragment = (ShoppingFragment) mCurrentFragment;
                }
                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
                break;
            case GlobalUtils.START_FRAGMENT_INTENT_ID:
                break;
            case GlobalUtils.SETTING_FRAGMENT_INTENT_ID:
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
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
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
                                // TODO: 17-12-16  Do nothing, we're already on that screen
                                if (swipeFragment == null) {
                                    swipeFragment = SwipeFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(
                                        getSupportFragmentManager(), swipeFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 start CalendarActivity
                                if (calendarFragment == null) {
                                    calendarFragment = CalendarFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), calendarFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 start MusicActivity
                                if (musicFragment == null) {
                                    musicFragment = ListFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),musicFragment,R.id.contentFrame);
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 start ShoppingActivty
                                if (shoppingFragment == null) {
                                    shoppingFragment = ShoppingFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 start MapActivity
                                startActivity(new Intent(SwipeActivity.this, MapActivity.class));
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 start SettingActivity
                                break;
                            default:
                                break;
                        }

                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
/*
    @Override
    public void finish() {
        moveTaskToBack(false);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(musicFragment != null) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (swipeFragment != null) swipeFragment = null;
        if (calendarFragment != null) calendarFragment = null;
        if (musicFragment != null) musicFragment = null;
        if (shoppingFragment != null) shoppingFragment = null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
