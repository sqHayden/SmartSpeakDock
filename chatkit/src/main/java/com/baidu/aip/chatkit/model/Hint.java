/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.chatkit.model;


public class Hint {

    public static final int ENABLE_HINT = 0;
    public static final int UNENABLE_HINT = 1;

    private int status = ENABLE_HINT;

    private String text;

    public Hint(String text) {
        this.text = text;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
