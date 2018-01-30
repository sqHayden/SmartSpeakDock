package com.idx.smartspeakdock.map;

/**
 * Created by derik on 18-1-2.
 */

public enum PathWay {
    WALK("步行"),
    RIDE("骑车"),
    DRIVE("驾车"),
    DODRIVE("开车"),
    TRANSIT("公交"),
    DOTRANSIT("坐公交");
    private String desc;
    private PathWay(String desc){
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
