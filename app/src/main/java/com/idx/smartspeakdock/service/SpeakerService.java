package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.client.TtsMode;
import com.idx.smartspeakdock.baidu.control.RecognizerManager;
import com.idx.smartspeakdock.baidu.control.TTSManager;
import com.idx.smartspeakdock.baidu.control.UnitManager;
import com.idx.smartspeakdock.baidu.control.WakeUpManager;
import com.idx.smartspeakdock.baidu.recognise.IStatus;
import com.idx.smartspeakdock.baidu.recognise.PidBuilder;
import com.idx.smartspeakdock.baidu.recognise.RecogResult;
import com.idx.smartspeakdock.baidu.recognise.StatusRecogListener;
import com.idx.smartspeakdock.baidu.unit.SpeakDialog;
import com.idx.smartspeakdock.baidu.unit.listener.ISessionListener;
import com.idx.smartspeakdock.baidu.wakeup.IWakeupListener;
import com.idx.smartspeakdock.baidu.wakeup.WakeUpResult;
import com.idx.smartspeakdock.baidu.wakeup.WakeupParams;
import com.idx.smartspeakdock.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by derik on 17-12-15.
 */

public class SpeakerService extends Service implements IStatus {

    private static final String TAG = SpeakerService.class.getName();
    //唤醒常量
    private static final int CONSTANT_WAKE_UP = 0x001;
    private static final int CONSTANT_WAKE_UP_START = 0x002;
    private static final int CONSTANT_WAKE_UP_STOP = 0x003;
    //识别常量
    private static final int CONSTANT_RECOGNIZE_START = 0x101;
    private static final int CONSTANT_RECOGNIZE_FINISH = 0x102;
    private static final int CONSTANT_RECOGNIZE_ERROR = 0x103;
    //会话常量
    private static final int CONSTANT_SESSION_START = 0x201;
    private static final int CONSTANT_SESSION_FINISH = 0x202;
    private static final int CONSTANT_SESSION_ERROR = 0x203;

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

    private int wakeUpStatus = STATUS_NONE;

    private Handler mHandler = null;
    private SpeakDialog speakDialog = null;
    private boolean isWaked = false;

    private static class VoiceHandler extends Handler {
        WeakReference<SpeakerService> weakReference;

