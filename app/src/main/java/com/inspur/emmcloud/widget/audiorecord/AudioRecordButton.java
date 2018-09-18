package com.inspur.emmcloud.widget.audiorecord;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.MediaPlayerManagerUtils;

public class AudioRecordButton extends Button {

    //正常状态
    private static final int STATE_NORMAL = 1;
    //录制状态
    private static final int STATE_RECORDING = 2;
    //上滑取消状态
    private static final int STATE_WANT_TO_CANCEL = 3;
    //上滑取消辅助变量
    private static final int DISTANCE_Y_CANCEL = 50;
    private static final int VOICE_MESSAGE = 4;
    private int mCurrentState = STATE_NORMAL;
    // 已经开始录音
    private boolean isRecording = false;
    //Dialog管理器，根据以上几个状态改变UI显示
    private AudioDialogManager mDialogManager;
    //音频录制管理器
    private AudioRecorderManager audioRecorderManager;
    //录音时间
    private float durationTime = 0;
    private int volumeSize = 0;
    //录音回调
    private AudioFinishRecorderListener mListener;

    /**
     * 先实现两个参数的构造方法，布局会默认引用这个构造方法， 用一个 构造参数的构造方法来引用这个方法 * @param context
     */
    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new AudioDialogManager(getContext());
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isRecording = true;
                mDialogManager.showRecordingDialog();
                audioRecorderManager = AudioRecorderManager.getInstance();
                audioRecorderManager.setCallBack(new AudioRecorderManager.AudioDataCallBack() {
                    @Override
                    public void onDataChange(int volume, float duration) {
                        if(isRecording){
                            durationTime = duration;
                            volumeSize = volume;
                            handler.sendEmptyMessage(VOICE_MESSAGE);
                        }
                    }
                });
                audioRecorderManager.startRecord();
                return false;
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(audioRecorderManager.isRecording() && durationTime <= 60.0){
                mDialogManager.updateVoiceLevel(volumeSize,durationTime);
            }else if(audioRecorderManager.isRecording() && durationTime >= 60.0){
                isRecording = false;
                voiceRecordFinish();
                mListener.onFinished(60f,audioRecorderManager.getCurrentFilePath());
            }
        }
    };

    /**
     * 设置Audio回调
     * @param listener
     */
    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    /**
     * 播放开始录制音效
     */
    private void playRecordStartMusic(){
        MediaPlayerManagerUtils.getManager().play(R.raw.voice_search_on, null);
    }

    /**
     * 直接复写这个监听函数
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                playRecordStartMusic();
                changeState(STATE_RECORDING);
                mListener.onStartRecordingVoice();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                if (!isRecording || audioRecorderManager.getDuration() < 0.8f) {
                    mDialogManager.tooShort();
                    voiceRecordFinish();
                } else if (mCurrentState == STATE_RECORDING && (durationTime < 60)) {//正常录制结束
                    voiceRecordFinish();
                    if (mListener != null) {// 并且callbackActivity，保存录音
                        mListener.onFinished(durationTime,audioRecorderManager.getCurrentFilePath());
                    }
                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    voiceRecordFinish();
                }
                reset();// 恢复标志位
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 结束处理
     */
    private void voiceRecordFinish() {
        if(mDialogManager != null){
            mDialogManager.dimissDialog();
        }
        if(audioRecorderManager != null){
            audioRecorderManager.stopRecord();
        }
    }

    /**
     * 回复标志位以及状态
     */
    private void reset() {
        isRecording = false;
        durationTime = 0;
        volumeSize = 0;
        changeState(STATE_NORMAL);
    }

    /**
     * 根据手指所在的坐标判断用户是否想要取消发送
     * @param x
     * @param y
     * @return
     */
    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        return y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL;
    }

    /**
     * 根据状态改变Dialog和button的UI
     * @param state
     */
    private void changeState(int state) {
        // TODO Auto-generated method stub
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.bg_record_btn_normal);
                    setText(R.string.hold_to_talk);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.bg_record_btn_recording);
                    setText(R.string.release_to_send);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.bg_record_btn_recording);
                    setText(R.string.release_to_cancel);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }

    @Override
    public boolean onPreDraw() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 录音完成后的回调，回调给activiy，可以获得mtime和文件的路径
     *
     * @author nickming
     */
    public interface AudioFinishRecorderListener {
        void onStartRecordingVoice();
        void onFinished(float seconds, String filePath);
        void onErrorRecordingVoice();
    }
}
