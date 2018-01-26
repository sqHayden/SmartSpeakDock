package com.idx.smartspeakdock.map.Bean;

import com.idx.smartspeakdock.baidu.unit.listener.ResultCallback;
import com.idx.smartspeakdock.map.PathWay;

/**
 * Created by hayden on 18-1-24.
 */

public interface MapCallBack {
    void onMapCallBack(String name, String address, String fromAddress, String toAddress, PathWay pathWay, ResultCallback result);
}
