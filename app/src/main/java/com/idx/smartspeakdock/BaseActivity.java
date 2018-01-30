package com.idx.smartspeakdock;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.idx.smartspeakdock.swipe.SwipeActivity;
import com.idx.smartspeakdock.utils.ActivityStatusUtils;

import java.util.ArrayList;
import java.util.List;

// 只用于继承
public  abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public static Fragment isFragmentTop;
    public boolean isActivityTop;
    public static FragmentManager mFragmentManager;
    private ArrayList<SwipeActivity.MyOnTouchListener> onTouchListeners = new ArrayList<SwipeActivity.MyOnTouchListener>(10);
//    public String fragment_show_activity = "SwipeActivity";


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
//        handler.postDelayed(runnable,1000 * 60 * 10);
//        handler.postDelayed(runnable,1000  * 10);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //当前Activity是否是SwipeActivity
        isActivityTop = ActivityStatusUtils.isTopActivity(this);
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
        isActivityTop = false;
        if (isFragmentTop != null) {
            isFragmentTop = null;
        }
        ActivityStatusUtils.onDestroy();
    }
}
