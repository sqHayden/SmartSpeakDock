package com.idx.smartspeakdock.music.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.music.entity.Music;
import com.idx.smartspeakdock.music.util.AppCache;
import com.idx.smartspeakdock.music.util.MusicUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener{
    private static final String TAG = MusicService.class.getName();
    //通知绑定着music的状态
    public static final String  ACTION_MEDIA_PLAY= "com.idx.ACTION_MEDIA_PLAY";
    public static final String ACTION_MEDIA_PAUSE="com.idx.ACTION_MEDIA_PAUSE";
    public static final String ACTION_MEDIA_NEXT = "com.idx.ACTION_MEDIA_NEXT";
    public static  final String ACTION_MEDIA_COMPLETE="com.idx.ACTION_MEDIA_COMPLETE";
    public static final String ACTION_MEDIA_PREVIOUS = "com.idx.music.ACTION_MEDIA_PREVIOUS";

    // 正在播放的歌曲的序号
    private int mPlayingPosition = -1;
    private MediaPlayer mediaPlayer ;
    private Music mPlayingMusic;

    private boolean isPlaying = false;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mediaPlayer=getMediaPlayer(getApplicationContext());
        mediaPlayer.setOnCompletionListener(this);

        UnitManager.getInstance(getBaseContext()).setMusicVoiceListener(new IMusicVoiceListener() {
            @Override
            public void onPlay(int index) {

            }

            @Override
            public void onPlay(String name) {
                Log.d(TAG, "onPlay: name, " + name);
                play(name);
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onContinue() {

            }

            @Override
            public void onNext() {
                next();
            }

            @Override
            public void onPrevious() {
                pre();

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onPrepared: MusicService");
            mp.start();
            notifyMusicState(ACTION_MEDIA_PLAY, true);
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: MusicService");
          next();
          notifyMusicState(ACTION_MEDIA_COMPLETE, true);
    }

    // 获取正在播放的歌曲的序号
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    // 获取正在播放的歌曲
    public Music getPlayingMusic() {
        return mPlayingMusic;
    }


    public long getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void playPause() {
        if (mediaPlayer.isPlaying()) {
            pause();
            notifyMusicState(ACTION_MEDIA_PAUSE,false);
        }
         else {
            play(getPlayingPosition());
            notifyMusicState(ACTION_MEDIA_PLAY,true);
        }
    }

    public void stop() {
        pause();
        mediaPlayer.reset();
    }
    public void next() {
        Log.d(TAG, "next: ");
        play(mPlayingPosition + 1);
        notifyMusicState(ACTION_MEDIA_NEXT,true);
    }

    public void pre(){
        Log.d(TAG, "pre: ");
        play(mPlayingPosition - 1);
        notifyMusicState(ACTION_MEDIA_PREVIOUS,true);
    }

    void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyMusicState(ACTION_MEDIA_PAUSE, false);
        }
    }

    //跳转到指定的时间位置
    public void seekTo(int position) {
        Log.d(TAG, "seekTo: ");
         mediaPlayer.seekTo(position);
    }



    public void play(String name) {
        Music music = MusicUtil.getMusic().get(name);
        mPlayingMusic=music;

        Log.d(TAG, "play: 进入musicService,当前音乐位置："+mPlayingPosition);
        play(music);
    }

    public void play(int position) {
        AppCache.get().initData();
        if (AppCache.get().getMusicList().size()==0) {
            return;
        }
        if (position<0){
            position = AppCache.get().getMusicList().size()- 1;
        }else if (position>= AppCache.get().getMusicList().size()){
            position=0;
        }
        mPlayingPosition = position;
        Music music= AppCache.get().getMusicList().get(mPlayingPosition);
        mPlayingMusic=music;
        Log.d(TAG, "play: 进入musicService,当前音乐位置："+mPlayingPosition);
        play(music);
    }

    public void play(Music music) {
        mPlayingMusic = music;
        try {
            Log.d(TAG, "play: 进入musicService，获取当前音乐:"+music.getUrl());
            mediaPlayer=getMediaPlayer(getApplicationContext());
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(music.getUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mPreparedListener);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class PlayBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return new PlayBinder();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void notifyMusicState(String action, boolean isPlaying){
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
        this.isPlaying = isPlaying;
    }

    private MediaPlayer getMediaPlayer(Context context) {
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
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

