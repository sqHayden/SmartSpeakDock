package com.idx.smartspeakdock.standby;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.Logger;

/**
 * Created by Franck on 1/4/18.
 */

public class StandByActivity extends BaseActivity {
    private StandByFragment standByFragment;
    private static final String TAG = "StandByActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Logger.setEnable(true);
        hideBottomUIMenu();
        setContentView(R.layout.content_main);
//        bindService(mControllerintent, myServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(mControllerintent, myServiceConnection, Context.BIND_AUTO_CREATE);
        standByFragment =
                (StandByFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (standByFragment == null) {
            standByFragment = new StandByFragment();
            ActivityUtils.replaceFragmentInActivity(
                    getSupportFragmentManager(), standByFragment, R.id.contentFrame);
        }
    }

    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: ");
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: standby");
//        if (handler != null){
//            Log.d(TAG, "onResume: handler");
//            handler.removeCallbacks(runnable);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(standByFragment != null){
            standByFragment = null;
        }
//        if (mControllerBinder != null){
//            unbindService(myServiceConnection);
//        }
    }
}
