package com.idx.smartspeakdock.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.client.TtsMode;
import com.idx.smartspeakdock.R;
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
import com.idx.smartspeakdock.utils.MathTool;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by derik on 17-12-15.
 */

public class SpeakerService extends Service implements IStatus {

    private static final String TAG = SpeakerService.class.getName();
    /**
     * 每次唤醒常量
     */
    private static final int CONSTANT_WAKE_UP = 0x001;
    /**
     * 唤醒服务开启常量
     */
    private static final int CONSTANT_WAKE_UP_START = 0x002;
    /**
     * 唤醒服务停止常量
     */
    private static final int CONSTANT_WAKE_UP_STOP = 0x003;
    /**
     * 开始识别常量
     */
    private static final int CONSTANT_RECOGNIZE_START = 0x101;
    /**
     * 识别结束常量
     */
    private static final int CONSTANT_RECOGNIZE_FINISH = 0x102;
    /**
     * 识别错误常量
     */
    private static final int CONSTANT_RECOGNIZE_ERROR = 0x103;
    /**
     * 会话开始常量
     */
    private static final int CONSTANT_SESSION_START = 0x201;
    /**
     * 会话结束常量
     */
    private static final int CONSTANT_SESSION_FINISH = 0x202;
    /**
     * 会话错误常量
     */
    private static final int CONSTANT_SESSION_ERROR = 0x203;
    /**
     * 超时时长常量
     */
    private static final int CONSTANT_TIME_STEP = 15000; //15s
    /**
     * 超时消息常量
     */
    private static final int CONSTANT_TIME_TICK = 0x301;
    /**
     * 相邻唤醒间隔时长
     */
    private static final int CONSTANT_WAKE_UP_SPACE = 5000; //5s
    /**
     * 唤醒后，识别回溯时长
     */
    private static final int BACK_TRACK = 1000; //1s
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
    /**
     * 唤醒服务状态标识
     */
    private int wakeUpStatus = STATUS_NONE;
    /**
     * 语音交互声波纹
     */
    private SpeakDialog mSpeakDialog = null;
    /**
     * 唤醒状态
     */
    private boolean isWaked = false;
    /**
     * 消息接收状态，定时置false，接收到消息置为true
     */
    private boolean isReceived = false;
    private Handler mHandler = null;
    /**
     * 超时定时器
     */
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHandler != null) {
                mHandler.postDelayed(timerRunnable, CONSTANT_TIME_STEP);
                if (!isReceived) {
                    mHandler.sendEmptyMessage(CONSTANT_TIME_TICK);
                }
                isReceived = false;
            }
        }
    };
    /**
     * 数据部分为本地化，若迁移至伺服器，可联网获取，动态变更
     * 再见语音数据
     */
    private String[] mVoiceArrayBye;
    /**
     * 欢迎语音数据
     */
    private String[] mVoiceArrayWel;
    /**
     * 抱歉语音数据
     */
    private String[] mVoiceArraySorry;
    /**
     * 重复语音数据
     */
    private String[] mVoiceRepeat;

    private static class VoiceHandler extends Handler {
        WeakReference<SpeakerService> weakReference;
        private long startTime;

        private VoiceHandler(SpeakerService service) {
            weakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final SpeakerService service = weakReference.get();
            if (service == null || service.mHandler == null) {
                return;
            }
            service.isReceived = true;
            switch (msg.what) {
                case CONSTANT_WAKE_UP:
                case CONSTANT_SESSION_START:
                    // 进入口为wake up，定义CONSTANT_SESSION_START，只为便于将session和wake up区分理解
                    // 语音唤醒后，开启会话，创建会话窗口
                    if (service.isWaked) {
                        long timeNow = System.currentTimeMillis();
                        if (timeNow - startTime < CONSTANT_WAKE_UP_SPACE) {
                            return;
                        }
                    }
                    service.isWaked = true;
                    startTime = System.currentTimeMillis();
                    service.mHandler.removeCallbacks(service.timerRunnable);
                    //开始计时
                    service.mHandler.post(service.timerRunnable);

                    UnitManager.getInstance(service.getBaseContext()).enableSession(true);
                    if (service.mSpeakDialog == null) {
                        service.mSpeakDialog = new SpeakDialog(service.getBaseContext());
                    }
                    service.mSpeakDialog.showReady();
                    String voiceWel = service.mVoiceArrayWel[MathTool.randomValue(service.mVoiceArrayWel.length)];
                    Log.d(TAG, "welcome voice: " + voiceWel);
                    TTSManager.getInstance().speak(voiceWel, new TTSManager.SpeakCallback() {
                        @Override
                        public void onSpeakStart() {

                        }

                        @Override
                        public void onSpeakFinish() {
                            service.mHandler.sendEmptyMessageDelayed(CONSTANT_RECOGNIZE_START, BACK_TRACK);
                        }

                        @Override
                        public void onSpeakError() {

                        }
                    });
                    break;
                case CONSTANT_RECOGNIZE_START:
                    //显示会话窗口，并开始识别，需回溯
                    if (service.mSpeakDialog != null) {
                        service.mSpeakDialog.showSpeaking();
                        service.startRecognize();
                    }

                    break;
                case CONSTANT_RECOGNIZE_FINISH:
                    if (service.mSpeakDialog != null) {
                        service.mSpeakDialog.showReady();
                        service.stopRecognize();
                    }
                    break;
                case CONSTANT_TIME_TICK:
                    //查询超时了
                    String voiceSorry = service.mVoiceArraySorry[MathTool.randomValue(service.mVoiceArraySorry.length)];
                    TTSManager.getInstance().speak(voiceSorry);
                case CONSTANT_RECOGNIZE_ERROR:
                case CONSTANT_SESSION_ERROR:
                case CONSTANT_SESSION_FINISH:
                    UnitManager.getInstance(service.getBaseContext()).enableSession(false);
                    if (service.mSpeakDialog != null) {
                        service.mSpeakDialog.dismiss();
                        service.mSpeakDialog = null;
                        service.stopRecognize();
                    }
                    service.isWaked = false;
                    service.mHandler.removeCallbacks(service.timerRunnable);
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
        initData();

    }

    private void initData() {
        mVoiceArrayBye = getResources().getStringArray(R.array.voice_bye);
        mVoiceArrayWel = getResources().getStringArray(R.array.voice_welcome);
        mVoiceArraySorry = getResources().getStringArray(R.array.voice_sorry);
        mVoiceRepeat = getResources().getStringArray(R.array.voice_repeat);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (wakeUpStatus == IStatus.STATUS_NONE) {
            Log.d(TAG, "onStartCommand: 开启唤醒");
            sendEmptyMsg(CONSTANT_WAKE_UP_START);
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

    /**
     * 初始化
     */
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

    private void stopRecognize() {
        mRecognizerManager.cancel();
        mRecognizerManager.stop();

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

        if (mSpeakDialog != null) {
            mSpeakDialog.dismiss();
            mSpeakDialog = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(timerRunnable);
            mHandler = null;
        }

        TTSManager.getInstance().release();
        UnitManager.getInstance(getBaseContext()).release();

    }

    /**
     * 唤醒事件监听器
     */
    public class SimpleWakeupListener implements IWakeupListener {
        @Override
        public void onSuccess(String word, WakeUpResult result) {
            //通知已经唤醒, 回溯1.5s
            sendEmptyMsg(CONSTANT_WAKE_UP);

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

    /**
     * 识别过程监听器
     */
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
            int length = results[0].length() - 1;
            String msg = results[0].substring(0, length);

            if (msg.equals("")) {
                TTSManager.getInstance().speak(mVoiceRepeat[MathTool.randomValue(mVoiceRepeat.length)], new TTSManager.SpeakCallback() {
                    @Override
                    public void onSpeakStart() {

                    }

                    @Override
                    public void onSpeakFinish() {
                        sendEmptyMsg(CONSTANT_RECOGNIZE_START, BACK_TRACK);

                    }

                    @Override
                    public void onSpeakError() {

                    }
                });
                return;
            } else {
                sendEmptyMsg(CONSTANT_RECOGNIZE_FINISH);
                //将识别后的语句交由Unit处理
                UnitManager.getInstance(getBaseContext()).sendMessage(msg);
            }

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
            String voice = mVoiceArrayBye[MathTool.randomValue(mVoiceArrayBye.length)];
            TTSManager.getInstance().speak(voice, new TTSManager.SpeakCallback() {
                @Override
                public void onSpeakStart() {

                }

                @Override
                public void onSpeakFinish() {
                    //超时交互，自动结束会话
                    sendEmptyMsg(CONSTANT_RECOGNIZE_ERROR);

                }

                @Override
                public void onSpeakError() {

                }
            });

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

    /**
     * 会话状态监听器
     */
    public class SessionListener implements ISessionListener {
        @Override
        public void onSessionFinish() {
            sendEmptyMsg(CONSTANT_SESSION_FINISH);

        }

        @Override
        public void onRegContinue() {
            sendEmptyMsg(CONSTANT_RECOGNIZE_START, BACK_TRACK);

        }

        @Override
        public void onSessionError() {
            //Unit Error，自动结束会话
            sendEmptyMsg(CONSTANT_SESSION_ERROR);

        }
    }

    private void sendEmptyMsg(int what, int timeDelay) {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(what, timeDelay);
        }
    }

    private void sendEmptyMsg(int what) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(what);
        }
    }

}
