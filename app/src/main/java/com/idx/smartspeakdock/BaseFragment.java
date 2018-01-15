package com.idx.smartspeakdock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.idx.smartspeakdock.calendar.CalendarFragment;
import com.idx.smartspeakdock.map.MapFragment;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.shopping.ShoppingFragment;
import com.idx.smartspeakdock.utils.NetStatusUtils;
import com.idx.smartspeakdock.weather.ui.WeatherFragment;

/**
 * Created by ryan on 17-12-26.
 * Email: Ryan_chan01212@yeah.net
 */

public class BaseFragment extends Fragment {
    private final String TAG = "BaseFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean checkNetworkStatus(){
        if(NetStatusUtils.isMobileConnected(getActivity()) || NetStatusUtils.isWifiConnected(getActivity())) {
            return true;
        } else {
            return false;
        }
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
