package com.idx.smartspeakdock.music.util;

import android.content.Context;


import com.idx.smartspeakdock.music.entity.LocalMusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/*
* 本地音乐列表*/

public class LocalMusicList {
    private static Object mLock = new Object();
    private static List<LocalMusic> mLocalMusicList = null;
    private LocalMusicList() {}

    public static List<LocalMusic> getAudioList(Context context) {
        if (mLocalMusicList == null) {
            synchronized (mLock) {
                if (mLocalMusicList == null) {
                    mLocalMusicList = new ArrayList<>();
                    for (LocalMusic  localMusic : MediaUtils.getAudioList(context)) {
                        /*if (!localMusic.isMusic()) {
                            Log.d("musiclist", "title: " + localMusic.getTitle() + ", isMusic: " + localMusic.isMusic()
                                    + ", isAlarm: " + localMusic.isAlarm() + ", isNotification: " + localMusic.isNotification() + ", isRingtone: " + localMusic.isRingtone());
                        }*/
                        if (localMusic.isMusic() && localMusic.getDuration() > 40 * 1000) {
                            mLocalMusicList.add(localMusic);
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(mLocalMusicList);
    }

    public static List<LocalMusic> getAudioList(Context context, Comparator<? super LocalMusic> cmp) {
        List<LocalMusic> newList = new ArrayList<>(getAudioList(context));
        Collections.sort(newList, cmp);
        return newList;
    }
}
