package com.idx.smartspeakdock.music.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.music.service.LocalPlayService;
import com.idx.smartspeakdock.music.util.MediaUtils;

import com.andremion.music.MusicCoverView;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {


    private SeekBar mSeekBar;
    private TextView mCurrentTextView;
    private TextView mDurationTextView;
    private TextView mTitleTextView;
    private TextView mArtistTextView;

    private MusicCoverView  mAlbumImageView;
    private FrameLayout mFrameLayout;
    private ImageButton mPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private ImageButton mReturnButton;

    private View mPauseButtonBackground;

    private Object mLock = new Object();

    private String mTitle;
    private String mArtist;
    private int mDuration;
    private String mPath;

    private boolean onDrag = false;

    private boolean mIsPlay;

    private boolean mIsLoadAlbum = false;



    private boolean mIsAlbum;


    private BroadcastReceiver mPlayingReceiver;
    private BroadcastReceiver mPlayEventReceiver;


    /**
     * UI更新包含音乐信息的bundle
     */
    private void updateUI(Bundle bundle) {
        synchronized (mLock) {
            String title = bundle.getString(LocalPlayService.AUDIO_TITLE_STR);
            String artist = bundle.getString(LocalPlayService.AUDIO_ARTIST_STR);
            String path = bundle.getString(LocalPlayService.AUDIO_PATH_STR);

            if (mTitle == null || !mTitle.equals(title)) {
                mTitleTextView.setText(mTitle = title);
            }
            if (mArtist == null || !mArtist.equals(artist)) {
                mArtistTextView.setText(mArtist = artist);
            }

            if (mPath == null || !mPath.equals(path)) {

                updateAlbum(mPath);
            }

//            // 特殊处理，停止旋转需要时间
            boolean isPlay = bundle.getBoolean(LocalPlayService.AUDIO_IS_PLAYING_BOOL, false);
            if (isPlay != mAlbumImageView.isRunning()) {
               if (isPlay) {
                   mAlbumImageView.start();
               } else {
                   mAlbumImageView.stop();
               }
            }

            int duration = bundle.getInt(LocalPlayService.AUDIO_DURATION_INT, 0);
            int current = Math.min(bundle.getInt(LocalPlayService.AUDIO_CURRENT_INT, 0), duration);

            if (!onDrag) {
                int min = 0, max = mSeekBar.getMax();
                int pos = 0;
                if (duration != 0 && (max - min) != 0) {
                    pos = (int) ((current * 1.0 / duration) * (max - min));
                }
                mSeekBar.setProgress(pos);
            }

            int totalSecond = current / 1000;
            int minute = totalSecond / 60;
            int second = totalSecond % 60;
            if (!onDrag) {
                mCurrentTextView.setText(String.format("%02d:%02d", minute, second));
            }
            if (mDuration != duration) {
                totalSecond = (mDuration = duration) / 1000;
                minute = totalSecond / 60;
                second = totalSecond % 60;
                mDurationTextView.setText(String.format("%02d:%02d", minute, second));
            }
        }
    }

    // 更新专辑图片
    private void updateAlbum(String path) {
        Bitmap bitmap = MediaUtils.getAlbumBitmapDrawable(path);
        if (bitmap != null) {
            // 进行缩放处理
            int viewWidth = mFrameLayout.getWidth();
            int viewHeight = mFrameLayout.getHeight();
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            float scaleWidth = viewWidth * 1.0f / imageWidth;
            float scaleHeight = viewHeight * 1.0f / imageHeight;

            float scale = Math.max(1.0f, Math.max(scaleWidth, scaleHeight));

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resize = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true);
            mAlbumImageView.setImageDrawable(new BitmapDrawable(getResources(), resize));
        }
        else {
            mAlbumImageView.setImageResource(R.drawable.no_album);
        }
    }

    // 下一首
    private void nextMusic() {
        Intent intent = new Intent(this, LocalPlayService.class);
        intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.NEXT_ACTION);
        startService(intent);
    }

    // 上一首
    private void previousMusic() {
        Intent intent = new Intent(this, LocalPlayService.class);
        intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.PREVIOUS_ACTION);
        startService(intent);
    }

    // seek歌曲
    private void seekMusic(int seekTo) {
        Intent intent = new Intent(PlayerActivity.this, LocalPlayService.class);
        intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.SEEK_ACTION);
        intent.putExtra(LocalPlayService.AUDIO_SEEK_POS_INT, seekTo);
        startService(intent);
    }

    // 切换暂停
    private void pauseMusic() {
        Intent intent = new Intent(this, LocalPlayService.class);
        if (mIsPlay) {
            intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.PAUSE_ACTION);
        } else {
            intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.REPLAY_ACTION);
        }

        enableButton(false, false);
        startService(intent);
    }



    /** 切换按钮的可用状态 */
    public void enableButton(boolean enable) {
        enableButton(enable, false);
    }
    public void enableButton(boolean enable, boolean grey) {
        mPauseButton.setEnabled(enable);
        mPauseButtonBackground.setEnabled(enable);
        mNextButton.setEnabled(enable);
        mPreviousButton.setEnabled(enable);

        if (grey && !enable) {
            mPauseButtonBackground.setBackgroundResource(R.drawable.shadowed_circle_grey);
        } else {
            mPauseButtonBackground.setBackgroundResource(R.drawable.shadowed_circle_red);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_activity_play);


        mPath = getIntent().getStringExtra(LocalPlayService.AUDIO_PATH_STR);
        mIsPlay = getIntent().getBooleanExtra(LocalPlayService.AUDIO_IS_PLAYING_BOOL, false);

        findViewById(R.id.nextButton).setOnClickListener(this);
        findViewById(R.id.previousButton).setOnClickListener(this);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mCurrentTextView = (TextView) findViewById(R.id.current);
        mDurationTextView = (TextView) findViewById(R.id.duration);
        mTitleTextView = (TextView) findViewById(R.id.title);
        mArtistTextView = (TextView) findViewById(R.id.artist);
        mFrameLayout = (FrameLayout) findViewById(R.id.album);
        mPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mPreviousButton = (ImageButton) findViewById(R.id.previousButton);
        mReturnButton = (ImageButton) findViewById(R.id.returnButton);
        mPauseButtonBackground = findViewById(R.id.playPauseButtonBackground);

        mTitleTextView.setHorizontallyScrolling(true);
        mTitleTextView.setSelected(true);
        mArtistTextView.setHorizontallyScrolling(true);
        mArtistTextView.setSelected(true);

        mPauseButton.setOnClickListener(this);
        mPauseButtonBackground.setOnClickListener(this);


        mReturnButton.setOnClickListener(this);




        mIsAlbum = true;
        mAlbumImageView = new MusicCoverView(this);
        mAlbumImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mAlbumImageView.setCallbacks(new MusicCoverView.Callbacks() {
            @Override
            public void onMorphEnd(MusicCoverView coverView) {
            }

            @Override
            public void onRotateEnd(MusicCoverView coverView) {
                enableButton(true, true);
            }
        });
        mAlbumImageView.setShape(MusicCoverView.SHAPE_CIRCLE);
        mFrameLayout.addView(mAlbumImageView);

//         加载专辑图片
        mAlbumImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!mIsLoadAlbum) {
                    updateAlbum(mPath);
                    mIsLoadAlbum = true;
                }
                return true;
            }
        });

        //专辑封面旋转
        if (mIsPlay) {
            mAlbumImageView.start();
        }
            else {
            mPauseButton.setImageResource(R.drawable.play_light);
        }

        // 进度条事件
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int min = 0, max = seekBar.getMax();
                    int changedCurrent = (int) (mDuration * 1.0 / (max - min) * progress);
                    int totalSecond = changedCurrent / 1000;
                    int minute = totalSecond / 60;
                    int second = totalSecond % 60;
                    mCurrentTextView.setText(String.format("%02d:%02d", minute, second));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                synchronized (mLock) {
                    onDrag = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int min = 0, max = seekBar.getMax();
                int changedCurrent = (int) (mDuration * 1.0 / (max - min) * seekBar.getProgress());
                seekMusic(changedCurrent);
                synchronized (mLock) {
                    onDrag = false;
                }
            }
        });

        mFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrameLayout.removeAllViews();
              if (!mIsAlbum){
                    mFrameLayout.addView(mAlbumImageView);
                    if (mIsPlay != mAlbumImageView.isRunning()) {
                        if (mIsPlay) {
                            mAlbumImageView.start();
                        } else {
                            mAlbumImageView.stop();
                        }
                    }
                }
                mIsAlbum = !mIsAlbum;
            }
        });

        updateUI(getIntent().getExtras());

        // 更新UI广播
        LocalBroadcastManager.getInstance(this).registerReceiver(mPlayingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI(intent.getExtras());
            }
        }, new IntentFilter(LocalPlayService.BROADCAST_PLAYING_FILTER));

        // 事件广播
        LocalBroadcastManager.getInstance(this).registerReceiver(mPlayEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String event = intent.getStringExtra(LocalPlayService.EVENT_KEY);
                if (event == null) {
                    return;
                }
                switch (event) {
                    case LocalPlayService.PAUSE_EVENT:
                        synchronized (mLock) {
                            mPauseButton.setImageResource(R.drawable.play_light);
                            mAlbumImageView.stop();
                            enableButton(true);

                        }
                        mIsPlay = false;
                        break;
                    case LocalPlayService.REPLAY_EVENT:
                        synchronized (mLock) {
                            mPauseButton.setImageResource(R.drawable.pause_light);
                            mAlbumImageView.start();
                            enableButton(true);
                        }
                        mIsPlay = true;
                        break;
                    case LocalPlayService.PLAY_EVENT:
                        synchronized (mLock) {
                            boolean isPlay = intent.getBooleanExtra(LocalPlayService.AUDIO_PLAY_NOW_BOOL, false);
                            if (isPlay) {
                                mPauseButton.setImageResource(R.drawable.pause_light);
                                mAlbumImageView.start();
                                enableButton(true);
                                mIsPlay = true;
                            } else {
                                mPauseButton.setImageResource(R.drawable.play_light);
                                if (mAlbumImageView.isRunning()) {
                                    if (mIsAlbum) {
                                        enableButton(false, true);
                                        mAlbumImageView.stop();
                                    } else {
                                        enableButton(true);
                                    }
                                }
                                  else {
                                    enableButton(true);
                                }
                                mIsPlay = false;
                            }
                        }
                        break;
                }
            }
        }, new IntentFilter(LocalPlayService.BROADCAST_EVENT_FILTER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPlayingReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPlayEventReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateUI(intent.getExtras());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playPauseButton: case R.id.playPauseButtonBackground:
                pauseMusic();
                break;
            case R.id.nextButton:
                nextMusic();
                break;
            case R.id.previousButton:
                previousMusic();
                break;
            case R.id.returnButton:
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
//                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
