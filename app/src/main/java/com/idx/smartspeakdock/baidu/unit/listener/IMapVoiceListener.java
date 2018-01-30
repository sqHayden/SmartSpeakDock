package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.map.SearchArea;

/**
 * Created by derik on 17-12-25.
 */

public interface IMapVoiceListener {
    void onLocationInfo(ResultCallback result);

    void onSearchInfo(String name, ResultCallback result);

    void onSearchAddress(String address, ResultCallback result);

    /**
     *
     * @param fromAddress replace it with the local address when it is null
     * @param toAddress
     * @param pathWay
     * @return
     */
    void onPathInfo(String fromAddress, String toAddress, String pathWay, ResultCallback result);

}
