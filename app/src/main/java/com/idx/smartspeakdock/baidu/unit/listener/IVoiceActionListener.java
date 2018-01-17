package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;

/**
 * Created by derik on 17-12-22.
 */

public interface IVoiceActionListener {

    interface IActionCallback {
        void onResult(boolean sayBye);
    }
    boolean onAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema, IActionCallback actionCallback);
}
