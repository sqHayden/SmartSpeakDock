package com.idx.smartspeakdock.baidu.control;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizeBag;
import com.idx.smartspeakdock.Intents;
import com.idx.smartspeakdock.baidu.unit.APIService;
import com.idx.smartspeakdock.baidu.unit.exception.UnitError;
import com.idx.smartspeakdock.baidu.unit.listener.ICalenderVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMapVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IMusicVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IShoppingVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.IWeatherVoiceListener;
import com.idx.smartspeakdock.baidu.unit.listener.OnResultListener;
import com.idx.smartspeakdock.baidu.unit.listener.VoiceActionAdapter;
import com.idx.smartspeakdock.baidu.unit.model.AccessToken;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.utils.AppExecutors;
import com.idx.smartspeakdock.utils.AuthInfo;
import com.idx.smartspeakdock.utils.Logger;

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
    private boolean isSessionOver = false;
    private TTSManager ttsManager;
    private APIService mApiService;
    //语音响应处理适配
    private VoiceActionAdapter mVoiceAdapter;
    private AppExecutors mAppExecutors;

    private UnitManager() {
    }

    public static UnitManager getInstance() {
        if (INSTANCE == null) {
            synchronized (UnitManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UnitManager();
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
        mAppExecutors = new AppExecutors();
    }

    /**
     * 发送数据至语音云端处理接口
     *
     * @param context 上下文
     * @param message 语音转化后的文本信息
     */
    public void sendMessage(final Context context, String message) {
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        Log.d(TAG, "send to unit: " + message);
        mApiService.communicate(new OnResultListener<CommunicateResponse>() {
            @Override
            public void onResult(CommunicateResponse result) {
                handleResponse(context, result);

            }

            @Override
            public void onError(UnitError error) {

            }
        }, sceneId, message, sessionId);

    }

    /**
     * 处理数据，播放语音，调用动作执行函数
     *
     * @param context 上下文
     * @param result  解析后的数据
     **/
    private void handleResponse(final Context context, CommunicateResponse result) {
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
                    ttsManager.batSpeak(bags, new TTSManager.SpeakCallback() {
                        @Override
                        public void onSpeakStart() {

                        }

                        @Override
                        public void onSpeakFinish() {
                            //执行自己的业务逻辑，回调执行，即语音播放结束后回调
                            executeTask(context, action, schema);
                        }
                    });

                } else if (!TextUtils.isEmpty(action.mainExe)) {
                    //没有语音信息返回
                    Log.d(TAG, "handleResponse: mainExe, " + action.mainExe);
                    //执行自己的业务逻辑
                    executeTask(context, action, schema);
                }

            }
        }
    }

    /**
     * @param context 上下文
     * @param action  执行的动作信息
     * @param schema  对应的词槽
     */
    private void executeTask(final Context context, final CommunicateResponse.Action action,
                             final CommunicateResponse.Schema schema) {
        mAppExecutors.getMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (mVoiceAdapter != null) {
                    Log.i(TAG, "run: action.id = "+action.actionId);
                    isSessionOver = mVoiceAdapter.onAction(action, schema);
                    //会话未结束，继续开始语音识别
                    if (!isSessionOver) {
                        context.sendBroadcast(new Intent(Intents.ACTION_RECOGNIZE));
                    }
                }

            }
        });
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
        mVoiceAdapter.setShoppingListener(listener);
    }

    /**
     * 判断当前会话是否结束，是否继续监听语音输入
     */
    public boolean isSessionOver() {
        return isSessionOver;
    }

    public void setSessionOver(boolean sessionOver) {
        isSessionOver = sessionOver;
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

        if (mAppExecutors != null) {
            mAppExecutors = null;
        }

    }
}
