package com.idx.smartspeakdock;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.Logger;

import java.util.ArrayList;
import java.util.List;

// 只用于继承
public abstract class BaseActivity extends AppCompatActivity {
    private final String TAG = "BaseActivity";
    public  boolean isActivityTop;
    public Fragment isFragmentTop;
    public FragmentManager mFragmentManager;
    public String fragment_show_activity = "SwipeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.drawer_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Window window = getWindow();
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);
        initPermission();
        //FrgamentManager
        mFragmentManager = getSupportFragmentManager();
        //当前Activity是否是SwipeActivity
        isTopActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //当前正在显示的Fragment
        isTopFragment();
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

    public void isTopActivity(){
        isActivityTop = false;
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.i(TAG, "isTopActivity = " + cn.getClassName());
        if (cn.getClassName().contains(fragment_show_activity))
        {
            isActivityTop = true;
        }
        Log.i(TAG, "isTop = " + isActivityTop);
    }

    public void isTopFragment(){
        List<Fragment> fragments = mFragmentManager.getFragments();
        Log.i(TAG, "isTopFragment: size = "+fragments.size());
        for(Fragment fragment : fragments){
//            if(fragment != null && fragment.isVisible()){
                isFragmentTop = fragment;
//            }
            Log.i(TAG, "isTopFragment: "+isFragmentTop.getClass().getSimpleName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityTop = false;
        if (isFragmentTop != null) {
            isFragmentTop = null;
        }
    }
}
