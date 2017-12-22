package com.idx.smartspeakdock.baidu.unit.listener;

import android.content.Context;
import android.content.Intent;

import com.idx.smartspeakdock.Actions;
import com.idx.smartspeakdock.calendar.CalendarActivity;
import com.idx.smartspeakdock.map.MapActivity;
import com.idx.smartspeakdock.music.MusicMainActivity;
import com.idx.smartspeakdock.weather.ui.WeatherActivity;

/**
 * Created by derik on 17-12-22.
 */

public class ActionListener implements IActionListener{
    private Context mContext;
    private Intent mIntent;
    public ActionListener(Context context){
        mContext = context;
    }
    @Override
    public boolean onAction(String actionId) {
        return handleAction(actionId);
    }

    private boolean handleAction(String actionId){
        switch (actionId){
            case Actions.Calender.OPEN_CALENDER:
                mIntent = new Intent(mContext, CalendarActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Calender.CLOSE_CALENDER:
                return true;
            case Actions.Weather.OPEN_WEATHER:
                mIntent = new Intent(mContext, WeatherActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Weather.CLOSE_WEATHER:
                return true;
            case Actions.Map.OPEN_MAP:
                mIntent = new Intent(mContext, MapActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Map.CLOSE_MAP:
                return true;
            case Actions.Music.OPEN_MUSIC:
                mIntent = new Intent(mContext, MusicMainActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(mIntent);
                return true;
            case Actions.Music.CLOSE_MUSIC:
                return true;
            case Actions.Shopping.OPEN_SHOPPING:
                return true;
            case Actions.Shopping.CLOSE_SHOPPING:
                return true;
            default:
                return false;

        }

    }
}
