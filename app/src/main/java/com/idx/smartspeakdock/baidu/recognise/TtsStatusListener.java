package com.idx.smartspeakdock.baidu.recognise;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.idx.smartspeakdock.Intents;
import com.idx.smartspeakdock.utils.AppExecutors;

/**
 * Created by derik on 17-12-20.
 */

public class TtsStatusListener implements SpeechSynthesizerListener {

    private String TAG = TtsStatusListener.class.getName();
    private Context mContext;

    private AppExecutors appExecutors = new AppExecutors();
    private int delayTime = 1200;
    private DelayRunnable delayRunnable = null;


    public TtsStatusListener(Context context) {
        mContext = context;
        delayRunnable = new DelayRunnable(delayTime);
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        appExecutors.getMainThread().execute(delayRunnable);
    }

    @Override
    public void onError(String s, SpeechError speechError) {

    }

    public class DelayRunnable implements Runnable {
        private int time;

        public DelayRunnable(int time) {
            this.time = time;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "run: sleep now");
                Thread.sleep(time);
                mContext.sendBroadcast(new Intent(Intents.ACTION_RECOGNIZE));
                Log.d(TAG, "run: send now");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
