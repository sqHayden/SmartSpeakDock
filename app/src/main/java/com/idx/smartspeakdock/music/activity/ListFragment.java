package com.idx.smartspeakdock.music.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.idx.smartspeakdock.BaseFragment;
import com.idx.smartspeakdock.R;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.music.adapter.LocalListAdapter;
import com.idx.smartspeakdock.music.adapter.ViewPagerAdapter;
import com.idx.smartspeakdock.music.entity.LocalMusic;
import com.idx.smartspeakdock.music.entity.LocalMusicItem;
import com.idx.smartspeakdock.music.service.LocalPlayService;
import com.idx.smartspeakdock.music.util.LocalMusicList;
import com.idx.smartspeakdock.music.util.LocalToLocalItem;
import com.idx.smartspeakdock.music.util.MediaUtils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ryan on 17-12-28.
 * Email: Ryan_chan01212@yeah.net
 */

public class ListFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG = ListFragment.class.getSimpleName();
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
    public boolean mIsPlaying = false;
    private Context mContext;
    private View mView;
    private View barView;


    public static ListFragment newInstance(){return new ListFragment();}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.music_activity_list,container,false);
        initView(mView);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addListener();

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

    private void addListener() {
        // 弹出播放器界面
        barView.setOnClickListener(this);

        // 播放条暂停按钮事件监听器
        mBarPauseButton.setOnClickListener(this);

        // 播放条暂停按钮背景事件监听器
        mBarPauseBackground.setOnClickListener(this);

        // 播放条下一首事件监听器
        mBarNextButton.setOnClickListener(this);

        // 事件广播接受器
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mEventReceiver = new BroadcastReceiver() {
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
            if (ActivityCompat.checkSelfPermission(mContext, permission)
                    != PermissionChecker.PERMISSION_GRANTED) {
                requestList.add(permission);
            }
        }

        if (requestList.size() > 0) {
            ActivityCompat.requestPermissions(getActivity(), requestList.toArray(new String[] {}),
                    PERMISSION_REQUEST_CODE);
        } else {
            init();
        }
    }

    public void initView(View view){
        barView = view.findViewById(R.id.bar);
        mBarTitle = (TextView) barView.findViewById(R.id.title);
        mBarArtist = (TextView) barView.findViewById(R.id.artist);
        mBarAlbum = (ImageView) barView.findViewById(R.id.album);
        mBarPauseButton = (ImageButton) barView.findViewById(R.id.home_pauseButton);
        mBarNextButton = (ImageButton) barView.findViewById(R.id.home_nextButton);
        mBarPauseBackground = barView.findViewById(R.id.homebar_background);
        mPaperTitleStrip = (PagerTitleStrip) view.findViewById(R.id.title_strip);

        //设置tab栏字体
        mPaperTitleStrip.setTextColor(getResources().getColor(R.color.text_color));
        mBarTitle.setHorizontallyScrolling(true);
        mBarTitle.setSelected(true);
        mBarArtist.setHorizontallyScrolling(true);
        mBarArtist.setSelected(true);

        enableButton(false, true);

    }

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
            serviceIntent.setClass(getActivity(), LocalPlayService.class);

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
            getActivity().startService(serviceIntent);
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
            List<LocalMusic> list = LocalMusicList.getAudioList(mContext, cmps[i]);
            List<LocalMusicItem> itemList = new ArrayList<>();
            for (LocalMusic localMusic : list) {
                itemList.add(trans[i].apply(localMusic));
            }
            mListOfAudioItemList.add(itemList);
            final LocalListAdapter adapter = new LocalListAdapter(mContext, R.layout.music_list, itemList);
            mAdapterList.add(adapter);
            ListView listView = new ListView(mContext);
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

        ViewPager viewPager = (ViewPager) mView.findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(viewList.size() - 1);
        viewPager.setAdapter(new ViewPagerAdapter(titleList, viewList));
    }

    private void pause() {
        if (mLastPlay >= 0) {
            Intent intent = new Intent(getActivity(), LocalPlayService.class);
            if (mIsPlaying) {
                intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.PAUSE_ACTION);
            } else {
                intent.putExtra(LocalPlayService.ACTION_KEY, LocalPlayService.REPLAY_ACTION);
            }
            enableButton(false);
            getActivity().startService(intent);
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
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(new Intent(getActivity(), LocalPlayService.class));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mEventReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                boolean good = true;
                for (int i = 0; i < permissions.length; ++i) {
                    if (grantResults[i] != PermissionChecker.PERMISSION_GRANTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("提示").setMessage("不允许读取SD卡权限")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().finish();
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

   /* @Override
    public void finish() {
        getActivity().moveTaskToBack(false);
    }*/

    private void start() {
        if (mPlayingIndex >= 0 && mPlayingIndex < mListOfAudioItemList.size()) {
            List<LocalMusicItem> localMusicItemList = mListOfAudioItemList.get(mPlayingIndex);
            if (mLastPlay >= 0 && mLastPlay < localMusicItemList.size()) {
                Intent intent = getAudioIntent(localMusicItemList.get(mLastPlay).getAudio());
                intent.setClass(mContext, PlayerActivity.class);
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
                if (mPlayingIndex >= 0 && mPlayingIndex < mListOfAudioItemList.size()) {
                    List<LocalMusicItem> localMusicItemList = mListOfAudioItemList.get(mPlayingIndex);
                    if (mLastPlay >= 0 && mLastPlay < localMusicItemList.size()) {
                        Intent intent = getAudioIntent(localMusicItemList.get(mLastPlay).getAudio());
                        intent.setClass(mContext, PlayerActivity.class);
                        intent.putExtra(LocalPlayService.AUDIO_IS_PLAYING_BOOL, mIsPlaying);
                        getActivity().startActivity(intent);
 //                       overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
                    }
                }
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

}
