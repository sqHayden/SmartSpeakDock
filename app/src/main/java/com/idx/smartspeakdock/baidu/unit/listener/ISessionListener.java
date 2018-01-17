package com.idx.smartspeakdock.baidu.unit.listener;

/**
 * Created by derik on 18-1-17.
 */

public interface ISessionListener {

    void onSessionFinish();

    void onRegContinue();

    void onSessionError();

}
