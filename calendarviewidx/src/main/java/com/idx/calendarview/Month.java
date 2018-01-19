/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.idx.calendarview;

import java.io.Serializable;

@SuppressWarnings("unused")
class Month implements Serializable {
    private int diff;//日期偏移
    private int count;
    private String months;
    private int year;
    private int month;

    int getDiff() {
        return diff;
    }

    void setDiff(int diff) {
        this.diff = diff;
    }

    int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

    String getMonths() {
        return months;
    }

    void setMonths(String month) {
        this.months = month;
    }
    void setMonth(int month){
        this.month = month;
    }
    int getMonth(){
        return month;
    }
    int getYear() {
        return year;
    }

    void setYear(int year) {
        this.year = year;
    }
}
