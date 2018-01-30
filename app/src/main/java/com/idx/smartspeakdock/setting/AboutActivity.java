package com.idx.smartspeakdock.setting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.swipe.SwipeActivity;
import com.idx.smartspeakdock.utils.BitmapUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;

/**
 * Created by ryan on 18-1-5.
 * Email: Ryan_chan01212@yeah.net
 */

public class AboutActivity extends BaseActivity {
    private static final String TAG = AboutActivity.class.getSimpleName();
    private Toolbar toolbar;
    private TextView app_version_show;
    private Bitmap mBitmap;
    private SharePrefrenceUtils mSharedPreferencesUtils;
    private ControllerServiceConnection mControlConnection;
    private ControllerService.MyBinder mControlBinder;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //绑定ControllerSevice
        Intent intent = new Intent(this,ControllerService.class);
        mControlConnection = new ControllerServiceConnection();
        bindService(intent,mControlConnection,0);
        //实例化SharePreferencesUtls
        mSharedPreferencesUtils = new SharePrefrenceUtils(this);
        mIntent = new Intent(this, SwipeActivity.class);
        initToolbar();
        initView();
    }

    private void initView() {
        app_version_show = findViewById(R.id.app_version);
        mBitmap = BitmapUtils.decodeBitmapFromResources(this,R.drawable.back);
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo("com.idx.smartspeakdock",0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        app_version_show.setText(getResources().getString(R.string.version)+" v" + packageInfo.versionName);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorSelfBlack));
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        mBitmap = BitmapUtils.scaleBitmapFromResources(this,R.drawable.back,15,30);
        ab.setHomeAsUpIndicator(new BitmapDrawable(mBitmap));
        ab.setTitle(getResources().getString(R.string.about));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null){
            mBitmap.recycle();
            mBitmap = null;
        }
        unbindService(mControlConnection);
        mControlConnection = null;
        if (mIntent != null) {
            mIntent = null;
        }
    }

    public class ControllerServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: AboutAcitivy服务绑定");
            mControlBinder = (ControllerService.MyBinder) iBinder;

            //shopping语音处理
            mControlBinder.onReturnWeburl(new ShoppingCallBack() {
                @Override
                public void onShoppingCallback(String web_url) {
                    revokeMainShoppingVoice(web_url);
                }
            });
            //calendar语音处理
            mControlBinder.setCalendarControllerListener(new CalendarCallBack() {
                @Override
                public void onCalendarCallBack() {
                    revokeMainCalendarVoice();
                }
            });
            //weather语音处理
            mControlBinder.setWeatherControllerListener(new WeatherCallback() {
                @Override
                public void onWeatherCallback(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
                    revokeMainWeatherVoice(cityName, time, returnVoice, func_flag, flag);
                }
            });
            //music语音处理
            mControlBinder.onGetMusicName(new MusicCallBack() {
                @Override
                public void onMusicCallBack(String music_name) {
                    revokeMainMusicVoice(music_name);
                }
            });
            //map语音处理
            mControlBinder.setMapControllerListener(new MapCallBack() {
                @Override
                public void onMapCallBack(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback result) {
                    revokeMainMapVoice(name,address,fromAddress,toAddress,pathWay,result);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mControlBinder != null){
                mControlBinder = null;
            }
        }
    }

    private void revokeMainShoppingVoice(String web_url) {
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainShoppingVoice: 当前Activity不是SwipeActivity");
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_INTENT_ID);
            mIntent.putExtra("weburl", web_url);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
            AboutActivity.this.finish();
        }
    }

    private void revokeMainCalendarVoice() {
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainCalendarVoice: 当前Activity不是SwipeActivity");
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
            AboutActivity.this.finish();
        }
    }


    private void revokeMainWeatherVoice(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainWeatherVoice: 当前Activity不是SwipeActivity");
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID);
            Bundle args = new Bundle();
            args.putString("cityname", cityName);
            args.putString("time", time);
            args.putString("fun_flag", func_flag);
            args.putInt("voice_flag", flag);
            mIntent.putExtra("weather", args);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
            AboutActivity.this.finish();
        }
    }

    private void  revokeMainMusicVoice(String music_name){
        if (!isActivityTop){
            Log.d(TAG, "music222: " + music_name);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID);
            mIntent.putExtra("music_name",music_name);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
            AboutActivity.this.finish();
        }
    }

    private void revokeMainMapVoice(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback result){
        if (!isActivityTop) {
            Log.i(TAG, "revokeMainMapVoice: 当前Activity不是SwipeActivity");
            mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID);
            Bundle args = new Bundle();
            args.putString("name", name);
            args.putString("address", address);
            args.putString("fromAddress", fromAddress);
            args.putString("toAddress", toAddress);
            args.putString("pathWay",pathWay);
            mIntent.putExtra("map", args);
            startActivity(mIntent);
            mSharedPreferencesUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
            AboutActivity.this.finish();
        }
    }
}
