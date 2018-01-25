package com.idx.smartspeakdock.music.activity;



import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.adapter.MusicAdapter;
import com.idx.smartspeakdock.music.service.MusicPlay;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.utils.GlobalUtils;


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
    //播放音乐出错
    private static final int CONSTAN_MUSIC_ERROR=6;
    private MusicAdapter musicAdapter;
    private ListView listView;
    public  ControllerService musicService;
    private PlayServiceConnection conn;
    private MusicBroadcastReceiver musicBroadcastReceiver;
    private ImageView bar_start;
    private ImageView bar_next;
    private View viewBar;
    private TextView bar_title;
    private View view;
    public boolean isPlaying =false;
    public ProgressDialog mProgressDialog;
    public String music_name;

    public MusicListFragment(){}

    public static MusicListFragment newInstance(String music_name){

        MusicListFragment musicListFragment=new MusicListFragment();
        Bundle args = new Bundle();
        args.putString(GlobalUtils.MUSIC_NAME_ID,music_name);
        musicListFragment.setArguments(args);
        Log.d(TAG, "newInstance: "+musicListFragment);
        return musicListFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        if(getArguments() != null){
            music_name = getArguments().getString(GlobalUtils.MUSIC_NAME_ID);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        if (savedInstanceState != null){
           music_name = savedInstanceState.getString("music_name");
           bar_title.setText(music_name);
        }


        //调用 bindService 保持与 Service 的通信
        try {
            if (conn==null) {
                Intent intent = new Intent(getActivity(), ControllerService.class);
                conn = new PlayServiceConnection();
                getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
            }
            handler.post(runnable);
        }catch (Exception e){
            e.printStackTrace();
        }

        //注册广播
        musicBroadcastReceiver=new MusicBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_NEXT);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_PLAY);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_PAUSE);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_PREVIOUS);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_COMPLETE);
        intentFilter.addAction(MusicPlay.ACTION_MEDIA_ERROR);
        intentFilter.addAction(GlobalUtils.MUSIC_BROADCAST_ACTION);
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

        //音乐未加载完毕
        mProgressDialog=new ProgressDialog(getActivity());
        mProgressDialog.setTitle("请稍等");
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(true);

    }

    //ListView点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //调用 bindService 保持与 Service 的通信
        Intent intent = new Intent(getActivity(), ControllerService.class);
        conn=new PlayServiceConnection();
        getActivity().bindService(intent,conn,Context.BIND_AUTO_CREATE);

        musicService.musicPlay.play(position);
        bar_title.setText(musicService.musicPlay.getPlayingMusic().getTitle());
        bar_start.setImageResource(R.mipmap.music_pause);
        handler.post(runnable);

    }

    //音乐暂停
    public void  pause(){
        musicService.musicPlay.pause();
    }

    //音乐播放
    public void play() {
        Log.d(TAG, "play: 进入play ");

        musicService.musicPlay.playPause();

    }

    //下一首
    public void next() {
        Log.d(TAG, "next111: ");
        musicService.musicPlay.next();

    }


    //音乐播放状态，界面图标显示
    public void playState(){
        Log.d(TAG, "playState111: ");
        isPlaying=true;
        bar_start.setImageResource(R.mipmap.music_pause);
        handler.post(runnable);
    }

    //音乐暂停状态，界面图标显示
    public void pauseState(){
        Log.d(TAG, "pauseState111: ");
        isPlaying=false;
        bar_start.setImageResource(R.mipmap.music_play);
        handler.post(runnable);

    }
    public void errorState(){
        Log.d(TAG, "errorState: ");
        isPlaying=false;
        bar_start.setImageResource(R.mipmap.music_play);
        Toast.makeText(getActivity(),"找不到该音乐",Toast.LENGTH_LONG).show();

    }
    //接受广播发送的消息，响应音乐在不同播放状态下ui的变化
    public Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CONSTANT_MUSIC_PLAY:
                    playState();
                    if (musicService.musicPlay.getPlayingMusic()!=null){
                        bar_title.setText(musicService.musicPlay.getPlayingMusic().getTitle());
                    }
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
                case CONSTAN_MUSIC_ERROR:
                   errorState();
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
                musicService=((ControllerService.MyBinder)iBinder).getControlService();
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
                Log.d(TAG, "run: "+musicService.musicPlay.getCurrentPosition());
                if (musicService.musicPlay.getCurrentPosition()==0 &&
                        musicService.musicPlay.getPlayingMusic()!=null){
                    mProgressDialog.show();
                }else {
                    mProgressDialog.dismiss();
                }
                    bar_title.setText(musicService.musicPlay.getPlayingMusic().getTitle());
                    if (musicService.musicPlay.isPlaying()) {
                       bar_start.setImageResource(R.mipmap.music_pause);
                    } else{
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
            switch (action){
                case MusicPlay.ACTION_MEDIA_PLAY:
                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PLAY);
                    break;
                case MusicPlay.ACTION_MEDIA_PAUSE:

                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PAUSE);
                    break;
                case MusicPlay.ACTION_MEDIA_NEXT:

                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_NEXT);
                    break;
                case MusicPlay.ACTION_MEDIA_COMPLETE:

                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_COMPLETE);
                    break;
                case MusicPlay.ACTION_MEDIA_ERROR:
                    mHandler.sendEmptyMessage(CONSTAN_MUSIC_ERROR);
                    break;
                case GlobalUtils.MUSIC_BROADCAST_ACTION:
                    music_name=intent.getStringExtra("music_name");
                    Log.d(TAG, "onReceive: "+music_name);
                    musicService.musicPlay.play(music_name);
                    break;
                default:
                    break;
            }
        }
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
                musicService.musicPlay.stop();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
