package com.idx.calendarview;

/**
 * Created by geno on 27/12/17.
 */

public class MessageEvent {
    private String message;
    private Integer day;
    public MessageEvent(String message,Integer day){
        this.message = message;
        this.day = day;
    }

    public String getMessage(){
        return message;
    }
    public Integer getday(){
        return day;
    }
}
