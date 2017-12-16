package com.idx.smartspeakdock.baidu.wakeup;


import android.content.Context;
import android.content.Intent;

import com.idx.smartspeakdock.Intents;
import com.idx.smartspeakdock.baidu.recognise.IWakeupListener;
import com.idx.smartspeakdock.baidu.recognise.WakeUpResult;
import com.idx.smartspeakdock.utils.Logger;

/**
 * Created by fujiayi on 2017/6/21.
 */

public class SimpleWakeupListener implements IWakeupListener {

    private Context mContext;

    public SimpleWakeupListener(Context context) {
        mContext = context;
    }

    private static final String TAG = "SimpleWakeupListener";

    @Override
    public void onSuccess(String word, WakeUpResult result) {
//        Intent intent = new Intent(mContext, SecondActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        mContext.startActivity(intent);
        mContext.sendBroadcast(new Intent(Intents.ACTION_WAKE_UP));

        Logger.info(TAG, "唤醒成功，唤醒词：" + word);
    }

    @Override
    public void onStop() {
        Logger.info(TAG, "唤醒词识别结束：");
    }

    @Override
    public void onError(int errorCode, String errorMessge, WakeUpResult result) {
        Logger.info(TAG, "唤醒错误：" + errorCode + ";错误消息：" + errorMessge + "; 原始返回" + result.getOrigalJson());
    }

    @Override
    public void onASrAudio(byte[] data, int offset, int length) {
        Logger.error(TAG, "audio data： " + data.length);
    }

}
