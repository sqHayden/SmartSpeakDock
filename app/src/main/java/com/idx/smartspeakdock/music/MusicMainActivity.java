package com.idx.smartspeakdock.music;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeFragment;
import com.idx.smartspeakdock.calendar.CalendarActivity;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.start.StartActivity;
import com.idx.smartspeakdock.utils.ActivityUtils;

import java.util.List;

public  class MusicMainActivity extends AppCompatActivity  {
private static final String TAG = MusicMainActivity.class.getSimpleName();
    private List<MusicBean> musicBeans = null;
    private MusicAdapter musicadapter;
    private ListView listview;
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.music_title));
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorSelfBlack);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        listview = (ListView) findViewById(R.id.music_list_view);
        musicBeans  = MusicUtil.getMp3Infos(MusicMainActivity.this);
        musicadapter = new MusicAdapter(this, musicBeans);
        listview.setAdapter(musicadapter);
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
//                                NavUtils.navigateUpFromSameTask(MusicMainActivity.this);
                                SwipeFragment swipeFragment =
                                        (SwipeFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                                if (swipeFragment == null) {
                                    swipeFragment = SwipeFragment.newInstance();
                                    ActivityUtils.addFragmentToActivity(
                                            getSupportFragmentManager(), swipeFragment, R.id.contentFrame);
                                }
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 start CalendarActivity
                                startActivity(new Intent(MusicMainActivity.this, CalendarActivity.class));
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16  Do nothing, we're already on that screen
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
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 start MapActivity
                                startActivity(new Intent(MusicMainActivity.this, MapActivity.class));
                                break;
                            case R.id.list_navigation_voice:
                                // TODO: 17-12-16 start voice function
                                startActivity(new Intent(MusicMainActivity.this, StartActivity.class));
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
}
