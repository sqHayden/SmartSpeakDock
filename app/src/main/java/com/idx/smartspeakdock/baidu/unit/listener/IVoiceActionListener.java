package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.baidu.unit.model.CommunicateResponse;

/**
 * Created by derik on 17-12-22.
 */

public interface IVoiceActionListener {
    boolean onAction(CommunicateResponse.Action action, CommunicateResponse.Schema schema);
}
