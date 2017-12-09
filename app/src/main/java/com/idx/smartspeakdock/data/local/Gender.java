package com.idx.smartspeakdock.data.local;

/**
 * Created by derik on 17-12-8.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public enum Gender {
    MALE(0), FEMALE(1);

    private final int index;
    Gender(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }
}
