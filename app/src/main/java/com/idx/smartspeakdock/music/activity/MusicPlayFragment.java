package com.idx.smartspeakdock.music.activity;


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

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.service.MusicService;

import java.util.Locale;


public class MusicPlayFragment extends Fragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener{

    private static final int CONSTANT_MUSIC_PLAY = 1;
    private static final int CONSTANT_MUSIC_PAUSE = 2;
    private static final int CONSTANT_MUSIC_NEXT=3;
    private static  final int CONSTANT_MUSIC_PRE=4;
    private static final int CONSTANT_MUSIC_COMPLETE =5;



    private static final String TAG = MusicPlayFragment.class.getName();
    private PlayServiceConnection conn;
    private MusicBroadcastReceiver musicBroadcastReceiver = new MusicBroadcastReceiver();
    private MusicListFragment musicListFragment;

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
    private View playorPauseBackground;
    private  View view;

    private MusicService musicService;
    private boolean isPlaying = true;

    private void setListener() {
        iv_back = (ImageView) view.findViewById(R.id.back);
        title = (TextView) view.findViewById(R.id.title);
        artist = (TextView) view.findViewById(R.id.artist);
        frameLayout = (FrameLayout) view.findViewById(R.id.album);
        current = (TextView) view.findViewById(R.id.current);
        draution = (TextView)view. findViewById(R.id.duration);
        seekBar = (SeekBar)view. findViewById(R.id.seek);
        ib_pre = (ImageButton) view.findViewById(R.id.iv_pre);
        ib_start = (ImageButton) view.findViewById(R.id.iv_start);
        ib_next = (ImageButton) view.findViewById(R.id.iv_next);
        playorPauseBackground = (View)view. findViewById(R.id.playPauseButtonBackground);
        iv_back.setOnClickListener(this);
        title.setOnClickListener(this);
        artist.setOnClickListener(this);
        frameLayout.setOnClickListener(this);
        current.setOnClickListener(this);
        draution.setOnClickListener(this);
        seekBar.setOnClickListener(this);
        ib_pre.setOnClickListener(this);
        ib_start.setOnClickListener(this);
        ib_next.setOnClickListener(this);
        playorPauseBackground.setOnClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        //调用 bindService 保持与 Service 的通信
        Intent intent = new Intent(getActivity(), MusicService.class);
        conn = new PlayServiceConnection();
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);

         //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_MEDIA_NEXT);
        intentFilter.addAction(MusicService.ACTION_MEDIA_PLAY);
        intentFilter.addAction(MusicService.ACTION_MEDIA_PAUSE);
        intentFilter.addAction(MusicService.ACTION_MEDIA_PREVIOUS);
        intentFilter.addAction(MusicService.ACTION_MEDIA_COMPLETE);
        getActivity().registerReceiver(musicBroadcastReceiver, intentFilter);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.music_fragment_play,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

         setListener();
         handler.post(runnable);

    }

    private Handler mHandler=new Handler()   {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONSTANT_MUSIC_PLAY:
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
                    completeState();
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
                showListFragment();
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

    @Override
    public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged: ");
        if (seekBar1 == seekBar) {
            current.setText(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar1) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar1) {
            if (musicService.getPlayingMusic()!=null ) {
                Log.d(TAG, "onStopTrackingTouch: ");
                int progress = seekBar1.getProgress();
                musicService.seekTo(progress);
        }
    }

    private void showListFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (musicListFragment == null) {
            musicListFragment = new MusicListFragment();
            ft.replace(android.R.id.content, musicListFragment);
        } else {
            ft.show(musicListFragment);
        }
        ft.commit();
    }

    private void play() {
        Log.d(TAG, "播放页面 play:");
        if (isPlaying) {
            musicService.playPause();
        }
    }

    private void next() {
        Log.d(TAG, "播放页面 next: ");
        musicService.next();

    }

    private void prev() {
        Log.d(TAG, "播放页面 prev: ");
        musicService.pre();
    }
    private void playState(){
        Log.d(TAG, "播放页面playState222: ");
        isPlaying=true;
    }
    private void pauseState(){
        Log.d(TAG, "播放页面pauseState222: ");
        isPlaying=false;
    }
    private void completeState(){
        Log.d(TAG, "播放页面completeState222: ");
        musicService.next();
        isPlaying=true;
    }

    // 通过 Handler 更新 UI 上的组件状态
    public Handler handler = new Handler();

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "run: ");
            title.setText(musicService.getPlayingMusic().getTitle());
            Log.d(TAG, "run: " + musicService.getCurrentPosition());
            current.setText(formatTime("mm:ss",musicService.getCurrentPosition()));
            seekBar.setProgress((int) musicService.getCurrentPosition());
            seekBar.setMax((int) musicService.getPlayingMusic().getDuration());
            draution.setText(formatTime("mm:ss", musicService.getPlayingMusic().getDuration()));
            handler.postDelayed(runnable, 1000);
        }
    };

    //  回调PlayServiceConnection 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    public class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                musicService = ((MusicService.PlayBinder) iBinder).getService();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
                musicService = null;
        }
    }

    public class MusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: playfragment"+action);
            switch (action){
                case MusicService.ACTION_MEDIA_PLAY:
                    Log.d(TAG, "onReceive: playfragment play2222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PLAY);
                    break;
                case MusicService.ACTION_MEDIA_PAUSE:
                    Log.d(TAG, "onReceive: playfragment pause2222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PAUSE);
                case MusicService.ACTION_MEDIA_NEXT:
                    Log.d(TAG, "onReceive: playfragment next222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_NEXT);
                    break;
                case MusicService.ACTION_MEDIA_PREVIOUS:
                    Log.d(TAG, "onReceive: playfragment pre222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PRE);
                    break;
                case MusicService.ACTION_MEDIA_COMPLETE:
                    Log.d(TAG, "onReceive: playfragment compelete222");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_COMPLETE);
                    break;
                default:
                    break;
            }
        }
    }

    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicBroadcastReceiver!=null){
            getActivity().unregisterReceiver(musicBroadcastReceiver);
        }
        if (conn!=null){
            getActivity().unbindService(conn);
        }if (musicService!=null){
            musicService.stop();
        }
    }
}



