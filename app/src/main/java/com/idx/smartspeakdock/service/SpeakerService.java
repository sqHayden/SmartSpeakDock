package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.idx.smartspeakdock.Intents;
import com.idx.smartspeakdock.broadcast.SpeakerBroadcastReceiver;
import com.idx.smartspeakdock.baidu.recognise.IStatus;

/**
 * Created by derik on 17-12-15.
 */

public class SpeakerService extends Service {

    private static final String TAG = SpeakerService.class.getName();

    private SpeakerBroadcastReceiver mReceiver = new SpeakerBroadcastReceiver();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intents.ACTION_WAKE_UP);
        intentFilter.addAction(Intents.ACTION_RECOGNIZE);
        intentFilter.addAction(Intents.ACTION_WAKE_UP_START);
        intentFilter.addAction(Intents.ACTION_WAKE_UP_STOP);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mReceiver, intentFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (mReceiver != null && mReceiver.getWakeUpStatus() == IStatus.STATUS_NONE) {
            Log.d(TAG, "onStartCommand: 开启唤醒");
            mReceiver.startWakeUp(getApplicationContext());
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mReceiver.destroy();
        unregisterReceiver(mReceiver);

    }

}
