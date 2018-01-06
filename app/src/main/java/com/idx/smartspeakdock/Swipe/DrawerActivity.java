package com.idx.smartspeakdock.Swipe;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.service.SpeakerService;
import com.idx.smartspeakdock.standby.StandByFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class DrawerActivity extends BaseActivity {
    private static final String TAG = DrawerActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    private Intent mIntent;
    private StandByFragment standByFragment;
    private Toolbar toolbar;
    private CoordinatorLayout right;
    private NavigationView left;
    private boolean isDrawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);
        Logger.setEnable(true);
        initToolBar();
        initDrawer();
        standByFragment =
                (StandByFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (standByFragment == null) {
            standByFragment = standByFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), standByFragment, R.id.contentFrame);
        }

        if (!isServiceRunning(this, "com.idx.smartspeakdock.start.SpeakerService")) {
            startService(new Intent(this, SpeakerService.class));
        }
    }

    @Override
    public boolean isTopActivity() {
        isActivityTop = false;
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Logger.info(TAG, "isTopActivity = " + cn.getClassName());
        if (cn.getClassName().contains(fragment_show_activity))
        {
            isActivityTop = true;
        }
        Logger.info(TAG, "isTop = " + isActivityTop);
        return isActivityTop;
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
                if(isDrawer){
                    return left.dispatchTouchEvent(motionEvent);
                }else{
                    return false;
                }
            }
        });
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                isDrawer=true;
                WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                right.layout(left.getRight(), 0, left.getRight() + display.getWidth(), display.getHeight());
            }
            @Override
            public void onDrawerOpened(View drawerView) {}
            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawer=false;
            }
            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setTitle("");
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
        if(mIntent != null) mIntent = null;
        mIntent = new Intent(DrawerActivity.this,SwipeActivity.class);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_weather:
                                // TODO: 17-12-16  WeatherFragment
                                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.WEATHER_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CalendarFragment
                                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MusicFragment
                                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.MUSIC_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 ShoppingFragment
                               mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MapFragemnt
                                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.MAP_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SettingFargment
                                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.SETTING_FRAGMENT_INTENT_ID);
                                break;
                            default:
                                break;
                        }
                        startActivity(mIntent);
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(standByFragment != null) {
            standByFragment = null;
        }
        isDrawer = false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
