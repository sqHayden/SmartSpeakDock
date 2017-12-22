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
    private Context mContext;

    public TtsStatusListener(Context context) {
        mContext = context;
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
        mContext.sendBroadcast(new Intent(Intents.ACTION_RECOGNIZE));
    }

    @Override
    public void onError(String s, SpeechError speechError) {

    }

}
