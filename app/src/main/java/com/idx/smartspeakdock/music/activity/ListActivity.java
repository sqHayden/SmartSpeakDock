package com.idx.smartspeakdock.music.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.Swipe.SwipeFragment;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.calendar.CalendarActivity;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.music.adapter.LocalListAdapter;
import com.idx.smartspeakdock.music.adapter.ViewPagerAdapter;
import com.idx.smartspeakdock.music.entity.LocalMusic;
import com.idx.smartspeakdock.music.entity.LocalMusicItem;
import com.idx.smartspeakdock.music.service.LocalPlayService;
import com.idx.smartspeakdock.music.util.LocalMusicList;
import com.idx.smartspeakdock.music.util.LocalToLocalItem;
import com.idx.smartspeakdock.music.util.MediaUtils;
import com.idx.smartspeakdock.start.StartActivity;
import com.idx.smartspeakdock.utils.ActivityUtils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ListActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static String[] permissionArray = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };

    private List<BaseAdapter> mAdapterList;

    private TextView mBarTitle;
    private TextView mBarArtist;
    private ImageView mBarAlbum;
    private ImageButton mBarPauseButton;
    private ImageButton mBarNextButton;
    private View mBarPauseBackground;
    private PagerTitleStrip mPaperTitleStrip;

    private BroadcastReceiver mEventReceiver;

    private List<List<LocalMusicItem>> mListOfAudioItemList;
    private int mPlayingIndex = -1;


    private int mLastPlay = -1;

    private boolean mIsPlaying = false;
    DrawerLayout mDrawerLayout;

    private static String getPinyinString(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(ch);
            if (pinyin != null) {
                builder.append(pinyin[0]);
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private Intent getAudioIntent(LocalMusic localMusic) {
        Intent intent = new Intent();
        intent.putExtra(LocalPlayService.AUDIO_PATH_STR, localMusic.getPath());
        intent.putExtra(LocalPlayService.AUDIO_TITLE_STR, localMusic.getTitle());
        intent.putExtra(LocalPlayService.AUDIO_ARTIST_STR, localMusic.getArtist());
        intent.putExtra(LocalPlayService.AUDIO_ALBUM_ID_INT, localMusic.getAlbumId());
        intent.putExtra(LocalPlayService.AUDIO_DURATION_INT, localMusic.getDuration());
        intent.putExtra(LocalPlayService.AUDIO_CURRENT_INT, 0);
        return intent;
    }

    /**
     *
     * @param position 在原始音乐列表的位置
     * @param shuffle  是否再次打乱顺序
     */
    private void playAudio(int position, boolean start, boolean shuffle, boolean forced) {
        if (forced || position != mLastPlay) {
            List<LocalMusicItem> list = mListOfAudioItemList.get(mPlayingIndex);
            LocalMusicItem localMusicItem = list.get(position);
            LocalMusic localMusic = localMusicItem.getAudio();



            Intent serviceIntent = getAudioIntent(localMusic);
            serviceIntent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.PLAY_ACTION);
            serviceIntent.putExtra(LocalPlayService.AUDIO_PLAY_NOW_BOOL, start);
            serviceIntent.setClass(this, LocalPlayService.class);

            mLastPlay = position;

            mBarTitle.setText(localMusic.getTitle());

            mBarArtist.setText(localMusic.getArtist());

            Bitmap bitmap = MediaUtils.getAlbumBitmapDrawable(localMusic);
            if (bitmap != null) {
                mBarAlbum.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
            } else {
                mBarAlbum.setImageResource(R.drawable.no_album);
            }
            enableButton(false);
            startService(serviceIntent);
        }
    }



    /**
     * 歌曲切换
     * @param next      是否为下一首
     * @param fromUser  是否来自用户的动作
     */
    private void musicChange(boolean next, boolean fromUser) {
//
            if (!fromUser){
            playAudio(mLastPlay, true, false, true);
           } else {
               int index;
//
                int listSize = mListOfAudioItemList.get(mPlayingIndex).size();
                if (next) {
                    index = (mLastPlay + 1) % listSize;
                } else {
                    index = (mLastPlay - 1 + listSize) % listSize;
                }
//
                playAudio(index, mIsPlaying, true, true);
//
        }
    }

    /**
     * 初始化列表
     */
    private void init() {
        // 标题
        final String[] titles = new String[] {
            "本地音乐", "网络"
        };
        // 排序比较器
        Comparator[] cmps = new Comparator[] {
                new Comparator<LocalMusic>() {
                    @Override
                    public int compare(LocalMusic o1, LocalMusic o2) {
                        return getPinyinString(o1.getTitle())
                                .compareToIgnoreCase(getPinyinString(o2.getTitle()));
                    }
                },
                new Comparator<LocalMusic>() {
                    @Override
                    public int compare(LocalMusic o1, LocalMusic o2) {
                        return getPinyinString(o1.getTitle())
                                .compareToIgnoreCase(getPinyinString(o2.getTitle()));
                    }
                },
        };
        // 转换
        LocalToLocalItem[] trans = new LocalToLocalItem[] {
                new LocalToLocalItem() {
                    @Override
                    public LocalMusicItem apply(LocalMusic audio) {
                        LocalMusicItem localMusicItem = new LocalMusicItem(audio);
                        String title = getPinyinString(audio.getTitle()).toUpperCase();
                        localMusicItem.setClassificationId(title.length() > 0 ? title.charAt(0) : -1);
                        localMusicItem.setClassificationName(title.length() > 0 ? title.charAt(0) + "" : "");
                        return localMusicItem;
                    }
                },
                new LocalToLocalItem() {
                    @Override
                    public LocalMusicItem apply(LocalMusic audio) {
                        LocalMusicItem localMusicItem = new LocalMusicItem(audio);
                        String title = getPinyinString(audio.getTitle()).toUpperCase();
                        localMusicItem.setClassificationId(title.length() > 0 ? title.charAt(0) : -1);
                        localMusicItem.setClassificationName(title.length() > 0 ? title.charAt(0) + "" : "");
                        return localMusicItem;
                    }
                },
        };

        List<View> viewList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();

        mAdapterList = new ArrayList<>();
        mListOfAudioItemList = new ArrayList<>();

        for (int i = 0; i < titles.length; ++i) {
            List<LocalMusic> list = LocalMusicList.getAudioList(this, cmps[i]);
            List<LocalMusicItem> itemList = new ArrayList<>();
            for (LocalMusic localMusic : list) {
                itemList.add(trans[i].apply(localMusic));
            }
            mListOfAudioItemList.add(itemList);
            final LocalListAdapter adapter = new LocalListAdapter(this, R.layout.music_list, itemList);
            mAdapterList.add(adapter);
            ListView listView = new ListView(this);
            listView.setAdapter(adapter);
            final int index = i;
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mPlayingIndex = index;
                    playAudio(position, true, true, false);
                    adapter.notifyDataSetChanged();
                }
            });
            viewList.add(listView);
            titleList.add(titles[i]);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(viewList.size() - 1);
        viewPager.setAdapter(new ViewPagerAdapter(titleList, viewList));
    }

    private void pause() {
        if (mLastPlay >= 0) {
            Intent intent = new Intent(ListActivity.this, LocalPlayService.class);
            if (mIsPlaying) {
                intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.PAUSE_ACTION);
            } else {
                intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.REPLAY_ACTION);
            }
            enableButton(false);
            startService(intent);
        }
    }

    private void enableButton(boolean enable) {
        enableButton(enable, false);
    }

    private void enableButton(boolean enable, boolean grey) {
        mBarPauseButton.setEnabled(enable);
        mBarPauseBackground.setEnabled(enable);
        mBarNextButton.setEnabled(enable);

        if (grey && !enable) {
            mBarPauseBackground.setBackgroundResource(R.drawable.shadowed_circle_grey);
        } else {
            mBarPauseBackground.setBackgroundResource(R.drawable.shadowed_circle_red);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_activity_list);


        View barView = findViewById(R.id.bar);
        mBarTitle = (TextView) barView.findViewById(R.id.title);
        mBarArtist = (TextView) barView.findViewById(R.id.artist);
        mBarAlbum = (ImageView) barView.findViewById(R.id.album);
        mBarPauseButton = (ImageButton) barView.findViewById(R.id.home_pauseButton);
        mBarNextButton = (ImageButton) barView.findViewById(R.id.home_nextButton);
        mBarPauseBackground = barView.findViewById(R.id.homebar_background);
        mPaperTitleStrip = (PagerTitleStrip) findViewById(R.id.title_strip);

        //设置tab栏字体
        mPaperTitleStrip.setTextColor(getResources().getColor(R.color.text_color));
        mBarTitle.setHorizontallyScrolling(true);
        mBarTitle.setSelected(true);
        mBarArtist.setHorizontallyScrolling(true);
        mBarArtist.setSelected(true);

        enableButton(false, true);

        // 弹出播放器界面
        barView.setOnClickListener(this);

        // 播放条暂停按钮事件监听器
        mBarPauseButton.setOnClickListener(this);

        // 播放条暂停按钮背景事件监听器
        mBarPauseBackground.setOnClickListener(this);

        // 播放条下一首事件监听器
        mBarNextButton.setOnClickListener(this);

        // 事件广播接受器
        LocalBroadcastManager.getInstance(this).registerReceiver(mEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String event = intent.getStringExtra(LocalPlayService.EVENT_KEY);
                if (event == null) {
                    return;
                }
                switch (event) {
                    case LocalPlayService.FINISHED_EVENT:
                        musicChange(true, false);
                        if (mPlayingIndex >= 0 && mPlayingIndex < mAdapterList.size()) {
                            mAdapterList.get(mPlayingIndex).notifyDataSetChanged();
                        }
                        break;
                    case LocalPlayService.NEXT_EVENT:
                        musicChange(true, true);
                        if (mPlayingIndex >= 0 && mPlayingIndex < mAdapterList.size()) {
                            mAdapterList.get(mPlayingIndex).notifyDataSetChanged();
                        }
                        break;
                    case LocalPlayService.PREVIOUS_EVENT:
                        musicChange(false, true);
                        if (mPlayingIndex >= 0 && mPlayingIndex < mAdapterList.size()) {
                            mAdapterList.get(mPlayingIndex).notifyDataSetChanged();
                        }
                        break;
                    case LocalPlayService.PLAY_EVENT:
                        boolean isPlay = intent.getBooleanExtra(LocalPlayService.AUDIO_PLAY_NOW_BOOL, false);
                        if (isPlay) {
                            mBarPauseButton.setImageResource(R.drawable.pause_light);
                            mIsPlaying = true;
                        } else {
                            mBarPauseButton.setImageResource(R.drawable.play_light);
                            mIsPlaying = false;
                        }
                        enableButton(true);
                        break;
                    case LocalPlayService.PAUSE_EVENT:
                        mBarPauseButton.setImageResource(R.drawable.play_light);
                        mIsPlaying = false;
                        enableButton(true);
                        break;
                    case LocalPlayService.REPLAY_EVENT:
                        mBarPauseButton.setImageResource(R.drawable.pause_light);
                        mIsPlaying = true;
                        enableButton(true);
                        break;
//
                }
            }
        }, new IntentFilter(LocalPlayService.BROADCAST_EVENT_FILTER));

        // 权限分配
        List<String> requestList = new ArrayList<>();

        for (String permission : permissionArray) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PermissionChecker.PERMISSION_GRANTED) {
                requestList.add(permission);
            }
        }

        if (requestList.size() > 0) {
            ActivityCompat.requestPermissions(this, requestList.toArray(new String[] {}),
                    PERMISSION_REQUEST_CODE);
        } else {
            init();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.music_title));
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorSelfBlack);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        UnitManager.getInstance().setMusicVoiceListener(new IMusicVoiceListener() {
            @Override
            public void onPlay(int index) {
                Log.d(TAG, "onPlay: index");
                start();
            }

            @Override
            public void onPlay(String name) {
                Log.d(TAG, "onPlay: name");
                mPlayingIndex = 1;
                start();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onContinue() {
                start();
            }

            @Override
            public void onNext() {
                musicChange(true, true);
            }

            @Override
            public void onPrevious() {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_weather:
//                                NavUtils.navigateUpFromSameTask(MusicMainActivity.this);
                                SwipeFragment swipeFragment =
                                        (SwipeFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                                if (swipeFragment == null) {
                                    swipeFragment = SwipeFragment.newInstance();
                                    ActivityUtils.addFragmentToActivity(
                                            getSupportFragmentManager(), swipeFragment, R.id.contentFrame);
                                }
                                break;
                            case R.id.list_navigation_calendar:
                                // TODO: 17-12-16 start CalendarActivity
                                startActivity(new Intent(ListActivity.this, CalendarActivity.class));
                                break;
                            case R.id.list_navigation_music:
                                // TODO: 17-12-16  Do nothing, we're already on that screen
                                break;
                            case R.id.list_navigation_shopping:
                                // TODO: 17-12-16 start ShoppingActivty
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.flnet.com"));
                                //List<ResolveInfo> list = pm.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
                                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                                Log.i(TAG, "onNavigationItemSelected: list.size() = "+list.size());
                                if (list.size() > 0) {
                                    Log.i(TAG, "onNavigationItemSelected: start");
                                    startActivity(intent);
                                }
                                break;
                            case R.id.list_navigation_map:
                                // TODO: 17-12-16 start MapActivity
                                startActivity(new Intent(ListActivity.this, MapActivity.class));
                                break;
                            case R.id.list_navigation_voice:
                                // TODO: 17-12-16 start voice function
                                startActivity(new Intent(ListActivity.this, StartActivity.class));
                                break;
                            case R.id.list_navigation_setting:
                                // TODO: 17-12-16 start SettingActivity
                                break;
                            default:
                                break;
                        }

                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LocalPlayService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mEventReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                boolean good = true;
                for (int i = 0; i < permissions.length; ++i) {
                    if (grantResults[i] != PermissionChecker.PERMISSION_GRANTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                        builder.setTitle("提示").setMessage("不允许读取SD卡权限")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ListActivity.super.finish();
                                    }
                                }).show();
                        good = false;
                        break;
                    }
                }
                if (good) {
                    init();
                }
                break;
        }
    }

    @Override
    public void finish() {
        moveTaskToBack(false);
    }

    private void start() {
        if (mPlayingIndex >= 0 && mPlayingIndex < mListOfAudioItemList.size()) {
            List<LocalMusicItem> localMusicItemList = mListOfAudioItemList.get(mPlayingIndex);
            if (mLastPlay >= 0 && mLastPlay < localMusicItemList.size()) {
                Intent intent = getAudioIntent(localMusicItemList.get(mLastPlay).getAudio());
                intent.setClass(ListActivity.this, PlayerActivity.class);
                intent.putExtra(LocalPlayService.AUDIO_IS_PLAYING_BOOL, mIsPlaying);
                startActivity(intent);
//                        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 弹出播放器界面
            case R.id.bar:
                start();
                break;
            // 暂停按钮
            case R.id.home_pauseButton: case R.id.homebar_background:
                pause();
                break;
            // 下一首
            case R.id.home_nextButton:
                musicChange(true, true);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mIsPlaying) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    AlertDialog dialog = builder.setTitle("音乐正在播放")
                            .setPositiveButton("后台播放", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ListActivity.this.finish();
                                }
                            })
                            .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ListActivity.super.finish();
                                }
                            }).create();

                    dialog.show();
                } else {
                    super.finish();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
