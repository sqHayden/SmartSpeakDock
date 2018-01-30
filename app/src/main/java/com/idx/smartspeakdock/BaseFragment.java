package com.idx.smartspeakdock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.MotionEvent;

import com.idx.smartspeakdock.swipe.MainActivity;
import com.idx.smartspeakdock.swipe.SwipeActivity;
import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.standby.StandByActivity;
import com.idx.smartspeakdock.utils.ActivityStatusUtils;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

/**
 * Created by ryan on 17-12-26.
 * Email: Ryan_chan01212@yeah.net
 */

public class BaseFragment extends Fragment {
    private final String TAG = "BaseFragment";
    private SwipeActivity.MyOnTouchListener onTouchListener;

    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setClass(SpeakerApplication.getContext(),StandByActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SpeakerApplication.getContext().startActivity(intent);
            handler.removeCallbacks(this);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean checkNetworkStatus() {
        if (NetStatusUtils.isMobileConnected(getActivity()) || NetStatusUtils.isWifiConnected(getActivity())) {
            return true;
        } else {
            return false;
        }
    }

    
    @Override
    public void onResume() {
        super.onResume();
       BaseActivity.isFragmentTop = ActivityStatusUtils.isTopFragment(getActivity(),BaseActivity.mFragmentManager);

        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,60*5*1000);
//        handler.postDelayed(runnable,10*1000);
        onTouchListener = new BaseActivity.MyOnTouchListener() {
            @Override
            public boolean onTouch(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (getActivity() instanceof MainActivity){
                            Log.i(TAG, "onTouch: MainActivity");
                            Fragment fragment = ActivityStatusUtils.isTopFragment(getActivity(),BaseActivity.mFragmentManager);
                            if (fragment.getClass().getSimpleName().equals("StandByFragment")){
                                Log.i(TAG, "onTouch: StandByFragment");
                                if (!(((MainActivity) getActivity()).mDrawerLayout.isDrawerVisible(GravityCompat.START))){
                                    Log.i(TAG, "onTouch: drawer inVisible");
                                    ((MainActivity) getActivity()).mDrawerLayout.openDrawer(GravityCompat.START);
                                }
                            }
                        }
                        if (handler != null) {
                            Log.d(TAG, "onTouch: 你开启了倒计时");
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, 1000 * 5 * 60);
//                            handler.postDelayed(runnable, 1000 * 10 );
                        }
                        Log.d(TAG, "onTouch: ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: ACTION_MOVE");

                        break;
                    case MotionEvent.ACTION_UP:
//                        if (handler != handler) {
//                            //        handler.postDelayed(runnable,1000 * 60 * 10);
////                            handler.postDelayed(runnable, 1000 * 10);
//                        }
                        Log.d(TAG, "onTouch: ACTION_UP");

                        break;
                }

                return false;
            }
        };
        ((BaseActivity) getActivity()).registerMyOnTouchListener(onTouchListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        ((BaseActivity) getActivity()).unregisterMyOnTouchListener(onTouchListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ActivityStatusUtils.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    public String judgeCurrentFragment(){
        if (this instanceof WeatherFragment){
            Log.i(TAG, "judgeCurrentFragment: weather");
            return "weather";
        }
        if (this instanceof CalendarFragment){
            Log.i(TAG, "judgeCurrentFragment: calendar");
            return "calendar";
        }
        if (this instanceof MusicListFragment){
            Log.i(TAG, "judgeCurrentFragment: music");
            return "music";
        }
        if (this instanceof ShoppingFragment){
            Log.i(TAG, "judgeCurrentFragment: shopping");
            return "shopping";
        }
        if (this instanceof MapFragment){
            Log.i(TAG, "judgeCurrentFragment: map");
            return "map";
        }
        if (this instanceof MapFragment){
            Log.i(TAG, "judgeCurrentFragment: setting");
            return "setting";
        }
        return "";
    }
}
