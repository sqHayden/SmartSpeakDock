package com.idx.smartspeakdock.baidu.unit.listener;

/**
 * Created by derik on 17-12-25.
 */

public interface IMusicVoiceListener {

    void onPlay(int index);

    void onPlay(String name, ResultCallback resultCallback);

    void onPause();

    void onStop();

    void onContinue();

    void onNext();

    void onPrevious();
}
