package com.idx.smartspeakdock.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ryan on 18-1-13.
 * Email: Ryan_chan01212@yeah.net
 */

public class SharePrefrenceUtils {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public SharePrefrenceUtils(Context context){
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("shopping",Context.MODE_WORLD_WRITEABLE);
        mEditor = mSharedPreferences.edit();
    }

    public void saveFirstAppStart(String firstStart,boolean isFirst){
        mEditor.putBoolean(firstStart,isFirst);
        mEditor.commit();
    }

    public boolean getFirstAppStart(String firstStart){
        return mSharedPreferences.getBoolean(firstStart,true);
    }

    public void insertWebUrl(String webname,String weburl){
        mEditor.putString(webname,weburl);
        mEditor.commit();
    }

    public String getWebUrl(String webname){
        return mSharedPreferences.getString(webname,null);
    }

}
