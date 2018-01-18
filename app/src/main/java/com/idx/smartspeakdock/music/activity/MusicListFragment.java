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
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.MainActivity;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.music.adapter.MusicAdapter;
import com.idx.smartspeakdock.music.service.MusicService;
import com.idx.smartspeakdock.music.util.AppCache;
import com.idx.smartspeakdock.utils.ActivityUtils;


public class MusicListFragment extends BaseFragment implements AdapterView.OnItemClickListener
        ,View.OnClickListener {
    private static final String TAG = MusicListFragment.class.getName();

    //音乐播放状态
    private static final int CONSTANT_MUSIC_PLAY = 1;
    //音乐暂停状态
    private static final int CONSTANT_MUSIC_PAUSE=2;
    //音乐下一首
    private static final int CONSTANT_MUSIC_NEXT=3;
    //上一首
    private static  final int CONSTANT_MUSIC_PRE=4;
    //完成状态
    private static final int CONSTANT_MUSIC_COMPLETE =5;
    private MusicAdapter musicAdapter;
    private ListView listView;
    private MusicService musicService;
    private PlayServiceConnection conn;
    private MusicBroadcastReceiver musicBroadcastReceiver=new MusicBroadcastReceiver();
    private ImageView bar_start;
    private ImageView bar_next;
    private View viewBar;
    private TextView bar_title;
    private View view;
    public boolean isPlaying =false;
    private ProgressBar progressBar;

    public MusicListFragment(){

    }

    public static MusicListFragment newInstance(){
        return new MusicListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        //调用 bindService 保持与 Service 的通信
        if (conn==null) {
            Intent intent = new Intent(getActivity(), MusicService.class);
            conn = new PlayServiceConnection();
            getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_MEDIA_NEXT);
        intentFilter.addAction(MusicService.ACTION_MEDIA_PLAY);
        intentFilter.addAction(MusicService.ACTION_MEDIA_PAUSE);
        intentFilter.addAction(MusicService.ACTION_MEDIA_PREVIOUS);
        intentFilter.addAction(MusicService.ACTION_MEDIA_COMPLETE);
        getActivity().registerReceiver(musicBroadcastReceiver,intentFilter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.music_fragment_list, container, false);
        return view;
    }

    //对组件设置监听
    private void setListener(){

        bar_start = (ImageView)view. findViewById(R.id.bar_play);
        bar_next = (ImageView) view.findViewById(R.id.bar_next);

        bar_title = (TextView) view.findViewById(R.id.bar_title);
        viewBar = (View) view.findViewById(R.id.bar);
        progressBar=(ProgressBar)view.findViewById(R.id.play_bar);
        bar_start.setOnClickListener(this);
        bar_next.setOnClickListener(this);

        bar_title.setOnClickListener(this);
        viewBar.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bar:
                Intent intent=new Intent(getActivity(),MusicPlayActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.bar_play:
                play();
                break;
            case R.id.bar_next:
                next();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");

        listView = (ListView) view.findViewById(R.id.music_list);
        musicAdapter = new MusicAdapter();
        listView.setAdapter(musicAdapter);
        listView.setOnItemClickListener(this);
        setListener();

        try {
            handler.post(runnable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //ListView点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //调用 bindService 保持与 Service 的通信
        Intent intent = new Intent(getActivity(), MusicService.class);
        conn=new PlayServiceConnection();
        getActivity().bindService(intent,conn,Context.BIND_AUTO_CREATE);

        musicService.play(position);
        bar_title.setText(musicService.getPlayingMusic().getTitle());
        bar_start.setImageResource(R.mipmap.music_pause);
        handler.post(runnable);
    }

    //音乐播放
    private void play() {
        Log.d(TAG, "play: 进入play ");

        musicService.playPause();

    }

    //下一首
    private void next() {
        Log.d(TAG, "next111: ");
        musicService.next();

    }
    //音乐播放状态，界面图标显示
    private void playState(){
        Log.d(TAG, "playState111: ");
        isPlaying=true;
        bar_start.setImageResource(R.mipmap.music_pause);

        
    }

    //音乐暂停状态，界面图标显示
    private void pauseState(){
        Log.d(TAG, "pauseState111: ");
        isPlaying=false;
        bar_start.setImageResource(R.mipmap.music_play);

    }

    //接受广播发送的消息，响应音乐在不同播放状态下ui的变化
    public Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CONSTANT_MUSIC_PLAY:
                    playState();
                    break;
                case CONSTANT_MUSIC_PAUSE:
                    pauseState();
                    break;
                case CONSTANT_MUSIC_NEXT:
                    playState();
                    break;
                case CONSTANT_MUSIC_COMPLETE:
                    pauseState();
                    break;
                default:
                    break;
            }
        }
    };

    //  回调PlayServiceConnection 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    public class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                musicService=((MusicService.PlayBinder)iBinder) .getService();
            }catch (ClassCastException e){
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }

    // 通过 Handler 更新 UI 上的组件状态
    public Handler handler = new Handler();

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                progressBar.setProgress((int)musicService.getCurrentPosition());
                progressBar.setMax((int)musicService.getPlayingMusic().getDuration());
                bar_title.setText(musicService.getPlayingMusic().getTitle());
                if (musicService.isPlaying()) {
                    bar_start.setImageResource(R.mipmap.music_pause);
                    }
                    else{
                        bar_start.setImageResource(R.mipmap.music_play);
                    }

                    handler.postDelayed(runnable, 1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //广播，发送消息
    public class MusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: listFragment"+action);
            switch (action){
                case MusicService.ACTION_MEDIA_PLAY:
                    Log.d(TAG, "onReceive: PLAY111 ");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PLAY);
                    break;
                case MusicService.ACTION_MEDIA_PAUSE:
                    Log.d(TAG, "onReceive: PAUSE111");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PAUSE);
                    break;
                case MusicService.ACTION_MEDIA_NEXT:
                    Log.d(TAG, "onReceive:  NEXT111");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_NEXT);
                    break;
                case MusicService.ACTION_MEDIA_COMPLETE:
                    Log.d(TAG, "onReceive: COMPLETE111");
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_COMPLETE);
                    break;
                default:
                    break;
            }
        }
    }
        @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("", "onAttach: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (musicBroadcastReceiver!=null) {
                getActivity().unregisterReceiver(musicBroadcastReceiver);
            }
            if (conn!=null){
                getActivity().unbindService(conn);
            }
            if (musicService!=null){
                musicService.stop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
