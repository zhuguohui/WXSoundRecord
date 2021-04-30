# WXSoundRecord
仿微信录音的库

# 序言
最近开发了一个语音输入控件，UI效果和微信的保持基本一直，除了颜色不一样。具体的功能如下

 - 动画效果和微信一样，都是通过声音的分贝大小驱动动画的大小
 - 如果声音过小，会进入监听模式，（一个小波浪从右到左移动）
 - 录音的气泡会随着时间不断变大
 - 最长支持60秒录音，在最后十秒会震动提示用户
 - 最后十秒会有倒计时，如果超时会自动截取
 - 支持转MP3格式
 - 使用简单，一个回调返回语音文件的地址和语音的时长


# 效果
这个demo 包含了录音，播放，权限申请的所有功能。建议自己下载试一试效果。
## 动画效果
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210430102924908.gif#pic_center)

## 监听模式（声音太小的时候自动进入）
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210430103811328.gif#pic_center)
## 其他功能
上滑取消和超时提醒

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210430104345864.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIyNzA2NTE1,size_16,color_FFFFFF,t_70#pic_center)

# 使用
布局文件中直接引用
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:layout_width="match_parent"
        android:id="@+id/recyclerView"
        android:layout_height="match_parent"
        android:layout_weight="1" />

    <RelativeLayout
        android:id="@+id/layout_voice"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#f5f5f5">

        <com.example.wxsoundrecord.voice.RecordButton
            android:id="@+id/btnAudio"
            android:paddingStart="140dp"
            android:paddingEnd="140dp"
            style="?android:attr/borderlessButtonStyle"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:layout_margin="10dp"
            android:background="@drawable/dyps_shape_txt_bg"
            android:drawableStart="@drawable/dypc_ic_voice_press"
            android:gravity="center"
            android:text="按住说话"
            android:textSize="13dp"
            android:visibility="visible" />
    </RelativeLayout>
</LinearLayout>
```
java中 默认转换MP3 是自动开启的。

```java
  		 RecordButton recordButton = findViewById(R.id.btnAudio);
        recordButton.setUseMP3(true);//使用mp3格式
        recordButton.setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                VoiceMsg msg = new VoiceMsg(audioPath, time, System.currentTimeMillis());
                voiceMsgList.add(msg);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(voiceMsgList.size() - 1);
            }
        });
```

# 感谢
wav 转 mp3 使用的是下面这个库

> [AndroidAudioConverter](https://github.com/adrielcafe/AndroidAudioConverter)

需要在Application中初始化

```java
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
```

# 实现
还是挺复杂的，准备单独弄一篇博客介绍

# 源码

> [WXSoundRecord](https://github.com/zhuguohui/WXSoundRecord)
