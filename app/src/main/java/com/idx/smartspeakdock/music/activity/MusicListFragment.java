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
import android.widget.TextView;

import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.adapter.MusicAdapter;
import com.idx.smartspeakdock.music.service.MusicService;


public class MusicListFragment extends BaseFragment implements AdapterView.OnItemClickListener
        ,View.OnClickListener {
    private static final String TAG = MusicListFragment.class.getName();

    private static final int CONSTANT_MUSIC_PLAY = 1;
    private static final int CONSTANT_MUSIC_PAUSE = 2;
    private static final int CONSTANT_MUSIC_NEXT=3;
    private static  final int CONSTANT_MUSIC_PRE=4;
    private static final int CONSTANT_MUSIC_COMPLETE =5;
    private MusicAdapter musicAdapter;
    private ListView listView;
    private MusicService musicService;
    private MusicPlayFragment musicPlayFragment;
    private PlayServiceConnection conn;
    private MusicBroadcastReceiver musicBroadcastReceiver=new MusicBroadcastReceiver();
    private ImageView bar_start;
    private ImageView bar_next;
    private View viewBar;
    private TextView bar_title;
    private View view;
    public boolean isPlaying =true;

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

    private void setListener(){

        bar_start = (ImageView)view. findViewById(R.id.bar_play);
        bar_next = (ImageView) view.findViewById(R.id.bar_next);

        bar_title = (TextView) view.findViewById(R.id.bar_title);
        viewBar = (View) view.findViewById(R.id.bar);

        bar_start.setOnClickListener(this);
        bar_next.setOnClickListener(this);

        bar_title.setOnClickListener(this);
        viewBar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bar:
                showPlayFragment();
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

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//        t1=AppCache.get().getMusicList().get(position).getTitle();
//        Log.d(TAG, "onItemClick: "+t1);

        //调用 bindService 保持与 Service 的通信
        Intent intent = new Intent(getActivity(), MusicService.class);
        conn=new PlayServiceConnection();
        getActivity().bindService(intent,conn,Context.BIND_AUTO_CREATE);

         musicService.play(position);

         handler.post(runnable);
    }

    private void showPlayFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (musicPlayFragment == null) {
            musicPlayFragment = new MusicPlayFragment();
            ft.replace(android.R.id.content, musicPlayFragment);
        } else {
            ft.show(musicPlayFragment);
        }
        ft.commit();
    }


    private void play() {
        Log.d(TAG, "play: 进入play ");
        if (isPlaying) {
            musicService.playPause();
        }

    }

    private void next() {
        Log.d(TAG, "next111: ");
        musicService.next();

    }

    private void playState(){
        Log.d(TAG, "playState111: ");
        isPlaying=true;
        
    }

    private void pauseState(){
        Log.d(TAG, "pauseState111: ");
        isPlaying=false;
    }

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
                    playState();
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
                Log.d(TAG, "onServiceConnected service: "+musicService);
                Log.d(TAG, "onServiceConnected,getPlayingMusic "+musicService.getPlayingMusic());
//                Log.d(TAG, "onServiceConnected: "+musicService.getPlayingMusic().getTitle());
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

            if (musicService.getPlayingMusic().getTitle()!=null){
                Log.d(TAG, "run: "+musicService.getCurrentPosition());
            bar_title.setText(musicService.getPlayingMusic().getTitle());
            handler.postDelayed(runnable, 200);
            }
        }
    };

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
        if (musicBroadcastReceiver!=null) {
            getActivity().unregisterReceiver(musicBroadcastReceiver);
        }
        if (conn!=null){
            getActivity().unbindService(conn);
        }
        try {
            if (musicService!=null){
                musicService.stop();
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
}
