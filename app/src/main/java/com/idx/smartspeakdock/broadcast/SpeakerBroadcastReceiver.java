package com.idx.smartspeakdock.broadcast;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.client.TtsMode;
import com.idx.smartspeakdock.Intents;
import com.idx.smartspeakdock.baidu.control.RecognizerManager;
import com.idx.smartspeakdock.baidu.control.TTSManager;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.control.WakeUpManager;
import com.idx.smartspeakdock.baidu.recognise.IStatus;
import com.idx.smartspeakdock.baidu.recognise.MessageStatusRecogListener;
import com.idx.smartspeakdock.baidu.recognise.PidBuilder;
import com.idx.smartspeakdock.baidu.recognise.StatusRecogListener;
import com.idx.smartspeakdock.baidu.recognise.WakeupParams;
import com.idx.smartspeakdock.baidu.unit.SpeakDialog;
import com.idx.smartspeakdock.baidu.wakeup.SimpleWakeupListener;
import com.idx.smartspeakdock.utils.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by derik on 17-12-15.
 */

public class SpeakerBroadcastReceiver extends BroadcastReceiver implements IStatus {

    private static final String TAG = SpeakerBroadcastReceiver.class.getName();
    /**
     * 屏锁释放等待时长
     */
    private static final int RELEASE_LOCK_DELAY = 5000; //ms
    /**
     * 屏幕解锁超时时长
     */
    private static final int WAKE_LOCK_TIMEOUT = 8000; //ms
    /**
     * 唤醒后，识别回溯时长
     */
    private static final int BACK_TRACK = 1500; //ms
    /**
     * 唤醒管理器
     */
    private WakeUpManager mWakeUpManager = null;
    /**
     * 唤醒参数
     */
    private WakeupParams mWakeupParams = null;
    /**
     * 识别管理器
     */
    private RecognizerManager mRecognizerManager = null;

//    private PowerManager.WakeLock mWL = null;
//    private KeyguardManager.KeyguardLock mKL = null;
//    private boolean isLocked = false;

    private int wakeUpStatus = STATUS_NONE;

    private Handler handler = new Handler();
    private Runnable mRecogRunnable = new Runnable() {
        @Override
        public void run() {
            startRecognize();
        }
    };
    private SpeakDialog speakDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Logger.info(TAG, "onReceive: " + action);
            switch (intent.getAction()) {
//                case Intent.ACTION_SCREEN_OFF:
//                    isLocked = true;
//                    break;
//                case Intent.ACTION_SCREEN_ON:
//                    isLocked = false;
//                    break;
//                case Intent.ACTION_USER_PRESENT:
//                    releaseWLKL();
//                    break;
                //唤醒后，即表示开始会话
                case Intents.ACTION_WAKE_UP:
                case Intents.ACTION_SESSION_START:
                    //开启会话
                    UnitManager.getInstance().setSessionOver(false);
//                    if (isLocked) {
//                        unlock(context);
//                    }
                    if (speakDialog == null) {
                        speakDialog = new SpeakDialog(context);
                    }
                case Intents.ACTION_RECOGNIZE_START:
                    if (!UnitManager.getInstance().isSessionOver()) {
                        if (speakDialog != null) {
                            speakDialog.showSpeaking();
                        }
                        handler.postDelayed(mRecogRunnable, BACK_TRACK);
                    }
                    break;
                case Intents.ACTION_RECOGNIZE_END:
                    if (speakDialog != null) {
                        speakDialog.showReady();
                    }
                    break;
                case Intents.ACTION_SESSION_END:
                    //会话结束
                    UnitManager.getInstance().setSessionOver(true);
                    if (speakDialog != null) {
                        speakDialog.dismiss();
                        speakDialog = null;
                    }
                    break;
                case Intents.ACTION_WAKE_UP_START:
                    startWakeUp(context);
                    break;
                case Intents.ACTION_WAKE_UP_STOP:
                    stopWakeUp();
                    stopRecognize();
                    break;
                default:
            }
        }
    }

    public int getWakeUpStatus() {
        return wakeUpStatus;
    }

    public void startWakeUp(Context context) {
        if (wakeUpStatus == STATUS_NONE) {
            Logger.info("MainFragment", "startWakeUp: ");
            initRecog(context);
            Map<String, Object> params = mWakeupParams.fetch();
            mWakeUpManager.start(params);
            wakeUpStatus = STATUS_READY;
        }

    }

    public void stopWakeUp() {
        if (wakeUpStatus == STATUS_READY) {
            Logger.info("MainFragment", "stopWakeUp: ");
            mWakeUpManager.stop();
            mWakeUpManager.release();
            mWakeUpManager = null;
            wakeUpStatus = STATUS_NONE;
        }

    }

    private void initRecog(final Context context) {
        //初始化语音唤醒
        SimpleWakeupListener listener = new SimpleWakeupListener(context);
        mWakeUpManager = new WakeUpManager(context, listener);
        mWakeupParams = new WakeupParams(context);

        //初始化语音识别
        StatusRecogListener recogListener = new MessageStatusRecogListener(context);
        mRecognizerManager = new RecognizerManager(context, recogListener);

        //初始化语音合成
        TTSManager.getInstance().init(context, TtsMode.ONLINE);

        //初始化Unit交互
        UnitManager.getInstance().init(context);

    }

    private void startRecognize() {
        // 此处 开始正常识别流程
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        int pid = PidBuilder.create().model(PidBuilder.INPUT).toPId(); //如识别短句，不需要需要逗号，将PidBuilder.INPUT改为搜索模型PidBuilder.SEARCH
        params.put(SpeechConstant.PID, pid);
        if (BACK_TRACK > 0) { // 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
            params.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - BACK_TRACK);

        }
        mRecognizerManager.cancel();
        mRecognizerManager.start(params);

    }

    private void stopRecognize() {
        mRecognizerManager.stop();
        mRecognizerManager.release();

    }

//    private void unlock(Context context) {
//        Log.d(TAG, "unlock: ");
//        //屏幕唤醒
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        //最后的参数是LogCat里用的Tag
//        mWL = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
//                | PowerManager.SCREEN_DIM_WAKE_LOCK, "StartupReceiver");
//        mWL.acquire(WAKE_LOCK_TIMEOUT);
//
//        //屏幕解锁
//        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        //参数是LogCat里用的Tag
//        mKL = km.newKeyguardLock("StartupReceiver");
//        mKL.disableKeyguard();
//    }

//    private void releaseWLKL() {
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (mWL != null) {
//                    mWL.release();
//                    mWL = null;
//                }
//
//                if (mKL != null) {
//                    mKL = null;
//                }
//            }
//        }, RELEASE_LOCK_DELAY);
//
//    }

    public void destroy() {
        if (mWakeUpManager != null) {
            mWakeUpManager.stop();
            mWakeUpManager.release();
            mWakeUpManager = null;
        }

        if (mRecognizerManager != null) {
            mRecognizerManager.stop();
            mRecognizerManager.release();
            mRecognizerManager = null;
        }

        TTSManager.getInstance().release();
        UnitManager.getInstance().release();
    }

}
