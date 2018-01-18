package com.idx.smartspeakdock.baidu.control;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizeBag;
import com.idx.smartspeakdock.baidu.unit.APIService;
import com.idx.smartspeakdock.baidu.unit.exception.UnitError;
import com.idx.smartspeakdock.baidu.unit.listener.ICalenderVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMapVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.ISessionListener;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IVoiceActionListener;
import com.idx.smartspeakdock.baidu.unit.listener.IWeatherVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.OnResultListener;
import com.idx.smartspeakdock.baidu.unit.listener.VoiceActionAdapter;
import com.idx.smartspeakdock.baidu.unit.model.AccessToken;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.utils.AuthInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by derik on 17-12-19.
 */

public class UnitManager {

    private static final String TAG = UnitManager.class.getName();
    private static UnitManager INSTANCE = null;
    //每次会话Id
    private String sessionId;
    //SmartSpeakDock的场景ID
    private int sceneId = 15213;
    private String accessToken;
    //会话标识
    private boolean enableSession = false;
    //退出voice标识
    private boolean isOver = false;

    private TTSManager ttsManager;
    private APIService mApiService;
    //语音响应处理适配
    private VoiceActionAdapter mVoiceAdapter;
    private ISessionListener mSessionListener;

    private UnitManager(Context context) {
        init(context);
    }

    public static UnitManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UnitManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UnitManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     **/
    public void init(Context context) {
        Map<String, Object> authParams = AuthInfo.getAuthParams(context);
        mApiService = APIService.getInstance();
        mApiService.init(context);
        mApiService.initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                accessToken = result.getAccessToken();
                Log.d(TAG, "get token: " + accessToken);
            }

