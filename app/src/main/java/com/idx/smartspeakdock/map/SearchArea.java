package com.idx.smartspeakdock.map;

/**
 * Created by derik on 18-1-2.
 */

public enum SearchArea {
    AREA_NEARBY("附近"),
    AREA_CITY("全市");
    private String desc;
    private SearchArea(String desc){
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
