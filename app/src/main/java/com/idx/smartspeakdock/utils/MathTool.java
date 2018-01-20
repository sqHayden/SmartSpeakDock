package com.idx.smartspeakdock.utils;


/**
 * Created by derik on 18-1-19.
 */

public class MathTool {
    public static int randomValue(int min, int max) {
        int num = (int) (min + Math.random() * (max - min));
        return num;
    }

    public static int randomValue(int value) {
        int num = (int) (Math.random() * value);
        return num;
    }
}
