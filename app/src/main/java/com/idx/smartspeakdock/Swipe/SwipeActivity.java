package com.idx.smartspeakdock.Swipe;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.CalendarActivity;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.music.activity.ListActivity;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.standby.StandbyActivity;
import com.idx.smartspeakdock.start.StartActivity;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.ToastUtils;
import com.idx.smartspeakdock.weather.model.weather.Weather;
import com.idx.smartspeakdock.weather.ui.ChooseCityDialogFragment;
import com.idx.smartspeakdock.weather.ui.WeatherUi;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ryan on 17-12-22.
 * Email: Ryan_chan01212@yeah.net
 */

public class SwipeActivity extends AppCompatActivity implements OnSelectCityListener,ChooseCityDialogFragment.OnChooseCityCompleted,WeatherUi {
    private static final String TAG = SwipeActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    private TextView mTitle;
//    Timer timer;
    private Intent intent;
    private SwipeFragment swipeFragment;
    private ShoppingFragment shoppingFragment;
    private String mCurrentCity = "深圳";
    private String mCurrentCounty = "深圳";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.swipe_main);
        initToolBar();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorSelfBlack);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //set self color
        //navigationView.setItemTextColor(null);
        navigationView.setItemIconTintList(null);
//        navigationView.setItemBackground(getResources().getDrawable(R.drawable.background));
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        swipeFragment =
                (SwipeFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (swipeFragment == null) {
            swipeFragment = SwipeFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), swipeFragment, R.id.contentFrame);
        }

        SplashScreen();
    }

    private void SplashScreen() {
        intent = new Intent(this, StandbyActivity.class);
        /*timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(intent);
            }
        };
        timer.schedule(task, 60 * 1000);*/
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
//                                timer.cancel();
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 start CalendarActivity
                                startActivity(new Intent(SwipeActivity.this, CalendarActivity.class));
//                                timer.cancel();
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 start MusicActivity
                                startActivity(new Intent(SwipeActivity.this, ListActivity.class));
//                                timer.cancel();
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 start ShoppingActivty
                                if(shoppingFragment == null) {
                                    shoppingFragment = ShoppingFragment.newInstance();
                                }
                                ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),shoppingFragment,R.id.contentFrame);
//                                timer.cancel();
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 start MapActivity
                                startActivity(new Intent(SwipeActivity.this, MapActivity.class));
//                                timer.cancel();
                                break;
                            case R.id.list_navigation_voice:
                                // TODO: 17-12-16 start voice function
                                startActivity(new Intent(SwipeActivity.this, StartActivity.class));
//                                timer.cancel();
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 start SettingActivity
//                                timer.cancel();
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

    @Override
    public void onSelectCity(View view) {
        ChooseCityDialogFragment cityDialogFragment=new ChooseCityDialogFragment();
        cityDialogFragment.setOnChooseCityCompleted(SwipeActivity.this);
        cityDialogFragment.show(getFragmentManager(),"ChooseCityDialog");
    }

    @Override
    public void OnInitView(View view) {
        mTitle= view.findViewById(R.id.weather_title);
    }

    @Override
    public void chooseCityCompleted(String countyName, String cityNime) {
        if(swipeFragment != null) swipeFragment.mWeatherPresenter.getWeather(cityNime);
        Log.d(TAG,cityNime);
        mCurrentCounty = countyName;
        mCurrentCity = cityNime;
        if (cityNime.equals("东城")||cityNime.equals("西城")){
            cityNime="北京";
            mCurrentCity = "北京";
        }else if (cityNime.equals("黄浦")||cityNime.equals("长宁")||
                cityNime.equals("静安")||cityNime.equals("普陀")||
                cityNime.equals("虹口")||cityNime.equals("杨浦")){
            cityNime="上海";
            mCurrentCity = "上海";
        }else if (cityNime.equals("和平")||cityNime.equals("河东")||
                cityNime.equals("河西")||cityNime.equals("南开")||
                cityNime.equals("河北")||cityNime.equals("红桥")){
            cityNime="天津";
            mCurrentCity = "天津";
        }else if (cityNime.equals("渝中")||cityNime.equals("大渡口")||
                cityNime.equals("江北")||cityNime.equals("沙坪坝")||
                cityNime.equals("九龙坡")||cityNime.equals("南岸")||cityNime.equals("开州")){
            cityNime="重庆";
            mCurrentCity = "重庆";
        }
        Log.d(TAG, "chooseCityCompleted: "+cityNime);
        if (!(cityNime.equals("香港") || cityNime.equals("澳门") || cityNime.equals("台北") || cityNime.equals("高雄") || cityNime.equals("台中"))) {
            if(swipeFragment != null) swipeFragment.mWeatherPresenter.getWeatherAqi(cityNime);
        }
        mTitle.setText(countyName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("city", mCurrentCity);
        outState.putString("county", mCurrentCounty);
    }

    @Override
    public void showLoading() {
        if(swipeFragment != null) swipeFragment.loading();
    }

    @Override
    public void hideLoading() {
        if(swipeFragment != null) swipeFragment.compelete();
    }

    @Override
    public void showError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showError(getApplicationContext(),getResources().getString(R.string.get_weather_info_error));
            }
        });
    }

    @Override
    public void setWeatherInfo(final Weather weather) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(swipeFragment != null) swipeFragment.updateWeatherINfo(weather);
            }
        });
    }

    @Override
    public void setWeatherAqi(final Weather weather) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(swipeFragment != null) swipeFragment.updateWeatherAqi(weather);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(swipeFragment != null) swipeFragment = null;
        if(shoppingFragment != null) shoppingFragment = null;
    }
}
