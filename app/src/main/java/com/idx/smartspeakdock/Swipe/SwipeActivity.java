package com.idx.smartspeakdock.Swipe;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;


import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.calendar.CalendarActivity;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.music.MusicMainActivity;
import com.idx.smartspeakdock.standby.StandbyActivity;
import com.idx.smartspeakdock.start.StartActivity;
import com.idx.smartspeakdock.weather.ui.WeatherActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SwipeActivity extends AppCompatActivity {
    private static final String TAG = SwipeActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    Timer timer;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.swipe_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorSelfBlack);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
      /*  ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels * 1 / 2;
        navigationView.setLayoutParams(params);*/
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        /*
        ShoppingFragment shoppingFragment =
                (ShoppingFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (shoppingFragment == null) {
            shoppingFragment = ShoppingFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), shoppingFragment, R.id.contentFrame);
        }
        */
        intent = new Intent(this, StandbyActivity.class);
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(intent);
            }
        };
        timer.schedule(task, 60 * 1000);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                                startActivity(new Intent(SwipeActivity.this, WeatherActivity.class));
                                timer.cancel();
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 start CalendarActivity
                                startActivity(new Intent(SwipeActivity.this, CalendarActivity.class));
                                timer.cancel();
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16 start MusicActivity
                                startActivity(new Intent(SwipeActivity.this, MusicMainActivity.class));
                                timer.cancel();
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 start ShoppingActivty
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.flnet.com"));
                                //List<ResolveInfo> list = pm.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
                                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                                Log.i(TAG, "onNavigationItemSelected: list.size() = "+list.size());
                                if (list.size() > 0) {
                                    Log.i(TAG, "onNavigationItemSelected: start");
                                    startActivity(intent);
                                }
                                timer.cancel();
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 start MapActivity
                                startActivity(new Intent(SwipeActivity.this, MapActivity.class));
                                timer.cancel();
                                break;
                            case R.id.list_navigation_voice:
                                // TODO: 17-12-16 start voice function
                                startActivity(new Intent(SwipeActivity.this, StartActivity.class));
                                timer.cancel();
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 start SettingActivity
                                timer.cancel();
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
}
