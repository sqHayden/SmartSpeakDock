package com.idx.smartspeakdock;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.calendar.service.CalendarCallBack;
import com.idx.smartspeakdock.map.Bean.MapCallBack;
import com.idx.smartspeakdock.map.Bean.ReturnMapAnswerCallBack;
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.service.MusicCallBack;
import com.idx.smartspeakdock.music.service.MusicPlay;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.setting.SettingFragment;
import com.idx.smartspeakdock.shopping.ShoppingCallBack;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.standby.ReturnCityName;
import com.idx.smartspeakdock.standby.StandByFragment;
import com.idx.smartspeakdock.swipe.MainActivity;
import com.idx.smartspeakdock.utils.ActivityStatusUtils;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.SharePrefrenceUtils;
import com.idx.smartspeakdock.weather.presenter.ReturnAnswerCallback;
import com.idx.smartspeakdock.weather.presenter.ReturnVoice;
import com.idx.smartspeakdock.weather.presenter.WeatherCallback;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

import java.util.ArrayList;

// 只用于继承
public  abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public ActionBar mActionBar;
    public Intent mControllerintent;
    public static Fragment isFragmentTop;
    public boolean isActivityTop;
    public static FragmentManager mFragmentManager;
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(10);
    public String websites_url;
    public String music_name;
    public String actionBar_title;
    public Resources mResources;
    //语音注册监听器service
    public MainActivity.MyServiceConnection myServiceConnection;
    public ControllerService.MyBinder mControllerBinder;
    public SharePrefrenceUtils mSharePrefrenceUtils;
    public String mCurr_Frag_Name;
    public Intent mShoppingBroadcastIntent;
    public Intent mWeatherBroadcastIntent;
    public Intent mMapBroadcastIntent;
    public StandByFragment standByFragment;
    public WeatherFragment weatherFragment;
    public CalendarFragment calendarFragment;
    public MusicListFragment musicFragment;
    public ShoppingFragment shoppingFragment;
    public MapFragment mapFragment;
    public SettingFragment settingFragment;
    //天气参数
    public int mWeather_voice_flag;
    public String mWeather_voice_city;
    public String mWeather_voice_time;
    public String mWeather_func_flag;
    public Intent mMusicBroadcastIntent;
    public ReturnVoice mWeather_return_voice;
    //地图参数
    public int mMap_voice_flag;
    public String mMap_voice_name;
    public String mMap_voice_address;
    public String mMap_voice_fromAddress;
    public String mMap_voice_toAddress;
    public String mMap_voice_pathWay;
    public ResultCallback mMap_result_callback;
    public String cityName;

    //music handler
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GlobalUtils.Music.STOP_MUSIC_FLAG:
                    Log.i("ryan", "handleMessage: ");
                    stopMusicPlay();
                    break;
                default:break;
            }
        }
    };

    //voice start Activity
    public Intent mIntent;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (BaseActivity.MyOnTouchListener listener : onTouchListeners) {
            if (listener != null) {
                listener.onTouch(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void registerMyOnTouchListener(BaseActivity.MyOnTouchListener myOnTouchListener) {
        onTouchListeners.add(myOnTouchListener);
    }

    public void unregisterMyOnTouchListener(BaseActivity.MyOnTouchListener myOnTouchListener) {
        onTouchListeners.remove(myOnTouchListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Window window = getWindow();
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);
        initPermission();
        //FrgamentManager
        mFragmentManager = getSupportFragmentManager();
        //voice start activity
        mIntent = new Intent(this, MainActivity.class);
        mControllerintent = new Intent(this, ControllerService.class);
        myServiceConnection = new BaseActivity.MyServiceConnection();
        //实例化SharePreferencesUtls
        mSharePrefrenceUtils = new SharePrefrenceUtils(this);
        mShoppingBroadcastIntent = new Intent(GlobalUtils.Shopping.SHOPPING_BROADCAST_ACTION);
//        handler.postDelayed(runnable,1000 * 60 * 10);
//        handler.postDelayed(runnable,1000  * 10);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //当前Activity是否是SwipeActivity
        isActivityTop = ActivityStatusUtils.isTopActivity(this);
        isFragmentTop = ActivityStatusUtils.isTopFragment(this,mFragmentManager);
//        Log.i("ryan", "onResume: baseActivity:isTop = "+isActivityTop+",isFragment = "+isFragmentTop.getClass().getSimpleName());
//        if (handler!=null) {
//            handler.removeCallbacks(runnable);
////            handler.postDelayed(runnable,1000 * 60 * 10);
//            handler.postDelayed(runnable,1000  * 10);
//        }
    }

    // 6.0以上权限获取
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,

                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(20);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*isActivityTop = false;
        if (isFragmentTop != null) {
            isFragmentTop = null;
        }
        ActivityStatusUtils.onDestroy();*/
    }

    public void initStandBy(){
        Log.i(TAG, "initStandBy: ");
        if (!checkFragment(GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME)){
            actionBar_title = "";
            if (standByFragment == null){
                standByFragment = new StandByFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager,standByFragment, R.id.contentFrame);
        }
    }

    public void initSetting() {
        if (!checkFragment(GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.setting_title);
            if (settingFragment == null) {
                settingFragment = new SettingFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, settingFragment, R.id.contentFrame);
        }
    }

    public void initMap() {
        if (!checkFragment(GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME)) {
            Log.d("进来init()了","123456749");
            if (mMap_voice_flag == GlobalUtils.Map.MAP_VOICE_FLAG) {
                if (mapFragment == null) {
                    Log.d("进来语音传值了","12346");
                    mapFragment = MapFragment.newInstance(mMap_voice_name,mMap_voice_address,mMap_voice_fromAddress,mMap_voice_toAddress,mMap_voice_pathWay);
                }
            } else {
                if (mapFragment== null) {
                    mapFragment = new MapFragment();
                }
            }
            actionBar_title = mResources.getString(R.string.map_title);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, mapFragment, R.id.contentFrame);
        }else{
            Log.d("表示当前是","map");
        }
    }

    public void initShopping(String web_url) {
        if (!checkFragment(GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.shopping_title);
            if (!(web_url.equals("")) && !TextUtils.isEmpty(web_url)) {
                if (shoppingFragment == null) {
                    shoppingFragment = ShoppingFragment.newInstance(web_url);
                }
                mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME);
                ActivityUtils.replaceFragmentInActivity(mFragmentManager, shoppingFragment, R.id.contentFrame);
            }
        }
    }

    public void initMusic() {
        if (!checkFragment(GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME)) {
            Log.i("ryan", "initMusic: base:init");
            actionBar_title = mResources.getString(R.string.music_title);
            Log.i(TAG, "initMusic: music_name = "+music_name);
            if (music_name != null) {
                if (musicFragment == null) {
                    musicFragment = MusicListFragment.newInstance(music_name);
                }
            }else {
                if (musicFragment == null) {
                    musicFragment = new MusicListFragment();
                }
            }
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, musicFragment, R.id.contentFrame);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME);
        }
    }

    public void initCalendar() {
        if (!checkFragment(GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME)) {
            actionBar_title = mResources.getString(R.string.calendar_title);
            if (calendarFragment == null) {
                calendarFragment = new CalendarFragment();
            }
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, calendarFragment, R.id.contentFrame);
        }
    }

    public void initWeather() {
        if (!checkFragment(GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME)) {
            if (mWeather_voice_flag == GlobalUtils.Weather.WEATHER_VOICE_FLAG) {
                if (weatherFragment == null) {
                    weatherFragment = WeatherFragment.newInstance(mWeather_voice_city, mWeather_voice_time, mWeather_func_flag, mWeather_voice_flag);
                }
            } else {
                if (weatherFragment == null) {
                    weatherFragment = new WeatherFragment();
                }
            }
            actionBar_title = mResources.getString(R.string.weather_title);
            mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME);
            ActivityUtils.replaceFragmentInActivity(mFragmentManager, weatherFragment, R.id.contentFrame);
        }
    }

    //判断当前哪个fragment
    public boolean checkFragment(String frag_name) {
        mCurr_Frag_Name = mSharePrefrenceUtils.getCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID);
        Log.i("ryan", "checkFragment: frag_name_curr = " + mCurr_Frag_Name + ",frag_name = " + frag_name);
        switch (frag_name) {
            case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            stopMusicPlay();
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:
                            stopMusicPlay();
                            break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            stopMusicPlay();
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:
                            stopMusicPlay();
                            break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                           stopMusicPlay();
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:
                            stopMusicPlay();
                            break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            stopMusicPlay();
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:
                            stopMusicPlay();
                            break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)) {return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            stopMusicPlay();
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                            standByFragment = null;break;
                        default:
                            stopMusicPlay();
                            break;
                    }
                }
                break;
            case GlobalUtils.WhichFragment.STANDBY_FRAGMENT_NAME:
                if (mCurr_Frag_Name.equals(frag_name)){return true;}
                else {
                    switch (mCurr_Frag_Name) {
                        case GlobalUtils.WhichFragment.WEATHER_FRAGMENT_NAME:
                            weatherFragment = null;break;
                        case GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_NAME:
                            calendarFragment = null;break;
                        case GlobalUtils.WhichFragment.MUSIC_FRAGMENT_NAME:
                            musicFragment = null;break;
                        case GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_NAME:
                            shoppingFragment = null;break;
                        case GlobalUtils.WhichFragment.MAP_FRAGMENT_NAME:
                            mapFragment = null;break;
                        case GlobalUtils.WhichFragment.SETTING_FRAGMENT_NAME:
                            settingFragment = null;break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    public void stopMusicPlay(){
        Log.i("ryan", "stopMusicPlay: ");
        if (mControllerBinder != null){
            Log.i("ryan", "stopMusicPlay: not null");
            MusicPlay musicPlay = mControllerBinder.getControlService().musicPlay;
            if (musicPlay.isPlaying()){
                musicPlay.stop();
            }
        }
//            Log.i("ryan", "checkFragment: ispalying = "+musicFragment.musicService.musicPlay.isPlaying());
/*            if (musicFragment.musicService.musicPlay.isPlaying()){
                musicFragment.pause();
            }*/
    }

    public class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: 主acitivy服务绑定");
            mControllerBinder = (ControllerService.MyBinder) iBinder;
            //停止music 播放，切换fragment时
            mHandler.sendEmptyMessage(GlobalUtils.Music.STOP_MUSIC_FLAG);

            //调城市
            mControllerBinder.getControlService().getCityName(new ReturnCityName() {
                @Override
                public void getCityName(String city) {
                     cityName = city;
                     returnCityName.getCityName(cityName);
                     Log.d("Base里拿到的值：",cityName);
                }
            });

            //shopping语音处理
            mControllerBinder.onReturnWeburl(new ShoppingCallBack() {
                @Override
                public void onShoppingCallback(String web_url) {
                    revokeMainShoppingVoice(web_url);
                }
            });

            //日历语音处理
            mControllerBinder.setCalendarControllerListener(new CalendarCallBack() {
                @Override
                public void onCalendarCallBack() {
                    revokeMainCalendarVoice();
                }
            });

            //音乐语音处理
            mControllerBinder.onGetMusicName(new MusicCallBack() {
                @Override
                public void onMusicCallBack(String music_name) {
                    revokeMainMusicVoice(music_name);
                }
            });
            //天气语音处理
            mControllerBinder.setWeatherControllerListener(new WeatherCallback() {
                @Override
                public void onWeatherCallback(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
                    mWeather_return_voice = returnVoice;
                    revokeMainWeatherVoice(cityName, time, returnVoice, func_flag, flag);
                }
            });

            //天气ReturnVoice注册
            if (mWeather_voice_flag == GlobalUtils.Weather.WEATHER_VOICE_FLAG){
                Log.i(TAG, "onServiceConnected: 天气ReturnVoice注册");
                mWeather_return_voice = mControllerBinder.getControlService().getReturnVoice();
                returnVoiceCallback();
            }

            //地图语音处理
            mControllerBinder.setMapControllerListener(new MapCallBack() {
                @Override
                public void onMapCallBack(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback result) {
                    revokeMainMapVoice(name,address,fromAddress,toAddress,pathWay,result);
                }
            });

            //地图ResultCallBack注册
            if (mMap_voice_flag == GlobalUtils.Map.MAP_VOICE_FLAG){
                Log.i(TAG, "onServiceConnected: 地图ResultCallBack注册");
                mMap_result_callback = mControllerBinder.getControlService().getResultCallBack();
                returnMapVoicecallBack();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mControllerBinder != null) {
                mControllerBinder = null;
            }
        }
    }

    //购物模块语音处理
    private void revokeMainShoppingVoice(String web_url) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("ShoppingFragment")) {
                    sendShoppingBroadcast(web_url);
                } else {
                    relaceShoppingFragment(web_url);
                }
            }
        } else {
            Log.i(TAG, "revokeMainShoppingVoice: not top");
            startShopping(web_url);
        }
    }

    //日历模块语音处理
    private void revokeMainCalendarVoice() {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("CalendarFragment")) {
                    Log.i("ryan", "openSpecifyWebsites: 当前Fragment是CalendarFragment");
                } else {
                    replaceCalendarFragment();
                }
            }
        } else {
            Log.i("ryan", "revokeMainCalendarVoice: not top");
            startCalendar();
        }
    }

    //天气模块语音处理
    private void revokeMainWeatherVoice(String cityName, String time, final ReturnVoice returnVoice, String func_flag, int flag) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("WeatherFragment")) {
                    sendWeatherBroadcast(cityName, time, returnVoice, func_flag);
                } else {
                    replaceWeatherBroadcast(cityName, time, returnVoice, func_flag, flag);
                }
            }
        } else {
            Log.i("ryan", "revokeMainWeatherVoice: not top");
            startWeather(cityName, time, returnVoice, func_flag, flag);
        }
    }

    //音乐模块语音处理
    private void revokeMainMusicVoice(String music_name) {
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("MusicFragment")) {
                    sendMusicBroadcast(music_name);
                } else {
                    replaceMusicFragment(music_name);
                }
            }
        } else {
            Log.i(TAG, "revokeMainMusicVoice: not top");
            startMusic(music_name);
        }
    }

    //地图模块处理
    private void revokeMainMapVoice(String name,String address,String fromAddress,String toAddress,String pathWay,ResultCallback resultCallback) {
        Log.d("isActivityTop:",""+isActivityTop);
        if (isActivityTop) {
            if (isFragmentTop != null) {
                if (isFragmentTop.getClass().getSimpleName().equals("MapFragment")) {
                    sendMapBroadcast(name, address, fromAddress, toAddress, pathWay, resultCallback);
                } else {
                    if(name==null){
                        Log.d(TAG, "revokeMainMapVoice: "+"   name的值是空的");
                    }
                    replaceMapFragment(name, address, fromAddress, toAddress, pathWay, resultCallback);
                }
            }
        } else {
            Log.i(TAG, "revokeMainMapVoice: not top");
            startMap(name, address, fromAddress, toAddress, pathWay, resultCallback);
        }
    }

    //天气语音接口回调
    private void returnVoiceCallback() {
        if (weatherFragment != null) {
            weatherFragment.setReturnAnswerCallback(new ReturnAnswerCallback() {
                @Override
                public void onReturnAnswer(String voiceAnswer) {
                    Log.i(TAG, "onReturnAnswer: returnAnswer");
                    if (mWeather_return_voice != null) {
                        Log.i(TAG, "onReturnAnswer: voiceAnswer");
                        mWeather_return_voice.onReturnVoice(voiceAnswer);
                        mWeather_voice_flag = -1;
                    }
                }
            });
        }
    }

    //地图语音接口回调
    private void returnMapVoicecallBack() {
        if(mapFragment != null){
            mapFragment.setMapReturnAnswerCallback(new ReturnMapAnswerCallBack() {
                @Override
                public void onReturnAnswer(String mapAnswer) {
                    Log.d("地图语音答复回调","return Answer");
                    if (mMap_result_callback !=null){
                        Log.d("看到说明没办法了","146");
                        mMap_result_callback.onResult(mapAnswer);
                        mMap_voice_flag = -1;
                    }
                }
            });
        }
    }

    private void relaceShoppingFragment(String web_url) {
        Log.i(TAG, "openSpecifyWebsites: 当前Fragment不是ShoppingFragment");
        initShopping(web_url);
        mActionBar.setTitle(actionBar_title);
    }

    private void sendShoppingBroadcast(String web_url) {
        Log.i(TAG, "openSpecifyWebsites: 当前Fragment是ShoppingFragment");
        mShoppingBroadcastIntent.putExtra("shoppings", web_url);
        sendBroadcast(mShoppingBroadcastIntent);
    }

    private void startShopping(String web_url){
        Log.i(TAG, "revokeMainShoppingVoice: 当前Activity不是SwipeActivity");
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
        mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.SHOPPING_FRAGMENT_INTENT_ID);
        mIntent.putExtra("weburl", web_url);
        startActivity(mIntent);
    }

    private void replaceCalendarFragment() {
        Log.i("ryan", "openSpecifyWebsites: 当前Fragment不是CalendarFragment");
        initCalendar();
        mActionBar.setTitle(actionBar_title);
    }

    private void startCalendar(){
        Log.i("ryan", "revokeMainCalendarVoice: 当前Activity不是SwipeActivity");
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
        mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.CALENDAR_FRAGMENT_INTENT_ID);
        startActivity(mIntent);
    }

    private void replaceWeatherBroadcast(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag) {
        Log.i("ryan", "revokeSwipeWeatherVoice: 当前Fragment不是WeatherFragment");
        mWeather_return_voice = returnVoice;
        mWeather_voice_city = cityName;
        mWeather_voice_time = time;
        mWeather_voice_flag = flag;
        mWeather_func_flag = func_flag;
        initWeather();
        mActionBar.setTitle(actionBar_title);
        returnVoiceCallback();
    }

    private void sendWeatherBroadcast(String cityName, String time, ReturnVoice returnVoice, String func_flag) {
        Log.i("ryan", "revokeSwipeWeatherVoice: 当前Fragment是WeatherFragment");
        mWeather_return_voice = returnVoice;
        returnVoiceCallback();
        mWeatherBroadcastIntent = new Intent(GlobalUtils.Weather.WEATHER_BROADCAST_ACTION);
        mWeatherBroadcastIntent.putExtra("cityname", cityName);
        mWeatherBroadcastIntent.putExtra("time", time);
        mWeatherBroadcastIntent.putExtra("flag", func_flag);
        sendBroadcast(mWeatherBroadcastIntent);
    }

    private void startWeather(String cityName, String time, ReturnVoice returnVoice, String func_flag, int flag){
        Log.i("ryan", "revokeMainWeatherVoice: 当前Activity不是SwipeActivity");
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
        mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.WEATHER_FRAGMENT_INTENT_ID);
        Bundle args = new Bundle();
        args.putString("cityname", cityName);
        args.putString("time", time);
        args.putString("fun_flag", func_flag);
        args.putInt("voice_flag", flag);
        mIntent.putExtra("weather", args);
        startActivity(mIntent);
    }

    private void replaceMusicFragment(String music_name) {
        BaseActivity.this.music_name = music_name;
        Log.i(TAG, "revokeSwipeMusicVoice: 当前Fragment不是MusicFragment");
        Log.d(TAG, "revokeSwipeMusicVoice music_name = "+BaseActivity.this.music_name);
        initMusic();
        mActionBar.setTitle(actionBar_title);
    }

    private void sendMusicBroadcast(String music_name) {
        Log.i(TAG, "revokeSwipeMusicVoice: 当前Fragment是MusicFragment");
        mMusicBroadcastIntent.putExtra("music", music_name);
        sendBroadcast(mMusicBroadcastIntent);
    }

    private void startMusic(String music_name){
        Log.d(TAG, "music222: " + music_name);
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
        mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MUSIC_FRAGMENT_INTENT_ID);
        mIntent.putExtra("music_name",music_name);
        startActivity(mIntent);
    }

    private void replaceMapFragment(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback resultCallback) {
        Log.i(TAG, "revokeSwipeMapVoice: 当前Fragment不是MapFragment");
        mMap_result_callback = resultCallback;
        mMap_voice_name = name;
        mMap_voice_address = address;
        mMap_voice_fromAddress = fromAddress;
        mMap_voice_toAddress = toAddress;
        mMap_voice_pathWay = pathWay;
        mMap_voice_flag = 6;
        initMap();
        mActionBar.setTitle(actionBar_title);
        returnMapVoicecallBack();
    }

    private void sendMapBroadcast(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback resultCallback) {
        Log.i(TAG, "revokeSwipeMapVoice: 当前Fragment是MapFragment");
        mMap_result_callback = resultCallback;
        returnMapVoicecallBack();
        mMapBroadcastIntent = new Intent(GlobalUtils.Map.MAP_BROADCAST_ACTION);
        mMapBroadcastIntent.putExtra("name", name);
        mMapBroadcastIntent.putExtra("address", address);
        mMapBroadcastIntent.putExtra("fromAddress", fromAddress);
        mMapBroadcastIntent.putExtra("toAddress", toAddress);
        mMapBroadcastIntent.putExtra("pathWay", pathWay);
        Log.d("广播发出去的出行方式:", pathWay);
        sendBroadcast(mMapBroadcastIntent);
    }

    private void startMap(String name, String address, String fromAddress, String toAddress, String pathWay, ResultCallback result){
        Log.i(TAG, "revokeMainMapVoice: 当前Activity不是SwipeActivity");
        mSharePrefrenceUtils.saveCurrentFragment(GlobalUtils.WhichFragment.CURRENT_FRAGMENT_ID, "");
        mSharePrefrenceUtils.saveChangeFragment(GlobalUtils.WhichFragment.FIRST_CHANGE_FRAGMENT, true);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mIntent.putExtra(GlobalUtils.WhichFragment.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WhichFragment.MAP_FRAGMENT_INTENT_ID);
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("address", address);
        args.putString("fromAddress", fromAddress);
        args.putString("toAddress", toAddress);
        args.putString("pathWay",pathWay);
        mIntent.putExtra("map", args);
        startActivity(mIntent);
    }

    private ReturnCityName returnCityName;

    public void setReturnCityName(ReturnCityName returnCity){
        returnCityName = returnCity;
    }
}
