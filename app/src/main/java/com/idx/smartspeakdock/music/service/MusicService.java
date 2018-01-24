package com.idx.smartspeakdock.music.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.music.activity.MusicListFragment;
import com.idx.smartspeakdock.music.entity.Music;
import com.idx.smartspeakdock.music.util.AppCache;
import com.idx.smartspeakdock.music.util.MusicUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener{
    private static final String TAG = MusicService.class.getName();

    public MusicPlay musicPlay;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        musicPlay=new MusicPlay(getApplicationContext());

        UnitManager.getInstance(getBaseContext()).setMusicVoiceListener(new IMusicVoiceListener() {
            @Override
            public void onPlay(int index) {

            }

            @Override
            public void onPlay(String name) {

                    musicPlay.play(name);
            }

            @Override
            public void onPause() {
                    musicPlay.pause();

            }

            @Override
            public void onContinue() {

                    musicPlay.continuePlay();
            }

            @Override
            public void onNext() {

                    musicPlay.next();
            }

            @Override
            public void onPrevious() {

                    musicPlay.pre();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    // 播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {
          musicPlay.next();
    }

    // 获取正在播放的音乐的序号
    public int getPlayingPosition() {
        return musicPlay.getPlayingPosition();
    }

    // 获取正在播放的音乐
    public Music getPlayingMusic() {
        return musicPlay.getPlayingMusic();
    }

    //获取正在播放音乐的长度
    public long getCurrentPosition() {

            return musicPlay.getCurrentPosition();

    }

    public void playPause() {
        musicPlay.playPause();
    }

    //暂停后继续播放
    public void continuePlay() {
        musicPlay.continuePlay();
    }


// 停止播放
    public void stop() {
       musicPlay.stop();
    }

    //下一首
    public void next() {
        Log.d(TAG, "next: ");
        musicPlay.next();
    }

    //上一首
    public void pre(){
        Log.d(TAG, "pre: ");
        musicPlay.pre();
    }

   //暂停播放
    public void pause() {
        musicPlay.pause();
   }
    // 指定播放的位置
    public void seekTo(int position) {
         Log.d(TAG, "seekTo: ");
        musicPlay.seekTo(position);
    }

    //音乐是否播放的标识
    public boolean isPlaying() {
        return musicPlay.isPlaying();
    }

    //按音乐名称播放
    public void play(String name) {
        musicPlay.play(name);
    }

    //按音乐位置播放
    public void play(int position) {
          musicPlay.play(position);
    }
    //播放音乐
    public void play(Music music) {
        musicPlay.play(music);
    }

    //
    public class PlayBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    //fragment与service通信,返回PlayBinder实例
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return new PlayBinder();
    }

//    //通知绑定者音乐状态
//    private void notifyMusicState(String action, boolean isPlaying){
//        Intent intent = new Intent();
//        intent.setAction(action);
//        sendBroadcast(intent);
//        this.isPlaying = isPlaying;
//    }
//
//    //对MediaPlayer进行实例化
//    private MediaPlayer getMediaPlayer(Context context) {
//        MediaPlayer mediaplayer = new MediaPlayer();
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
//             return mediaplayer;
//        }
//        try {
//            Class<?> cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider");
//            Class<?> cSubtitleController = Class.forName("android.media.SubtitleController");
//            Class<?> iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController$Anchor");
//            Class<?> iSubtitleControllerListener = Class.forName("android.media.SubtitleController$Listener");
//            Constructor constructor = cSubtitleController.getConstructor(
//                    new Class[]{Context.class, cMediaTimeProvider, iSubtitleControllerListener});
//            Object subtitleInstance = constructor.newInstance(context, null, null);
//            Field f = cSubtitleController.getDeclaredField("mHandler");
//            f.setAccessible(true);
//            try {
//                f.set(subtitleInstance, new Handler());
//            } catch (IllegalAccessException e) {
//                return mediaplayer;
//            } finally {
//                f.setAccessible(false);
//            }
//            Method setsubtitleanchor = mediaplayer.getClass().getMethod("setSubtitleAnchor",
//                    cSubtitleController, iSubtitleControllerAnchor);
//            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null);
//        } catch (Exception e) {
//            Log.d(TAG, "getMediaPlayer crash ,exception = " + e);
//        }
//        return mediaplayer;
//    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

