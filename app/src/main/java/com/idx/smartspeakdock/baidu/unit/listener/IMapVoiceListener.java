package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.map.SearchArea;

/**
 * Created by derik on 17-12-25.
 */

public interface IMapVoiceListener {
    String onLocationInfo();

    String onSearchInfo(String name, SearchArea searchArea);

    String onSearchAddress(String address);

    /**
     *
     * @param fromAddress replace it with the local address when it is null
     * @param toAddress
     * @param pathWay
     * @return
     */
    String onPathInfo(String fromAddress, String toAddress, PathWay pathWay);

}
