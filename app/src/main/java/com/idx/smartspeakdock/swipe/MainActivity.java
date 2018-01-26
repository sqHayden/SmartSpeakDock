package com.idx.smartspeakdock.swipe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.service.SpeakerService;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.shopping.shoproom.entity.Shopping;
import com.idx.smartspeakdock.standby.StandByFragment;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.AppExecutors;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.PreUtils;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 17-12-27.
 * Email: Ryan_chan01212@yeah.net
 */

public class MainActivity extends BaseActivity {
    private final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private Intent mIntent;
    private Intent mControllerintent;
    private StandByFragment standByFragment;
    private Toolbar toolbar;
    private CoordinatorLayout right;
    private NavigationView left;
    //侧滑是否已开启
    private boolean isDrawer;
    //语音注册监听器service
    private MyServiceConnection myServiceConnection;
    private ControllerService.MyBinder mControllerBinder;
    private SharePrefrenceUtils mSharedPreferencesUtils;
    private AppExecutors mAppExecutors;
    List<Shopping> mShoppings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);
        initToolBar();
        //侧滑栏配置
        initDrawer();
        mIntent = new Intent(MainActivity.this, SwipeActivity.class);
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

        //实例化SharePreferencesUtls
        mSharedPreferencesUtils = new SharePrefrenceUtils(this);
        //线程池
        mAppExecutors = new AppExecutors();
    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorSelfBlack));
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.menu_left);
        ab.setTitle("");
        ab.setDisplayHomeAsUpEnabled(true);
        //待机界面
        standByFragment =
                (StandByFragment) mFragmentManager.findFragmentById(R.id.contentFrame);
        if (standByFragment == null) {
            standByFragment = new StandByFragment();
            ActivityUtils.addFragmentToActivity(
                    mFragmentManager, standByFragment, R.id.contentFrame);
        }
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
                                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 CalendarFragment
                                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 MusicFragment
                                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 ShoppingFragment
                                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_INTENT_ID);
                                mIntent.putExtra("weburl", "https://mall.flnet.com");
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 MapFragemnt
                                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID);
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 SettingFargment
                                Log.i("ryan", "main onNavigationItemSelected: settingFragment");
                                mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.SETTING_FRAGMENT_INTENT_ID);
                                break;
                            default:
                                break;
                        }
                        startActivity(mIntent);
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
                        return true;
                    }
                });
    }

    public void isAppFirstStart() {
        if (mSharedPreferencesUtils.getFirstAppStart(GlobalUtils.FirstSatrt.FIRST_APP_START)) {
            Log.i(TAG, "isAppFirstStart: isFirst = " + mSharedPreferencesUtils.getFirstAppStart(GlobalUtils.FirstSatrt.FIRST_APP_START));
            mSharedPreferencesUtils.saveFirstAppStart(GlobalUtils.FirstSatrt.FIRST_APP_START, false);
            mAppExecutors.getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mShoppings = readXMLPull();
                    for (int i = 0; i < mShoppings.size(); i++) {
                        Shopping shopping = mShoppings.get(i);
                        mSharedPreferencesUtils.insertWebUrl(shopping.getWebName(), shopping.getWebUrl());
                    }
                }
            });
        }
    }

    //weburl xml资源解析
    public List<Shopping> readXMLPull() {
        XmlResourceParser parser = getResources().getXml(R.xml.shopurls);
        try {
            int eventType = parser.getEventType();
            Shopping curr_shopping = null;
            List<Shopping> shoppings = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        shoppings = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (name.equalsIgnoreCase("shopping")) {
                            curr_shopping = new Shopping();
//                            curr_shopping.setId(new Integer(parser.getAttributeValue(null, "id")));
                        } else if (curr_shopping != null) {
                            if (name.equalsIgnoreCase("webname")) {
                                // 如果后面是Text元素,即返回它的值
                                curr_shopping.setWebName(parser.nextText());
                            } else if (name.equalsIgnoreCase("weburl")) {
                                curr_shopping.setWebUrl(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equalsIgnoreCase("shopping") && curr_shopping != null) {
                            shoppings.add(curr_shopping);
                            curr_shopping = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
            return shoppings;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (standByFragment != null) {
            standByFragment = null;
        }
        isDrawer = false;
        if (mSharedPreferencesUtils != null) {
            mSharedPreferencesUtils = null;
        }
        if (mAppExecutors != null) {
            mAppExecutors = null;
        }
        if (mShoppings != null) {
            mShoppings.clear();
            mShoppings = null;
        }
        if (mIntent != null) {
            mIntent = null;
        }
        //停止ControllerService
        unbindService(myServiceConnection);
//        stopService(mControllerintent);
        if (mControllerintent != null) {
            mControllerintent = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                    Log.i(TAG, "onShoppingCallback: " + web_url);
                    revokeMainShoppingVoice(web_url);
                }
            });
            //calendar语音处理
            mControllerBinder.setCalendarControllerListener(new CalendarCallBack() {
                @Override
                public void onCalendarCallBack() {
                    revokeMainCalendarVoice();
                }
            });
            //weather语音处理
            mControllerBinder.setWeatherControllerListener(new WeatherCallback() {
                @Override
                public void onWeatherCallback(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
                    Log.i(TAG, "onWeatherCallback: ");
                    revokeMainWeatherVoice(cityName, time, returnVoice, func_flag, flag);
                }
            });
            //music语音处理
            mControllerBinder.onGetMusicName(new MusicCallBack() {
                @Override
                public void onMusicCallBack(String music_name) {
                    revokeMainMusicVoice(music_name);
                }
            });
            //map语音处理
            mControllerBinder.setMapControllerListener(new MapCallBack() {
                @Override
                public void onMapCallBack(String name, String address, String fromAddress, String toAddress, PathWay pathWay, ResultCallback result) {
                    revokeMainMapVoice(name,address,fromAddress,toAddress,pathWay,result);
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

    private void revokeMainShoppingVoice(String web_url) {
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainShoppingVoice: 当前Activity不是SwipeActivity");
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_INTENT_ID);
            mIntent.putExtra("weburl", web_url);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        }
    }

    private void revokeMainCalendarVoice() {
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainCalendarVoice: 当前Activity不是SwipeActivity");
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        }
    }


    private void revokeMainWeatherVoice(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainWeatherVoice: 当前Activity不是SwipeActivity");
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID);
            Bundle args = new Bundle();
            args.putString("cityname", cityName);
            args.putString("time", time);
            args.putString("fun_flag", func_flag);
            args.putInt("voice_flag", flag);
            mIntent.putExtra("weather", args);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        }
    }

    private void  revokeMainMusicVoice(String music_name){
        if (!isActivityTop){
            Log.d(TAG, "music222: " + music_name);
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID);
            mIntent.putExtra("music_name",music_name);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        }
    }

    private void revokeMainMapVoice(String name, String address, String fromAddress, String toAddress, PathWay pathWay, ResultCallback result){
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainMapVoice: 当前Activity不是SwipeActivity");
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID);
            Bundle args = new Bundle();
            args.putString("name", name);
            args.putString("address", address);
            args.putString("fromAddress", fromAddress);
            args.putString("toAddress", toAddress);
            if(pathWay==null){
                Log.d("pathWay","是空的");
                args.putString("pathWay","");
            }else{
                Log.d("pathWay不为空，是：",pathWay.getDesc());
                args.putString("pathWay",pathWay.getDesc());
            }
            mIntent.putExtra("map", args);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        }
    }

}
