package com.idx.smartspeakdock.music.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.idx.smartspeakdock.music.entity.Music;
import com.idx.smartspeakdock.music.util.AppCache;
import com.idx.smartspeakdock.music.util.MusicUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by sunny on 18-1-23.
 */

public class MusicPlay {

    private static final String TAG = MusicPlay.class.getName();
    //通知绑定者music的状态
    public static final String  ACTION_MEDIA_PLAY= "com.idx.ACTION_MEDIA_PLAY";
    public static final String ACTION_MEDIA_PAUSE="com.idx.ACTION_MEDIA_PAUSE";
    public static final String ACTION_MEDIA_NEXT = "com.idx.ACTION_MEDIA_NEXT";
    public static  final String ACTION_MEDIA_COMPLETE="com.idx.ACTION_MEDIA_COMPLETE";
    public static final String ACTION_MEDIA_PREVIOUS = "com.idx.music.ACTION_MEDIA_PREVIOUS";
    public static  final String ACTION_MEDIA_ERROR="com.idx.music.ACTION_ERROR" ;

    // 正在播放的音乐的序号
    private int mPlayingPosition = -1;
    //媒体播放器
    private MediaPlayer mediaPlayer;
    //正在播放的音乐
    private Music mPlayingMusic;

    private boolean isPlaying = false;
    private Music music;
    private Context mContext;
    public MusicPlay(Context context){
        this.mContext=context;
        mediaPlayer=getMediaPlayer(context);
    }

    // 获取正在播放的音乐的序号
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    // 获取正在播放的音乐
    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    //获取正在播放音乐的长度
    public long getCurrentPosition1() {
        if (getPlayingMusic()!=null) {
            return mediaPlayer.getCurrentPosition();
        }else {
            return 0;
        }
    }

    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            pause();
            notifyMusicState(ACTION_MEDIA_PAUSE,false);
        }
        else if (getCurrentPosition1()>0){
            continuePlay();
            notifyMusicState(ACTION_MEDIA_PLAY,true);
        }
        else {
            play(getPlayingPosition());
            notifyMusicState(ACTION_MEDIA_PLAY,true);
        }
    }

    //暂停后继续播放
    public void continuePlay() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            notifyMusicState(ACTION_MEDIA_PLAY, true);
        }
    }

    //停止播放
    public void stop() {
        pause();
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
    //下一首
    public void next() {
        Log.d(TAG, "next: ");
        play(mPlayingPosition + 1);
        notifyMusicState(ACTION_MEDIA_NEXT,true);
    }
    //上一首
    public void pre(){
        play(mPlayingPosition - 1);
        notifyMusicState(ACTION_MEDIA_PREVIOUS,true);
    }
    //暂停播放
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyMusicState(ACTION_MEDIA_PAUSE, false);
        }
    }

    // 指定播放的位置
    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    //音乐是否播放的标识
    public boolean isPlaying() {
        return isPlaying;
    }

    //按音乐名称播放
    public void play(String name) {
        if (MusicUtil.getMusic().get(name)!=null) {
            music = MusicUtil.getMusic().get(name);
            mPlayingMusic = music;
            play(music);
            notifyMusicState(ACTION_MEDIA_PLAY,true);
        }else {
            Log.d("ccc", "play: ");
            notifyMusicState(ACTION_MEDIA_ERROR,false);
        }
    }

    public void play(int position) {
        //音乐列表初始化
        AppCache.get().initData();
        if (AppCache.get().getMusicList().size() == 0) {
            return;
        }
        else if (position < 0) {
            position = AppCache.get().getMusicList().size() - 1;
        } else if (position >= AppCache.get().getMusicList().size()) {
            position = 0;
        }
        mPlayingPosition = position;
        music = AppCache.get().getMusicList().get(mPlayingPosition);
        mPlayingMusic = music;
        play(music);
    }

    //播放音乐
    public void play(Music music) {
        mPlayingMusic = music;
        try {
//            Log.d(TAG, "play: 进入musicService，获取当前音乐:"+music.getUrl());
            mediaPlayer.reset();
            mediaPlayer.setOnPreparedListener(mPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnErrorListener(mErrorListener);
            mediaPlayer.setDataSource(music.getUrl());
            mediaPlayer.prepareAsync();
            notifyMusicState(ACTION_MEDIA_PLAY,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //通知绑定者音乐状态
    public void notifyMusicState(String action, boolean isPlaying){
        Intent intent = new Intent();
        intent.setAction(action);
        mContext.sendBroadcast(intent);
        this.isPlaying = isPlaying;
    }

    // 播放完成
    public MediaPlayer.OnCompletionListener onCompletionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
                    next();
                    notifyMusicState(ACTION_MEDIA_COMPLETE, true);
        }
    };

    //播放准备
    public MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            notifyMusicState(ACTION_MEDIA_PLAY, true);
        }
    };

    public MediaPlayer.OnErrorListener mErrorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            notifyMusicState(ACTION_MEDIA_ERROR,false);
            return false;
        }
    };
    //对MediaPlayer进行实例化
    public MediaPlayer getMediaPlayer(Context context) {
        MediaPlayer mediaplayer = new MediaPlayer();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return mediaplayer;
        }
        try {
            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
            Constructor constructor = cSubtitleController.getConstructor(
                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
            Object subtitleInstance = constructor.newInstance(context, null, null);
            Field f = cSubtitleController.getDeclaredField("mHandler");
            f.setAccessible(true);
            try {
                f.set(subtitleInstance, new Handler());
            } catch (IllegalAccessException e) {
                return mediaplayer;
            } finally {
                f.setAccessible(false);
            }
            Method setsubtitleanchor = mediaplayer.getClass().getMethod("setSubtitleAnchor",
                    cSubtitleController, iSubtitleControllerAnchor);
            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null);
        } catch (Exception e) {
            Log.d(TAG, "getMediaPlayer crash ,exception = " + e);
        }
        return mediaplayer;
    }
}
