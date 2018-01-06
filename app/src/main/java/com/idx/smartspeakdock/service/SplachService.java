package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.idx.smartspeakdock.standby.StandByActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplachService extends Service {
    private static final String TAG = "SplachService";
    private final int EVENT_LOCK_WINDOW = 0x100;
    private Handler mHandler;
    private Timer mTimer;
    private MyTimerTask mTimerTask;
    public SplachService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        mTimer = new Timer(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        mHandler = new Handler(){
            public void handleMessage(Message message){
                Log.i(TAG, "message what = " + message.what);
                if (message.what == 0x100){
                    lockWindow();
                }
            }
        };

        StartLockWindowTimer();

        return super.onStartCommand(intent, flags, startId);
    }

    public void lockWindow(){
        Log.d(TAG, "lockWindow: ");
        Intent intent = new Intent();
        intent.setClass(SplachService.this,StandByActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.putExtra(GlobalUtils.RECONGINIZE_WHICH_FRAGMENT,GlobalUtils.STANDBY_FRAGMENT_INTENT_ID);
        startActivity(intent);
    }

    public void StartLockWindowTimer(){
        if (mTimer != null){
            if (mTimerTask != null){
                mTimerTask.cancel();  //将原任务从队列中移除
            }
            mTimerTask = new MyTimerTask();  // 新建一个任务
            mTimer.schedule(mTimerTask, 600000);
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.i(TAG, "run...");
            Message msg = mHandler.obtainMessage(EVENT_LOCK_WINDOW);
            msg.sendToTarget();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        Log.d(TAG, "onDestroy: 取消timer");
    }
}
