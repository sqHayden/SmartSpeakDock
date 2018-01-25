package com.idx.smartspeakdock.music.activity;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.idx.smartspeakdock.R;

import com.idx.smartspeakdock.music.service.MusicPlay;

import com.idx.smartspeakdock.service.ControllerService;

import com.idx.smartspeakdock.utils.GlobalUtils;

import java.util.Locale;

import static com.idx.smartspeakdock.music.entity.Music.formatTime;


public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener{

    //音乐播放状态
    private static final int CONSTANT_MUSIC_PLAY = 1;
    //音乐暂停状态
    private static final int CONSTANT_MUSIC_PAUSE = 2;
    //下一首
    private static final int CONSTANT_MUSIC_NEXT=3;
    //上一首
    private static  final int CONSTANT_MUSIC_PRE=4;
    //完成状态
    private static final int CONSTANT_MUSIC_COMPLETE =5;
    //播放音乐出错
    private static final int CONSTAN_MUSIC_ERROR=6;

    private static final String TAG = MusicPlayActivity.class.getName();
    private PlayServiceConnection conn;
    private MusicBroadcastReceiver musicBroadcastReceiver = new MusicBroadcastReceiver();
    private ImageView iv_back;
    private TextView title;
    private TextView artist;
    private FrameLayout frameLayout;
    private TextView current;
    private TextView draution;
    private SeekBar seekBar;
    private ImageButton ib_pre;
    private ImageButton ib_next;
    private ImageButton ib_start;
    private ControllerService musicService;
    private boolean isPlay=false;
    public ProgressDialog mProgressDialog;

    //对组件设置监听
    private void setListener() {
        iv_back = (ImageView)findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        artist = (TextView)findViewById(R.id.artist);
        frameLayout = (FrameLayout)findViewById(R.id.album);
        current = (TextView)findViewById(R.id.current);
        draution = (TextView) findViewById(R.id.duration);
        seekBar = (SeekBar) findViewById(R.id.seek);
        ib_pre = (ImageButton) findViewById(R.id.iv_pre);
        ib_start = (ImageButton) findViewById(R.id.iv_start);
        ib_next = (ImageButton) findViewById(R.id.iv_next);
        iv_back.setOnClickListener(this);
        title.setOnClickListener(this);
        artist.setOnClickListener(this);
        frameLayout.setOnClickListener(this);
        current.setOnClickListener(this);
        draution.setOnClickListener(this);
        ib_pre.setOnClickListener(this);
        ib_start.setOnClickListener(this);
        ib_next.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_fragment_play);
        Log.d(TAG, "onCreate: ");
        //调用 bindService 保持与 Service 的通信
        Intent intent = new Intent(MusicPlayActivity.this, ControllerService.class);
        conn = new PlayServiceConnection();
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

         //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_NEXT);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_PLAY);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_PAUSE);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_PREVIOUS);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_COMPLETE);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_ERROR);
        intentFilter.addAction(GlobalUtils.MUSIC_BROADCAST_ACTION);
        registerReceiver(musicBroadcastReceiver, intentFilter);

        setListener();
        handler.post(runnable);
        //音乐未加载完毕
        mProgressDialog=new ProgressDialog(MusicPlayActivity.this);
        mProgressDialog.setTitle("请稍等");
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(true);

    }


    //接受广播发送的消息，响应音乐在不同播放状态下ui的变化
    private Handler mHandler=new Handler()   {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONSTANT_MUSIC_PLAY:
                    playState();
                    if (musicService.musicPlay.getPlayingMusic() != null) {
                        title.setText(musicService.musicPlay.getPlayingMusic().getTitle());
                        current.setText(formatTime("mm:ss",musicService.musicPlay.getCurrentPosition()));
                        if (musicService.musicPlay.getPlayingMusic().getArtist()!=null) {
                            artist.setText(musicService.musicPlay.getPlayingMusic().getArtist());
                        }else {
                            artist.setText("未知");
                        }
                    }
                    playState();
                    break;
                case CONSTANT_MUSIC_PAUSE:
                    pauseState();
                    break;
                case CONSTANT_MUSIC_NEXT:
                    playState();
                    break;
                case CONSTANT_MUSIC_PRE:
                    playState();
                    break;
                case CONSTANT_MUSIC_COMPLETE:
                    pauseState();
                case CONSTAN_MUSIC_ERROR:
                    pauseState();
                    Toast.makeText(MusicPlayActivity.this,"找不到该音乐",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.iv_pre:
                prev();
                break;
            case R.id.iv_next:
                next();
                break;
            case R.id.iv_start:
                play();
                break;
            default:
                break;
        }
    }

    //进度条进度改变
    @Override
    public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser) {

        Log.d(TAG, "onProgressChanged: "+progress);
        current.setText(formatTime("mm:ss",progress));

    }

    //进度条开始拖动
    @Override
    public void onStartTrackingTouch(SeekBar seekBar1) {

    }

    //进度条停止拖动
    @Override
    public void onStopTrackingTouch(SeekBar seekBar1) {
        if (musicService.musicPlay.getPlayingMusic()!=null ) {
            Log.d(TAG, "onStopTrackingTouch: ");
            int progress = seekBar1.getProgress();
            musicService.musicPlay.seekTo(progress);
        }
    }

    //播放
    private void play() {

        musicService.musicPlay.playPause();
    }

    //下一首
    private void next() {

        musicService.musicPlay.next();

    }

    //上一首
    private void prev() {

        musicService.musicPlay.pre();
    }

    //音乐播放状态，界面图标显示
    private void playState(){
            isPlay=true;
            ib_start.setImageResource(R.mipmap.music_pause);
            handler.post(runnable);
    }

    //音乐暂停状态，界面图标显示
    private void pauseState(){
            isPlay=false;
            ib_start.setImageResource(R.mipmap.music_play);
            mHandler.post(runnable);
    }

    // 通过 Handler 更新 UI 上的组件状态
    public Handler handler = new Handler();

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (musicService.musicPlay.getCurrentPosition() == 0 &&
                        musicService.musicPlay.getPlayingMusic() != null) {
                    mProgressDialog.show();
                   }else {
                    mProgressDialog.dismiss();
                   }
                    title.setText(musicService.musicPlay.getPlayingMusic().getTitle());
                    Log.d(TAG, "run: " + musicService.musicPlay.getCurrentPosition());
                    current.setText("00:00");
                    current.setText(formatTime("mm:ss", musicService.musicPlay.getCurrentPosition()));
                    seekBar.setProgress((int) musicService.musicPlay.getCurrentPosition());
                    seekBar.setMax((int) musicService.musicPlay.getPlayingMusic().getDuration());
                    draution.setText(formatTime("mm:ss", musicService.musicPlay.getPlayingMusic().getDuration()));
                    if (musicService.musicPlay.isPlaying()) {
                        ib_start.setImageResource(R.mipmap.music_pause);
                    } else {
                        ib_start.setImageResource(R.mipmap.music_play);
                    }
                    handler.postDelayed(runnable, 1000);

                }catch(Exception e){
                    e.printStackTrace();
                }

        }
    };

    //  回调PlayServiceConnection 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    public class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                musicService=((ControllerService.MyBinder)iBinder).getControlService();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    //广播，发送消息
    public class MusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case MusicPlay.ACTION_MEDIA_PLAY:
                    Log.d(TAG, "onReceive: playfragment play2222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PLAY);
                    break;
                case MusicPlay.ACTION_MEDIA_PAUSE:
                    Log.d(TAG, "onReceive: playfragment pause2222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PAUSE);
                    break;
                case MusicPlay.ACTION_MEDIA_NEXT:
                    Log.d(TAG, "onReceive: playfragment next222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_NEXT);
                    break;
                case MusicPlay.ACTION_MEDIA_PREVIOUS:
                    Log.d(TAG, "onReceive: playfragment pre222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PRE);
                    break;
                case MusicPlay.ACTION_MEDIA_COMPLETE:
                    Log.d(TAG, "onReceive: playfragment complete222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_COMPLETE);
                    break;
                case MusicPlay.ACTION_MEDIA_ERROR:
                    mHandler.sendEmptyMessage(CONSTAN_MUSIC_ERROR);
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (musicBroadcastReceiver!=null){
                unregisterReceiver(musicBroadcastReceiver);
            }
            if (conn!=null){
                unbindService(conn);
            }
            if (musicService!=null){
                musicService.musicPlay.stop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}