            @Override
            public void onError(UnitError error) {
                Log.d(TAG, "onError: ");
            }
        }, (String) authParams.get(AuthInfo.META_APP_KEY), (String) authParams.get(AuthInfo.META_APP_SECRET));
        mVoiceAdapter = new VoiceActionAdapter(context);
        ttsManager = TTSManager.getInstance();
    }

    /**
     * 发送数据至语音云端处理接口
     *
     * @param message  语音转化后的文本信息
     */
    public void sendMessage(String message) {
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        Log.d(TAG, "send to unit: " + message);
        mApiService.communicate(new OnResultListener<CommunicateResponse>() {
            @Override
            public void onResult(CommunicateResponse result) {
                handleResponse(result);

            }

            @Override
            public void onError(UnitError error) {
                if (mSessionListener != null) {
                    mSessionListener.onSessionError();
                }

            }
        }, sceneId, message, sessionId);

    }

    /**
     * 处理数据，播放语音，调用动作执行函数
     *
     * @param result   解析后的数据
     **/
    private void handleResponse(CommunicateResponse result) {
        if (result != null) {
            sessionId = result.sessionId;
            //  如果有对于的动作action，请执行相应的逻辑
            List<CommunicateResponse.Action> actionList = result.actionList;
            if (actionList.size() > 1) {
                //意图引导
                List<SpeechSynthesizeBag> bags = new ArrayList<>();
                for (CommunicateResponse.Action action : actionList) {

                    if (!TextUtils.isEmpty(action.say)) {
                        Log.d(TAG, "handleResponse: StringBuilder " + action.say);
                        SpeechSynthesizeBag msg = new SpeechSynthesizeBag();
                        msg.setText(action.say);
                        bags.add(msg);
                        for (String hintText : action.hintList) {
                            SpeechSynthesizeBag hint = new SpeechSynthesizeBag();
                            hint.setText(hintText);
                            bags.add(hint);
                        }

                    }
                }
                ttsManager.batSpeak(bags);

            } else if (actionList.size() == 1) {
                //单一意图，意图引导最后出口亦是单一意图
                final CommunicateResponse.Action action = actionList.get(0);
                final CommunicateResponse.Schema schema = result.schema;

                if (!TextUtils.isEmpty(action.say)) {
                    //有语音文本信息返回
                    List<SpeechSynthesizeBag> bags = new ArrayList<>();
                    SpeechSynthesizeBag msg = new SpeechSynthesizeBag();
                    msg.setText(action.say);
                    bags.add(msg);
                    for (String hintText : action.hintList) {
                        SpeechSynthesizeBag hint = new SpeechSynthesizeBag();
                        hint.setText(hintText);
                        bags.add(hint);
                    }

                    //播放Unit返回的语音包
                    ttsManager.batSpeak(bags, new TTSManager.SpeakCallback() {
                        @Override
                        public void onSpeakStart() {

                        }

                        @Override
                        public void onSpeakFinish() {
                            //执行自己的业务逻辑，回调执行，即语音播放结束后回调
                            Log.d(TAG, "onSpeakFinish: " + Thread.currentThread().getId());
                            executeTask(action, schema);
                        }

                        @Override
                        public void onSpeakError() {

                        }
                    });

                } else if (!TextUtils.isEmpty(action.mainExe)) {
                    //没有语音信息返回
                    Log.d(TAG, "handleResponse: mainExe, " + action.mainExe);
                    //执行自己的业务逻辑
                    executeTask(action, schema);
                }

            }
        }
    }

    /**
     * @param action   执行的动作信息
     * @param schema   对应的词槽
     */
    private void executeTask(final CommunicateResponse.Action action, final CommunicateResponse.Schema schema) {
        if (mVoiceAdapter != null) {
            Log.i(TAG, "run: action.id = " + action.actionId);
            isOver = mVoiceAdapter.action(action, schema, new IVoiceActionListener.IActionCallback() {
                @Override
                public void onResult(boolean sayBye) {
                    if (enableSession) {
                        if (sayBye){
                            //询问是否关闭会话
                            TTSManager.getInstance().speak("您还有什么吩咐吗，没有请说“再见!”", new TTSManager.SpeakCallback() {
                                @Override
                                public void onSpeakStart() {
                                }

                                @Override
                                public void onSpeakFinish() {
                                    if (mSessionListener != null) {
                                        if (enableSession) {
                                            mSessionListener.onRegContinue();
                                        }
                                    }
                                }

                                @Override
                                public void onSpeakError() {

                                }
                            });
                        } else {
                            if (mSessionListener != null) {
                                mSessionListener.onSessionFinish();
                            }
                        }

                    } else {
                        if (mSessionListener != null) {
                            mSessionListener.onSessionFinish();
                        }
                    }
                }
            });

            if (!isOver) {
                //如未结束，继续识别
                if (mSessionListener != null) {
                    mSessionListener.onRegContinue();
                }
            }
        }
    }

    /**
     * @param listener 音乐语音监听器
     */
    public void setMusicVoiceListener(IMusicVoiceListener listener) {
        Log.d(TAG, "setMusicVoiceListener: ");
        mVoiceAdapter.setMusicListener(listener);

    }

    /**
     * @param listener 日历语音监听器
     */
    public void setCalenderVoiceListener(ICalenderVoiceListener listener) {
        mVoiceAdapter.setCalenderListener(listener);
    }

    /**
     * @param listener 地图语音监听器
     */
    public void setMapVoiceListener(IMapVoiceListener listener) {
        mVoiceAdapter.setMapListener(listener);
    }

    /**
     * @param listener 天气语音监听器
     */
    public void setWeatherVoiceListener(IWeatherVoiceListener listener) {
        mVoiceAdapter.setWeatherListener(listener);
    }

    /**
     * @param listener 购物语音监听器
     */
    public void setShoppingVoiceListener(IShoppingVoiceListener listener) {
        Log.i(TAG, "setShoppingVoiceListener: ");
        if (listener != null) {
            mVoiceAdapter.setShoppingListener(listener);
        }
    }

    public void setSessionListener(ISessionListener sessionListener){
        mSessionListener = sessionListener;
        mVoiceAdapter.setSessionListener(sessionListener);
    }

    public void enableSession(boolean sessionOver) {
        enableSession = sessionOver;
    }

    public void release() {
        if (ttsManager != null) {
            ttsManager.release();
        }

        if (INSTANCE != null) {
            INSTANCE = null;
        }

        if (mApiService != null) {
            mApiService = null;
        }

    }
}
