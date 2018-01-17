package com.idx.smartspeakdock.baidu.wakeup;

/**
 * Created by fujiayi on 2017/6/21.
 */

public interface IWakeupListener {

    void onSuccess(String word, WakeUpResult result);

    void onStop();

    void onError(int errorCode, String errorMessage, WakeUpResult result);

}
