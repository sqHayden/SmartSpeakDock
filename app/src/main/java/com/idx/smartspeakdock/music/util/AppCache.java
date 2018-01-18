package com.idx.smartspeakdock.music.util;

import com.idx.smartspeakdock.music.entity.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunny on 18-1-10.
 */

//初始化音乐数据
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

    //得到音乐列表的大小
    public List<Music> getMusicList() {

        initData();
        return mMusicList;
    }
    //初始化音乐列表
    public void initData(){

        for (String key : MusicUtil.getMusic().keySet()) {
            mMusicList.add(MusicUtil.getMusic().get(key));
        }
    }
}
