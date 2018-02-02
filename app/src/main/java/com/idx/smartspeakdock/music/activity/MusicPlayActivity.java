package com.idx.smartspeakdock.music.activity;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.idx.smartspeakdock.BaseActivity;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.service.MusicPlay;
import com.idx.smartspeakdock.music.util.AppCache;
import com.idx.smartspeakdock.service.ControllerService;
import com.idx.smartspeakdock.utils.BitmapUtils;
import com.idx.smartspeakdock.utils.GlobalUtils;
import com.idx.smartspeakdock.utils.ToastUtils;

import static com.idx.smartspeakdock.music.entity.Music.formatTime;


public class MusicPlayActivity extends BaseActivity implements View.OnClickListener,
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
    private MusicBroadcastReceiver musicBroadcastReceiver = new MusicBroadcastReceiver();
    private PlayServiceConnection conn;
    private TextView title;
    private TextView artist;
    private ImageView imageView;
    private TextView current;
    private TextView draution;
    private SeekBar seekBar;
    private ImageButton ib_pre;
    private ImageButton ib_next;
    private ImageButton ib_start;
    private ControllerService musicService;
    private boolean isPlay=false;
    private Toolbar toolbar;
    private Bitmap mBitmap;

    //对组件设置监听
    private void setListener() {

        title = (TextView) findViewById(R.id.title);
        artist = (TextView)findViewById(R.id.artist);
        imageView = (ImageView) findViewById(R.id.album);
        current = (TextView)findViewById(R.id.current);
        draution = (TextView) findViewById(R.id.duration);
        seekBar = (SeekBar) findViewById(R.id.seek);
        ib_pre = (ImageButton) findViewById(R.id.iv_pre);
        ib_start = (ImageButton) findViewById(R.id.iv_start);
        ib_next = (ImageButton) findViewById(R.id.iv_next);

        title.setOnClickListener(this);
        artist.setOnClickListener(this);
        imageView.setOnClickListener(this);
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
        AppCache.get().getMusicList();
        setContentView(R.layout.music_fragment_play);
        initToolbar();
        setListener();
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
        intentFilter.addAction(GlobalUtils.Music.MUSIC_BROADCAST_ACTION);
        registerReceiver(musicBroadcastReceiver, intentFilter);
        //绑定service
        bindService(mControllerintent, myServiceConnection, 0);
        handler.post(runnable);
    }

    //接受广播发送的消息，响应音乐在不同播放状态下ui的变化
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
                    pauseState();
                case CONSTAN_MUSIC_ERROR:
                    errorState();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    }

    //音乐暂停状态，界面图标显示
    private void pauseState(){
            isPlay=false;
            ib_start.setImageResource(R.mipmap.music_play);
    }

    public void errorState(){
        isPlay=false;
        ib_start.setImageResource(R.mipmap.music_play);
    }
    // 通过 Handler 更新 UI 上的组件状态
    public Handler handler = new Handler();

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                    if (musicService.musicPlay.getCurrentPosition1()==0){
                         seekBar.setProgress(0);
                         current.setText("00:00");
                    }else {
                        seekBar.setProgress((int) musicService.musicPlay.getCurrentPosition1());
                        current.setText(formatTime("mm:ss", musicService.musicPlay.getCurrentPosition1()));
                    }
                    seekBar.setMax((int) musicService.musicPlay.getPlayingMusic().getDuration());
                    title.setText(musicService.musicPlay.getPlayingMusic().getTitle());
                    draution.setText(formatTime("mm:ss",musicService.musicPlay.getPlayingMusic().getDuration()));
                    if (musicService.musicPlay.isPlaying()) {
                        ib_start.setImageResource(R.mipmap.music_pause);
                    } else {
                        ib_start.setImageResource(R.mipmap.music_play);
                    }
                      handler.post(runnable);
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
                 musicService=null;
        }
    }

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
                case MusicPlay.ACTION_MEDIA_PREVIOUS:

                    mHandler.sendEmptyMessage(CONSTANT_MUSIC_PRE);
                    break;
                case MusicPlay.ACTION_MEDIA_COMPLETE:

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

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.music_back);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        mBitmap = BitmapUtils.scaleBitmapFromResources(this,R.drawable.back,15,30);
        ab.setHomeAsUpIndicator(new BitmapDrawable(mBitmap));
        ab.setTitle("音乐");
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        handler.post(runnable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(myServiceConnection);
            if (musicBroadcastReceiver!=null){
                unregisterReceiver(musicBroadcastReceiver);
            }
            if (conn!=null){
                unbindService(conn);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}



