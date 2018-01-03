package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.Modules;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.TTSManager;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.map.PathWay;
import com.idx.smartspeakdock.map.SearchArea;
import com.idx.smartspeakdock.utils.GlobalUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by derik on 17-12-22.
 */

public class VoiceActionAdapter implements IVoiceActionListener {
    private Context mContext;
    private Intent mIntent;

    private IWeatherVoiceListener mWeatherListener;
    private ICalenderVoiceListener mCalenderListener;
    private IMusicVoiceListener mMusicListener;
    private IMapVoiceListener mMapListener;
    private IShoppingVoiceListener mShoppingListener;

    private Set originalWords = new HashSet<String>();

    public VoiceActionAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean onAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {
        return handleAction(action, schema);
    }

    public void setWeatherListener(IWeatherVoiceListener listener) {
        mWeatherListener = listener;
    }

    public void setCalenderListener(ICalenderVoiceListener listener) {
        mCalenderListener = listener;
    }

    public void setMapListener(IMapVoiceListener listener) {
        mMapListener = listener;
    }

    public void setMusicListener(IMusicVoiceListener listener) {
        mMusicListener = listener;
    }

    public void setShoppingListener(IShoppingVoiceListener listener) {
        mShoppingListener = listener;
    }

    /**
     * @param action
     * @param schema
     * @return boolean 会话是否结束，true为结束，不再监听语音输入
     */
    private boolean handleAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {

        Log.d("handleAction name", ": " + action.actionId);
        String musicIndex = null;
        String musicName = null;

        SearchArea searchArea = null;
        String searchName = null;
        PathWay pathWay = null;
        originalWords.clear();

        for (int i = 0; i < schema.botMergedSlots.size(); i++) {
            String word = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(i)).original_word;
            originalWords.add(word);
        }

        switch (action.actionId) {

            /**开启指令*/
            case Actions.OPEN_MODULE:
                return false;
            case Actions.OPEN_NOW:
                if (originalWords.contains(Modules.CALENDER)) {
                    openModule(Modules.CALENDER);
                } else if (originalWords.contains(Modules.WEATHER)) {
                    openModule(Modules.WEATHER);
                } else if (originalWords.contains(Modules.MAP)) {
                    openModule(Modules.MAP);
                } else if (originalWords.contains(Modules.MUSIC)) {
                    openModule(Modules.MUSIC);
                } else if (originalWords.contains(Modules.SHOPPING)) {
                    openModule(Modules.SHOPPING);
                }
                return true;

            /**音乐指令*/
            case Actions.Music.MUSIC_INDEX:
                //TODO musicIndex =
            case Actions.Music.MUSIC_NAME:
                //TODO musicName =
                return false;
            case Actions.Music.MUSIC_PLAY:
                if (mMusicListener != null) {
                    if (musicIndex != null) {
                        mMusicListener.onPlay(musicIndex);
                    } else if (musicName != null) {
                        mMusicListener.onPlay(musicName);
                    } else {
                        mMusicListener.onPlay(0);
                    }
                }
                return true;
            case Actions.Music.MUSIC_PAUSE:
                if (mMusicListener != null) {
                    mMusicListener.onPause();
                }
                return true;
            case Actions.Music.MUSIC_CONTINUE:
                if (mMusicListener != null) {
                    mMusicListener.onContinue();
                }
                return true;
            case Actions.Music.MUSIC_NEXT:
                if (mMusicListener != null) {
                    mMusicListener.onNext();
                }
                return true;
            case Actions.Music.MUSIC_PREVIOUS:
                if (mMusicListener != null) {
                    mMusicListener.onPrevious();
                }
                return true;

            /**日历指令*/
            case Actions.Calender.CALENDER_WEEK_INFO:
                if (mCalenderListener != null) {
                    String weekInfo = mCalenderListener.onWeekInfo();
                    if (weekInfo != null && !weekInfo.equals("")) {
                        TTSManager.getInstance().speak(weekInfo);
                    }
                }
                return true;
            case Actions.Calender.CALENDER_FESTIVAL_INFO:
                if (mCalenderListener != null) {
                    String festivalInfo = mCalenderListener.onFestivalInfo();
                    if (festivalInfo != null && !festivalInfo.equals("")) {
                        TTSManager.getInstance().speak(festivalInfo);
                    }
                }
                return true;
            case Actions.Calender.CALENDER_ACT_INFO:
                if (mCalenderListener != null) {
                    String actInfo = mCalenderListener.onActInfo();
                    if (actInfo != null && !actInfo.equals("")) {
                        TTSManager.getInstance().speak(actInfo);
                    }
                }
                return true;
            case Actions.Calender.CALENDER_DATE_INFO:
                if (mCalenderListener != null) {
                    String dateInfo = mCalenderListener.onDateInfo();
                    if (dateInfo != null && !dateInfo.equals("")) {
                        TTSManager.getInstance().speak(dateInfo);
                    }
                }
                return true;
            case Actions.Calender.CALENDER_LUNAR_DATE_INFO:
                if (mCalenderListener != null) {
                    String lunarDateInfo = mCalenderListener.onLunarDateInfo();
                    if (lunarDateInfo != null && !lunarDateInfo.equals("")) {
                        TTSManager.getInstance().speak(lunarDateInfo);
                    }
                }
                return true;

            /**
             * 地图指令
             */
            case Actions.Map.MAP_LOCATION_INFO:
                if (mMapListener != null) {
                    String locationInfo = mMapListener.onLocationInfo();
                    if (locationInfo != null && !locationInfo.equals("")) {
                        TTSManager.getInstance().speak(locationInfo);
                    }
                }
                return true;
            case Actions.Map.MAP_SEARCH_AREA:
                //TODO searchArea =
                return false;
            case Actions.Map.MAP_SEARCH_NAME:
                searchName = UnitManager.getInstance().getSendMsg();
                if (mMapListener != null) {
                    mMapListener.onSearchInfo(searchName, searchArea);
                }
                return false;
            case Actions.Map.MAP_SEARCH_ADDRESS:
                if (mMapListener != null) {
                    mMapListener.onSearchAddress("");
                }
                return true;
            case Actions.Map.MAP_PATH_INFO:
                if (mMapListener != null) {
                    mMapListener.onPathInfo("", "", null);
                }
                return false;
            default:
                return false;
        }

    }

    private void openModule(String name) {
        if (mIntent != null) mIntent = null;
        mIntent = new Intent(mContext, SwipeActivity.class);
        switch (name) {
            case Modules.CALENDER:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.WEATHER:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.WEATHER_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.MAP:
                if (mIntent != null) mIntent = null;
                mIntent = new Intent(mContext, MapActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(mIntent);
                break;
            case Modules.MUSIC:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.MUSIC_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.SHOPPING:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT, GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;

        }
    }

}
