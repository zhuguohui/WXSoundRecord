package com.example.wxsoundrecord.bean;

/**
 * @author zhuguohui
 * @description:
 * @date :2021/4/29 11:00
 */
public class VoiceMsg {
    private String path;
    private int duration;
    private long time;

    public VoiceMsg(String path, int duration, long time) {
        this.path = path;
        this.duration = duration;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}