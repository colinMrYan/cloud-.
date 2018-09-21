package com.inspur.emmcloud.widget.audiorecord;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.czt.mp3recorder.MP3Recorder;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.shuyu.waveview.FileUtils;

import java.io.File;

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
    private static final int VOICE_DISMISS_DIALOG = 5;
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
    //录制mp3的Record
    private MP3Recorder mp3Recorder;
    //录制mp3的文件路径
    private String mp3FilePath = "";
    private long mp3BeginTime;

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
                if(AppUtils.getIsVoiceWordOpen()){
                    audioRecorderManager = AudioRecorderManager.getInstance();
                    audioRecorderManager.setCallBack(new AudioRecorderManager.AudioDataCallBack() {
                        @Override
                        public void onDataChange(int volume, float duration) {
                            if(isRecording){
                                //超过0.2秒再回调
                                if(duration - durationTime > 0.2){
                                    durationTime = duration;
                                    volumeSize = volume;
                                    handler.sendEmptyMessage(VOICE_MESSAGE);
                                }
                            }
                        }

                        @Override
                        public void onAudioPrepared(int state) {
                            changeState(STATE_RECORDING);
                            audioRecorderManager.startRecord();
                            mListener.onStartRecordingVoice();
                        }

                        @Override
                        public void onAudioPrepareError() {
                            if(mDialogManager != null){
                                mDialogManager.dismissRecordingDialog();
                            }
                            ToastUtils.show(MyApplication.getInstance(),"当前录音设备不可用，请检查录音权限权限是否开启");
                        }
                    });
                    //按下开关，先调用准备Audio
                    audioRecorderManager.prepareAudioRecord();
                }else{
                    mp3FilePath = getMp3FilePath()+AppUtils.generalFileName()+".mp3";
                    File file = new File(mp3FilePath);
                    mp3Recorder = new MP3Recorder(file);
                    //处理异常
                    mp3Recorder.setErrorHandler(new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == MP3Recorder.ERROR_TYPE) {
                                resolveMp3Error();
                            }
                        }
                    });
                    recorderMp3Voice();
                }
                return false;
            }
        });
    }

    /**
     * 录音异常
     */
    private void resolveMp3Error() {
        changeState(STATE_NORMAL);
        FileUtils.deleteFile(mp3FilePath);
        mp3FilePath = "";
        if (mp3Recorder != null && mp3Recorder.isRecording()) {
            mp3Recorder.stop();
        }
    }

    /**
     * 录制mp3音频
     */
    private void recorderMp3Voice() {
        try {
            changeState(STATE_RECORDING);
            mp3BeginTime = System.currentTimeMillis();
            mp3Recorder.start();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while (isRecording){
                        float time = (System.currentTimeMillis() - mp3BeginTime)/1000f;
                        float spacingTime = time - durationTime;
                        if(spacingTime > 0.2){
                            durationTime = time;
                            volumeSize = getMp3Volume(mp3Recorder.getVolume());
                            handler.sendEmptyMessage(VOICE_MESSAGE);
                        }
                    }
                }
            };
            new Thread(runnable).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取录制mp3时的音量
     * @param realVolume
     */
    private int getMp3Volume(int realVolume) {
        int db = 1;
        if(realVolume <= 10){
            db = 1;
        }else if(realVolume <= 30){
            db = 2;
        }else if(realVolume <= 100){
            db = 3;
        }else if(realVolume <= 250){
            db = 4;
        }else if(realVolume <= 500){
            db = 5;
        }else{
            db = 6;
        }
        return db;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VOICE_MESSAGE:
                    if( durationTime < 60.0){
                        mDialogManager.updateVoiceLevelAndDurationTime(volumeSize,durationTime);
                    }else if(durationTime >= 60.0){
                        isRecording = false;
                        voiceRecordFinish();
                        if(AppUtils.getIsVoiceWordOpen()){
                            mListener.onFinished(60f,audioRecorderManager.getCurrentFilePath());
                        }else{
                            reset();
                            voiceRecordFinish();
                            mListener.onFinished(60f,mp3FilePath);
                        }
                    }
                    break;
                case VOICE_DISMISS_DIALOG:
                    voiceRecordFinish();
                    break;
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
                if (!isRecording || durationTime < 0.8f) {
                    mDialogManager.tooShort();
                    //延迟500毫秒
                    handler.sendEmptyMessageDelayed(VOICE_DISMISS_DIALOG,500);
                } else if (mCurrentState == STATE_RECORDING && (durationTime < 60)) {//正常录制结束
                    voiceRecordFinish();
                    if (mListener != null) {// 并且callbackActivity，保存录音
                        if(AppUtils.getIsVoiceWordOpen()){
                            mListener.onFinished(durationTime,audioRecorderManager.getCurrentFilePath());
                        }else{
                            mListener.onFinished(durationTime,mp3FilePath);
                        }
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
            mDialogManager.dismissRecordingDialog();
        }
        if(audioRecorderManager != null){
            audioRecorderManager.stopRecord();
        }
        if(mp3Recorder != null){
            mp3Recorder.setPause(false);
            mp3Recorder.stop();
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

    /**
     * 获取Mp3文件夹路径
     *
     * @return
     */
    private String getMp3FilePath() {
        return MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
    }
}
