package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.Modules;
import com.idx.smartspeakdock.Swipe.SwipeActivity;
import com.idx.smartspeakdock.baidu.control.TTSManager;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.utils.GlobalUtils;


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

    private boolean handleAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema) {

        Log.d("handleAction", ": " + action.actionId);
        String musicIndex = null;
        String musicName = null;

        switch (action.actionId) {

            /**开启指令*/
            case Actions.OPEN_MODULE:
                return false;
            case Actions.OPEN_NOW:
                //多个槽点时，需另做处理
                String moduleName = ((CommunicateResponse.Schema.MergedSlots) schema.botMergedSlots.get(0)).original_word;
                openModule(moduleName);
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
                    } else if (musicName != null){
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
            default:
                return false;
        }

    }

    private void openModule(String name) {
        if(mIntent != null) mIntent = null;
        mIntent = new Intent(mContext, SwipeActivity.class);
        switch (name){
            case Modules.CALENDER:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.CALENDAR_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.WEATHER:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.WEATHER_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.MAP:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.MAP_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.MUSIC:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.MUSIC_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;
            case Modules.SHOPPING:
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mIntent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.SHOPPING_FRAGMENT_INTENT_ID);
                mContext.startActivity(mIntent);
                break;

        }
    }

}
