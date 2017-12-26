package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.calendar.CalendarActivity;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.music.activity.ListActivity;
import com.idx.smartspeakdock.weather.ui.WeatherActivity;


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
    public boolean onAction(CommunicateResponse.Action action) {
        return handleAction(action);
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

    private boolean handleAction(CommunicateResponse.Action action) {
        Log.d("handleAction", ": " + action.actionId);
        switch (action.actionId) {
            /**日历控制*/
            case Actions.Calender.OPEN_CALENDER:
                mIntent = new Intent(mContext, CalendarActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Calender.CLOSE_CALENDER:
                return true;
            /**天气控制*/
            case Actions.Weather.OPEN_WEATHER:
                mIntent = new Intent(mContext, WeatherActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Weather.CLOSE_WEATHER:
                return true;
            /**地图控制*/
            case Actions.Map.OPEN_MAP:
                mIntent = new Intent(mContext, MapActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Map.CLOSE_MAP:
                return true;
            /**音乐控制*/
            case Actions.Music.OPEN_MUSIC:
                mIntent = new Intent(mContext, ListActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Music.MUSIC_NAME:
                return false;
            case Actions.Music.MUSIC_PLAY:
                if (mMusicListener != null) {
                    mMusicListener.onPlay(0);
                }
                return true;
            case Actions.Music.MUSIC_PAUSE:
                if (mMusicListener != null) {
                    mMusicListener.onPause();
                }
                return true;
            case Actions.Music.MUSIC_STOP:
                if (mMusicListener != null) {
                    mMusicListener.onStop();
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
            case Actions.Music.CLOSE_MUSIC:
                return true;
            /**购物控制*/
            case Actions.Shopping.OPEN_SHOPPING:
                return true;
            case Actions.Shopping.CLOSE_SHOPPING:
                return true;
            default:
                return false;
        }

    }

}
