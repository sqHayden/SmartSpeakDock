package com.idx.smartspeakdock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.idx.smartspeakdock.utils.NetStatusUtils;

/**
 * Created by ryan on 17-12-26.
 * Email: Ryan_chan01212@yeah.net
 */

public class BaseFragment extends Fragment {

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
}
