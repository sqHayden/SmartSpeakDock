package com.idx.smartspeakdock.utils;


/**
 * Created by derik on 18-1-19.
 */

public class MathTool {
    public static int randomIndex(int min, int max){
        int num = (int) (min + Math.random() * (max - min));
        return num;
    }
}
