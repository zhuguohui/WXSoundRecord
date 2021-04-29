package com.example.wxsoundrecord;

import android.app.Application;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

/**
 * @author zhuguohui
 * @description:
 * @date :2021/4/29 10:25
 */
public class MyApp  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //wav转mp3工具
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
            }
        });
    }
}