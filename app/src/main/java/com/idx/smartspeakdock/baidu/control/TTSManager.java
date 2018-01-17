package com.idx.smartspeakdock.baidu.control;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.idx.smartspeakdock.utils.AppExecutors;
import com.idx.smartspeakdock.utils.AuthInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by derik on 17-12-19.
 */

public class TTSManager {

    private static final String TAG = TTSManager.class.getName();
    private static TTSManager INSTANCE = null;
    private SpeechSynthesizer mSpeechSynthesizer;
    private SpeakCallback mCallback;
    private AppExecutors mAppExecutors;

    public interface SpeakCallback {
        void onSpeakStart();

        void onSpeakFinish();

        void onSpeakError();
    }

    private TTSManager() {
    }

    public static TTSManager getInstance() {
        if (INSTANCE == null) {
            synchronized (TTSManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TTSManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 语音合成管理器初始化
     *
     * @param context 上下文
     * @param ttsMode Tts模式
     */
    public void init(Context context, TtsMode ttsMode) {
        mAppExecutors = new AppExecutors();
        Map<String, Object> authParams = AuthInfo.getAuthParams(context);
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);
        mSpeechSynthesizer.setAppId((String) authParams.get(AuthInfo.META_APP_ID));
        mSpeechSynthesizer.setApiKey((String) authParams.get(AuthInfo.META_APP_KEY),
                (String) authParams.get(AuthInfo.META_APP_SECRET));
        //授权检验接口
        if (!checkAuth(ttsMode)) {
            return;
        }

        //设置事件监听器
        mSpeechSynthesizer.setSpeechSynthesizerListener(new TtsStatusListener());

        //设置合成参数，0女声，1男声
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        //初始化合成引擎
        mSpeechSynthesizer.initTts(ttsMode);

    }

    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth(TtsMode ttsMode) {
        com.baidu.tts.auth.AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d(TAG, "【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.d(TAG, "验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    /**
     * 播放单个语音时，暂时取消回调
     *
     * @param text 要播放的文本
     */
    public void speak(String text) {
        if (text == null || text.equals("")) {
            return;
        }
        if (mCallback != null) {
            mCallback = null;
        }
        mSpeechSynthesizer.speak(text);
    }

    /**
     * 播放单个语音时，暂时取消回调
     *
     * @param text 要播放的文本
     */
    public void speak(String text, SpeakCallback callback) {
        if (text == null || text.equals("")) {
            return;
        }
        if (mCallback != null) {
            mCallback = null;
        }
        mCallback = callback;
        mSpeechSynthesizer.speak(text);
    }

    /**
     * 播放多条语音
     *
     * @param list 语音文本包
     */
    public void batSpeak(List<SpeechSynthesizeBag> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        if (mCallback != null) {
            mCallback = null;
        }
        mSpeechSynthesizer.batchSpeak(list);
    }

    /**
     * 播放多条语音
     *
     * @param list     语音文本包
     * @param callback 语音回调函数
     */
    public void batSpeak(List<SpeechSynthesizeBag> list, SpeakCallback callback) {
        if (list == null || list.size() == 0) {
            return;
        }
        if (mCallback != null) {
            mCallback = null;
        }
        mCallback = callback;
        mSpeechSynthesizer.batchSpeak(list);
    }

    public int pause() {
        return mSpeechSynthesizer.pause();
    }

    public int resume() {
        return mSpeechSynthesizer.resume();
    }

    public int stop() {
        return mSpeechSynthesizer.stop();
    }

    public void release() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
        }
        if (mAppExecutors != null) {
            mAppExecutors = null;
        }
        if (mCallback != null) {
            mCallback = null;
        }
        if (INSTANCE != null) {
            INSTANCE = null;
        }
    }

    private class TtsStatusListener implements SpeechSynthesizerListener {
        private TtsStatusListener() {
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
            mAppExecutors.getMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onSpeakStart();
                    }
                }
            });
        }

        @Override
        public void onSpeechProgressChanged(String s, int i) {

        }

        @Override
        public void onSpeechFinish(String s) {
            mAppExecutors.getMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onSpeakFinish();
                    }
                }
            });
        }

        @Override
        public void onError(String s, SpeechError speechError) {
            mAppExecutors.getMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onSpeakError();
                    }
                }
            });
        }
    }
}
