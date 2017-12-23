package com.idx.smartspeakdock.music.entity;



public class LocalMusicItem {
    private LocalMusic mLocalMusic;

    private int mClassificationId;
    private String mClassificationName;

    public LocalMusicItem(LocalMusic localMusic) {
        mLocalMusic = localMusic;
    }

    public LocalMusic getAudio() {
        return mLocalMusic;
    }

    public void setClassificationId(int classificationId) {
        mClassificationId = classificationId;
    }

    public int getClassificationId() {
        return mClassificationId;
    }

    public void setClassificationName(String classificationName) {
        mClassificationName = classificationName;
    }

    public String getClassificationName() {
        return mClassificationName;
    }
}
