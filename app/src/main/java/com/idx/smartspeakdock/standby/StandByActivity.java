package com.idx.smartspeakdock.standby;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.utils.ActivityUtils;
import com.idx.smartspeakdock.utils.Logger;

/**
 * Created by peter on 1/4/18.
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
        setContentView(R.layout.content_main);
        standByFragment =
                (StandByFragment) mFragmentManager.findFragmentById(R.id.contentFrame);
        if (standByFragment == null) {
            standByFragment = new StandByFragment();
            ActivityUtils.replaceFragmentInActivity(
                    mFragmentManager, standByFragment, R.id.contentFrame);
        }
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: ");
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(standByFragment != null){
            standByFragment = null;
        }
    }
}
