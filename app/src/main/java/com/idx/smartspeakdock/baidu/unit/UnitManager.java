package com.idx.smartspeakdock.baidu.unit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.aip.chatkit.model.Hint;
import com.baidu.aip.chatkit.model.Message;
import com.baidu.aip.chatkit.model.User;
import com.baidu.tts.client.TtsMode;
import com.idx.smartspeakdock.baidu.tts.TTSManager;
import com.idx.smartspeakdock.baidu.unit.exception.UnitError;
import com.idx.smartspeakdock.baidu.unit.listener.OnResultListener;
import com.idx.smartspeakdock.baidu.unit.model.AccessToken;
import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;
import com.idx.smartspeakdock.utils.AuthInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by derik on 17-12-19.
 */

public class UnitManager {

    private static final String TAG = UnitManager.class.getName();
    private static UnitManager INSTANCE = null;
    private String sessionId;
    private String message;
    private int sceneId = 14818;
    private int id = 0;
    private User sender;
    private User cs;
    private String accessToken;
    private Context mContext;
    private TTSManager ttsManager;

    public UnitManager(Context context) {
        mContext = context;
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

    private void init(Context context) {
        sender = new User("0", "kf", "", true);
        cs = new User("1", "客服", "", true);
        Map<String, Object> authParams = AuthInfo.getAuthParams(context);
        ttsManager = TTSManager.getInstance(mContext, TtsMode.ONLINE);
        APIService.getInstance().init(context);
        APIService.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
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
    }

    public void sendMessage(Message message) {
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        APIService.getInstance().communicate(new OnResultListener<CommunicateResponse>() {
            @Override
            public void onResult(CommunicateResponse result) {
                handleResponse(result);

            }

            @Override
            public void onError(UnitError error) {

            }

        }, sceneId, message.getText(), sessionId);

    }

    private void handleResponse(CommunicateResponse result) {
        if (result != null) {
            sessionId = result.sessionId;
            //  如果有对于的动作action，请执行相应的逻辑
            List<CommunicateResponse.Action> actionList = result.actionList;
            if (actionList.size() > 1) {
                Message message = new Message(String.valueOf(id++), cs, "", new Date());
                for (CommunicateResponse.Action action : actionList) {

                    if (!TextUtils.isEmpty(action.say)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(action.say);

                        Message actionMessage = new Message("", cs, sb.toString(), new Date());

                        for (String hintText : action.hintList) {

                            actionMessage.getHintList().add(new Hint(hintText));
                        }
                        message.getComplexMessage().add(actionMessage);
                    }
                }
//                messagesAdapter.addToStart(message, true);
            } else if (actionList.size() == 1) {
                CommunicateResponse.Action action = actionList.get(0);
                if (!TextUtils.isEmpty(action.say)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(action.say);

                    Message message = new Message(String.valueOf(id++), cs, sb.toString(), new Date());
//                    messagesAdapter.addToStart(message, true);
                    for (String hintText : action.hintList) {

                        message.getHintList().add(new Hint(hintText));
                    }

                }

                // 执行自己的业务逻辑
                if ("rain_user_time_clarify".equals(action.actionId)) {
                    Log.d("rain", "time ?: ");
                    ttsManager.speak("什么时候？");
                } else if ("rain_user_loc_clarify".equals(action.actionId)) {
                    Log.d("rain", "where ?: ");
                    ttsManager.speak("哪里呢？");
                } else if ("rain_satisfy".equals(action.actionId)) {
                    Log.d("rain", " OK: ");
                    ttsManager.speak("为你查询下雨情况？");
                }

                if (!TextUtils.isEmpty(action.mainExe)) {
//                    Toast.makeText(UnitManager.this, "请执行函数：" + action.mainExe, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
