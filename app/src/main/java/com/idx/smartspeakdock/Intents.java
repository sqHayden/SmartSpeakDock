package com.idx.smartspeakdock;

/**
 * Created by derik on 17-12-12.
 */

public interface Intents {
    String ACTION_WAKE_UP_START = "com.idx.speaker.WAKE_UP_START";
    String ACTION_WAKE_UP_STOP = "com.idx.speaker.WAKE_UP_STOP";
    //收到唤醒的通知
    String ACTION_WAKE_UP = "com.idx.speaker.WAKE_UP";
    String ACTION_RECOGNIZE_START = "com.idx.speaker.RECOGNIZE_START";
    String ACTION_RECOGNIZE_END = "com.idx.speaker.RECOGNIZE_END";
    String ACTION_SESSION_START = "com.idx.speaker.SESSION_START";
    String ACTION_SESSION_END = "com.idx.speaker.SESSION_END";
}
