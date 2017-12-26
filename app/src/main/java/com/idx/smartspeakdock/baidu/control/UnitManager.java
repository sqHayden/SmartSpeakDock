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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by derik on 17-12-19.
 */

public class UnitManager {

    private static final String TAG = UnitManager.class.getName();
    private static UnitManager INSTANCE = null;
    private String sessionId;
    private int sceneId = 15213;
    private String accessToken;
    //会话标识
    private boolean isSessionOver = false;
    private TTSManager ttsManager;
    private APIService mApiService;
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

    private void handleResponse(final Context context, CommunicateResponse result) {
        if (result != null) {
            sessionId = result.sessionId;
            //  如果有对于的动作action，请执行相应的逻辑
            List<CommunicateResponse.Action> actionList = result.actionList;
            Log.e(TAG, "handleResponse: size, " + actionList.size());
            if (actionList.size() > 1) {

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
                final CommunicateResponse.Action action = actionList.get(0);

                if (!TextUtils.isEmpty(action.say)) {
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
                            // 执行自己的业务逻辑，回调执行
                            executeTask(context, action);
                        }
                    });
                }

            }
        }
    }

    private void executeTask(final Context context, final CommunicateResponse.Action action) {
        mAppExecutors.getMainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (mVoiceAdapter != null) {
                    isSessionOver = mVoiceAdapter.onAction(action);
                    //会话未结束，继续开始语音识别
                    if (!isSessionOver) {
                        context.sendBroadcast(new Intent(Intents.ACTION_RECOGNIZE));
                    }
                }

                if (!TextUtils.isEmpty(action.mainExe)) {
                    Log.d(TAG, "handleResponse: mainExe, " + action.mainExe);
                    //Toast.makeText(UnitManager.this, "请执行函数：" + action.mainExe, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setMusicVoiceListener(IMusicVoiceListener listener) {
        Log.d(TAG, "setMusicVoiceListener: ");
        mVoiceAdapter.setMusicListener(listener);

    }

    public void setCalenderVoiceListener(ICalenderVoiceListener listener) {
        mVoiceAdapter.setCalenderListener(listener);
    }

    public void setMapVoiceListener(IMapVoiceListener listener) {
        mVoiceAdapter.setMapListener(listener);
    }

    public void setWeatherVoiceListener(IWeatherVoiceListener listener) {
        mVoiceAdapter.setWeatherListener(listener);
    }

    public void setShoppingVoiceListener(IShoppingVoiceListener listener) {
        mVoiceAdapter.setShoppingListener(listener);
    }

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
