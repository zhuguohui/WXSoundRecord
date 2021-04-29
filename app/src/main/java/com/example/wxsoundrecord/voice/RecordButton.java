package com.example.wxsoundrecord.voice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;


import com.example.wxsoundrecord.R;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;


public class RecordButton extends AppCompatButton {


    public RecordButton(Context context) {
        this(context, null);

    }

    public RecordButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private String mFileName = getContext().getFilesDir() + "/" + "voice_" + System.currentTimeMillis() + ".wav";


    private OnFinishedRecordListener finishedListener;
    /**
     * 最短录音时间
     **/
    private int MIN_INTERVAL_TIME = 1000;
    /**
     * 最长录音时间
     **/
    private int MAX_INTERVAL_TIME = 1000 * 60;


    private TextView mStateTV;


    private volatile MediaRecorder mRecorder;
    private boolean runningObtainDecibelThread = true;
    private ObtainDecibelThread mThread;

    private WXVoiceButton wxVoiceButton;

    private float downY;


    public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
        finishedListener = listener;
    }


    private long startTime;
    private Dialog recordDialog;
    private static final int WT_VOICE = 1;//改变voice
    private static final int WT_TXT = 2;//改变文字


    @SuppressLint("HandlerLeak")
    private void init() {

        //更新音量动画的handler

    }

    private long updateUITime = 0;


    private volatile boolean firstNotice = true;
    private int moveY = 0;

    //因为


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        moveY = (int) (event.getY() - downY);
        if (mStateTV != null && wxVoiceButton != null && moveY < 0 && moveY < -20) {
            mStateTV.setText("松开手指,取消发送");
            wxVoiceButton.setCancel(true);
        } else if (mStateTV != null && wxVoiceButton != null) {
            mStateTV.setText("手指上滑,取消发送");
            wxVoiceButton.setCancel(false);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //按下的时候，重新生成一个语音保存的地址，避免一直读写一个文件，可以引起错误
                downY = event.getY();
                setText("松开发送");
                MediaManager.reset();//停止其他音频播放
                initDialogAndStartRecord();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.setText("按住录音");
                if (moveY < 0 && moveY < -20) {  //当手指向上滑，会cancel
                    cancelRecord();
                } else {
                    finishRecord();
                }
                break;
        }

        return true;
    }

    /**
     * 初始化录音对话框 并 开始录音
     */
    private void initDialogAndStartRecord() {

        recordDialog = new Dialog(getContext(), R.style.like_toast_dialog_style);
        // view = new ImageView(getContext());
        View view = View.inflate(getContext(), R.layout.dialog_record, null);
        wxVoiceButton = view.findViewById(R.id.btn_wx_voice);
        mStateTV = view.findViewById(R.id.rc_audio_state_text);


        //mStateIV.setImageResource(R.drawable.ic_volume_1);
        mStateTV.setVisibility(View.VISIBLE);
        mStateTV.setText("手指上滑,取消发送");
        recordDialog.setContentView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = recordDialog.getWindow();
        if (window != null) {
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }


        if (startRecording()) {
            recordDialog.show();
        }

    }


    /**
     * 默认使用MP3转码
     */
    private boolean useMP3 = true;

    public void setUseMP3(boolean useMP3) {
        this.useMP3 = useMP3;
    }

    /**
     * 放开手指，结束录音处理
     */
    private void finishRecord() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post(() -> finishRecord());
            return;
        }

        long intervalTime = System.currentTimeMillis() - startTime;
        firstNotice = true;
        final String wavFileName = mFileName;
        File file = new File(wavFileName);
        stopRecording();
        if (!file.exists()) {
            //如果文件不存在，则返回
            //当我们到底最长时间，会在ObtainDecibelThread中，和onTouchEvent方法中，重复调用该方法
            //因此做一个检测

            return;
        }
        updateFileName();


        if (intervalTime < MIN_INTERVAL_TIME) {

            Toast.makeText(getContext(), "录音时间太短", Toast.LENGTH_SHORT).show();
            mStateTV.setText("录音时间太短");
            file.delete();

            return;
        }


        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(wavFileName);
            mediaPlayer.prepare();
            mediaPlayer.getDuration();

        } catch (Exception e) {

        }

        if (finishedListener == null) return;
        if (!useMP3) {
            //不用转码成MP3的话直接返回
            finishedListener.onFinishedRecord(wavFileName, mediaPlayer.getDuration() / 1000);
            updateFileName();
            return;
        }
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                finishedListener.onFinishedRecord(convertedFile.getPath(), mediaPlayer.getDuration() / 1000);
            }

            @Override
            public void onFailure(Exception error) {
                // Oops! Something went wrong
                error.addSuppressed(new RuntimeException("wav 转 mp3 失败"));
                error.printStackTrace();
//                ToastUtils.s(getContext(), "转码失败");
                Toast.makeText(getContext(), "转码失败", Toast.LENGTH_SHORT).show();

            }
        };
        AndroidAudioConverter.with(getContext())
                // Your current audio file
                .setFile(new File((wavFileName)))

                // Your desired audio format
                .setFormat(AudioFormat.MP3)

                // An callback to know when conversion is finished
                .setCallback(callback)

                // Start conversion
                .convert();

    }

    private void updateFileName() {
        mFileName = getContext().getFilesDir() + "/" + "voice_" + System.currentTimeMillis() + ".wav";
    }

    /**
     * 取消录音对话框和停止录音
     */
    public void cancelRecord() {
        stopRecording();
        File file = new File(mFileName);
        file.delete();
        updateFileName();
    }


    /**
     * 执行录音操作
     */
    //int num = 0 ;
    private boolean startRecording() {
        if (mRecorder != null) {
            mRecorder.reset();
        } else {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File file = new File(mFileName);


        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
            mRecorder.start();
            startTime = System.currentTimeMillis();
        } catch (Exception e) {

            e.printStackTrace();
            mRecorder.release();
            mRecorder = null;
            toast("录音启动失败[" + e.getMessage() + "]");
            return false;
        }
        runningObtainDecibelThread = true;
        mThread = new ObtainDecibelThread();
        mThread.start();
        return true;
    }

    private void toast(String content) {
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }

    private void stopRecording() {

        runningObtainDecibelThread = false;
        if (mThread != null) {
            mThread = null;
        }

        if (mRecorder != null) {
            try {
                mRecorder.stop();//停止时没有prepare，就会报stop failed
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            } catch (RuntimeException pE) {
                pE.printStackTrace();
            } finally {

            }
        }
        if (recordDialog != null) {
            recordDialog.dismiss();
            recordDialog = null;
        }
        if (wxVoiceButton != null) {
            wxVoiceButton.quit();
            wxVoiceButton = null;
        }
    }

    /**
     * 用来定时获取录音的声音大小，以驱动动画
     * 获取录音时间，提醒用户
     * 到达最大时间以后自动停止
     */
    private class ObtainDecibelThread extends Thread {


        @Override
        public void run() {

            while (runningObtainDecibelThread) {
                if (mRecorder == null || !runningObtainDecibelThread) {
                    break;
                }

                // int x = recorder.getMaxAmplitude(); //振幅
                int maxAmplitude = mRecorder.getMaxAmplitude();
                int db = maxAmplitude / 35;
                //  Log.i("zzz", "分贝:" + db);

                db = Math.min(200, db);

                long now = System.currentTimeMillis();
                long useTime = now - startTime;
                if (useTime > MAX_INTERVAL_TIME) {
                    finishRecord();
                    return;
                }
                //少于十秒则提醒
                long lessTime = (MAX_INTERVAL_TIME - useTime) / 1000;
                if (lessTime < 10) {
                    wxVoiceButton.setContent(lessTime + "秒后将结束录音");
                    if (firstNotice) {
                        firstNotice = false;//第一次需要震动
                        Vibrator vibrator = (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
                        vibrator.vibrate(500);
                    }

                } else {
                    wxVoiceButton.addVoiceSize(db);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public interface OnFinishedRecordListener {
        void onFinishedRecord(String audioPath, int time);
    }


}