        private VoiceHandler(SpeakerService service) {
            weakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final SpeakerService service = weakReference.get();

            if (service == null) {
                return;
            }

            switch (msg.what) {
                case CONSTANT_WAKE_UP:
                    if (service.isWaked) {
                        return;
                    } else {
                        service.isWaked = true;
                    }
                case CONSTANT_SESSION_START:
                    //语音唤醒后，开启会话，创建会话窗口
                    UnitManager.getInstance(service.getBaseContext()).enableSession(true);
                    if (service.speakDialog == null) {
                        service.speakDialog = new SpeakDialog(service.getBaseContext());
                    }
                case CONSTANT_RECOGNIZE_START:
                    //显示会话窗口，并开始识别，需回溯
                    if (service.speakDialog != null) {
                        service.speakDialog.showSpeaking();
                    }
                    service.startRecognize();
                    break;
                case CONSTANT_RECOGNIZE_FINISH:
                    if (service.speakDialog != null) {
                        service.speakDialog.showReady();
                    }
                    break;
                case CONSTANT_RECOGNIZE_ERROR:
                    //出错结束会话
                case CONSTANT_SESSION_ERROR:
                    //出错结束会话
                case CONSTANT_SESSION_FINISH:
                    //口令结束会话
                    UnitManager.getInstance(service.getBaseContext()).enableSession(false);
                    if (service.speakDialog != null) {
                        service.speakDialog.dismiss();
                        service.speakDialog = null;
                    }
                    service.isWaked = false;
                    break;
                case CONSTANT_WAKE_UP_START:
                    service.startWakeUp();
                    break;
                case CONSTANT_WAKE_UP_STOP:
                    service.stopWakeUp();
                    service.stopRecognize();
                    break;
                default:
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: " + Thread.currentThread().getId());
        super.onCreate();
        mHandler = new VoiceHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (wakeUpStatus == IStatus.STATUS_NONE) {
            Log.d(TAG, "onStartCommand: 开启唤醒");
            mHandler.sendEmptyMessage(CONSTANT_WAKE_UP_START);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开启唤醒服务
     */
    public void startWakeUp() {
        if (wakeUpStatus == STATUS_NONE) {
            Log.d(TAG, "startWakeUp: ");
            initRecog();
            Map<String, Object> params = mWakeupParams.fetch();
            mWakeUpManager.start(params);
            wakeUpStatus = STATUS_READY;
        }

    }

    /**
     * 停止唤醒服务
     */
    public void stopWakeUp() {
        if (wakeUpStatus == STATUS_READY) {
            Log.d(TAG, "stopWakeUp: ");
            mWakeUpManager.stop();
            mWakeUpManager.release();
            mWakeUpManager = null;
            wakeUpStatus = STATUS_NONE;
        }

    }

    private void initRecog() {
        //初始化语音唤醒
        mWakeUpManager = new WakeUpManager(getBaseContext(), new SimpleWakeupListener());
        mWakeupParams = new WakeupParams(getBaseContext());

        //初始化语音识别
        mRecognizerManager = new RecognizerManager(getBaseContext(), new MessageStatusRecogListener());

        //初始化语音合成
        TTSManager.getInstance().init(getBaseContext(), TtsMode.ONLINE);

        //初始化Unit交互
        UnitManager.getInstance(getBaseContext()).setSessionListener(new SessionListener());

    }

    //百度Demo原函数
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

    //百度Demo原函数
    private void stopRecognize() {
        mRecognizerManager.stop();
        mRecognizerManager.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
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
        UnitManager.getInstance(getBaseContext()).release();

    }

    public class SimpleWakeupListener implements IWakeupListener {
        @Override
        public void onSuccess(String word, WakeUpResult result) {
            if (mHandler != null) {
                //通知已经唤醒, 回溯1.5s
                mHandler.sendEmptyMessageDelayed(CONSTANT_WAKE_UP, BACK_TRACK);
            }
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

//    @Override
//    public void onASrAudio(byte[] data, int offset, int length) {
//        Logger.error(TAG, "audio data： " + data.length);
//    }

    }

    //百度Demo引用类
    public class MessageStatusRecogListener extends StatusRecogListener {

        private long speechEndTime;

        private boolean needTime = true;

        private MessageStatusRecogListener() {
        }

        @Override
        public void onAsrReady() {
            super.onAsrReady();
            sendStatusMessage("引擎就绪，可以开始说话。");
        }

        @Override
        public void onAsrBegin() {
            super.onAsrBegin();
            sendStatusMessage("检测到用户说话");
        }

        @Override
        public void onAsrEnd() {
            super.onAsrEnd();
            speechEndTime = System.currentTimeMillis();
            sendMessage("检测到用户说话结束");
        }

        @Override
        public void onAsrPartialResult(String[] results, RecogResult recogResult) {
            sendStatusMessage("临时识别结果，结果是“" + results[0] + "”；原始json：" + recogResult.getOrigalJson());
            super.onAsrPartialResult(results, recogResult);
        }

        @Override
        public void onAsrFinalResult(String[] results, RecogResult recogResult) {
            super.onAsrFinalResult(results, recogResult);
            Log.d("onAsrFinalResult", "onAsrFinalResult: ");
            if (mHandler != null) {
                mHandler.sendEmptyMessage(SpeakerService.CONSTANT_RECOGNIZE_FINISH);
            }
            int length = results[0].length() - 1;
            String msg = results[0].substring(0, length);
            //将识别后的语句交由Unit处理
            UnitManager.getInstance(getBaseContext()).sendMessage(msg);

            //调试使用
            String message = "识别结束，结果是“" + msg + "”";
            sendStatusMessage(message + "“；原始json：" + recogResult.getOrigalJson());
            if (speechEndTime > 0) {
                long diffTime = System.currentTimeMillis() - speechEndTime;
                message += "；说话结束到识别结束耗时【" + diffTime + "ms】";

            }
            speechEndTime = 0;
            sendMessage(message, status, true);
        }

        @Override
        public void onAsrFinishError(int errorCode, int subErrorCode, String errorMessage, String descMessage, RecogResult recogResult) {
            super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult);
            TTSManager.getInstance().speak("您好像没有什么事，下次再见！");
            //超时交互，自动结束会话
            if (mHandler != null) {
                mHandler.sendEmptyMessage(CONSTANT_RECOGNIZE_ERROR);
            }
            String message = "识别错误, 错误码：" + errorCode + "," + subErrorCode;
            sendStatusMessage(message + "；错误消息:" + errorMessage + "；描述信息：" + descMessage);
            if (speechEndTime > 0) {
                long diffTime = System.currentTimeMillis() - speechEndTime;
                message += "。说话结束到识别结束耗时【" + diffTime + "ms】";
            }
            speechEndTime = 0;
            sendMessage(message, status, true);
            speechEndTime = 0;

        }

        @Override
        public void onAsrOnlineNluResult(String nluResult) {
            super.onAsrOnlineNluResult(nluResult);
            if (!nluResult.isEmpty()) {
                sendStatusMessage("原始语义识别结果json：" + nluResult);
            }

        }

        @Override
        public void onAsrFinish(RecogResult recogResult) {
            super.onAsrFinish(recogResult);
            sendStatusMessage("识别一段话结束。如果是长语音的情况会继续识别下段话。");

        }

        /**
         * 长语音识别结束
         */
        @Override
        public void onAsrLongFinish() {
            super.onAsrLongFinish();
            sendStatusMessage("长语音识别结束。");

        }


        /**
         * 使用离线命令词时，有该回调说明离线语法资源加载成功
         */
        @Override
        public void onOfflineLoaded() {
            sendStatusMessage("【重要】离线资源加载成功。没有此回调可能离线语法功能不能使用。");
        }

        /**
         * 使用离线命令词时，有该回调说明离线语法资源加载成功
         */
        @Override
        public void onOfflineUnLoaded() {
            sendStatusMessage(" 离线资源卸载成功。");
        }

        @Override
        public void onAsrExit() {
            super.onAsrExit();
            sendStatusMessage("识别引擎结束并空闲中");

        }

        private void sendMessage(String message) {
            sendMessage(message, WHAT_MESSAGE_STATUS);
        }

        private void sendStatusMessage(String message) {
            sendMessage(message, status);
        }

        private void sendMessage(String message, int what) {
            sendMessage(message, what, false);
        }


        private void sendMessage(String message, int what, boolean highlight) {
            if (needTime && what != STATUS_FINISHED) {
                message += "  ;time=" + System.currentTimeMillis();
            }
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = status;
            if (highlight) {
                msg.arg2 = 1;
            }
            msg.obj = message + "\n";
            Log.d("message", "sendMessage: " + message);
        }
    }

    public class SessionListener implements ISessionListener {
        @Override
        public void onSessionFinish() {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(CONSTANT_SESSION_FINISH);
            }
        }

        @Override
        public void onRegContinue() {
            if (mHandler != null) {
                mHandler.sendEmptyMessageDelayed(CONSTANT_RECOGNIZE_START, BACK_TRACK);
            }
        }

        @Override
        public void onSessionError() {
            //Unit Error，自动结束会话
            if (mHandler != null) {
                mHandler.sendEmptyMessage(CONSTANT_SESSION_ERROR);
            }
        }
    }

}
