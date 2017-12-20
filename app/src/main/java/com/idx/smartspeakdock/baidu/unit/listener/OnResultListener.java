/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.idx.smartspeakdock.baidu.unit.listener;

import com.idx.smartspeakdock.baidu.unit.exception.UnitError;

public interface OnResultListener<T> {
    void onResult(T result);

    void onError(UnitError error);
}
