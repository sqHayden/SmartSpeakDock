package com.idx.smartspeakdock.weather.event;

import com.idx.smartspeakdock.weather.presenter.ReturnVoice;

/**
 * Created by ryan on 18-1-20.
 * Email: Ryan_chan01212@yeah.net
 */

public class ReturnVoiceEvent {
    ReturnVoice returnVoice;

    public ReturnVoiceEvent(ReturnVoice returnVoice){
        this.returnVoice = returnVoice;
    }

    public ReturnVoice getReturnVoice() {
        return returnVoice;
    }

    public void setReturnVoice(ReturnVoice returnVoice) {
        this.returnVoice = returnVoice;
    }
}
