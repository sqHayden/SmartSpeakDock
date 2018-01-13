package com.idx.smartspeakdock.music.util;

import com.idx.smartspeakdock.music.entity.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunny on 18-1-10.
 */

public class AppCache {

    //歌曲列表
    private final List<Music> mMusicList = new ArrayList<>();


    private AppCache() {
    }

    private static class SingletonHolder {
        private static AppCache instance = new AppCache();
    }

    public static AppCache get() {
        return SingletonHolder.instance;
    }

    public List<Music> getMusicList() {

        for (String key : MusicUtil.getMusic().keySet()) {
            mMusicList.add(MusicUtil.getMusic().get(key));
        }
        return mMusicList;
    }

    public void initData(){

        for (String key : MusicUtil.getMusic().keySet()) {
            mMusicList.add(MusicUtil.getMusic().get(key));
        }
    }
}
