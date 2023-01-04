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
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.shuyu.waveview.FileUtils;

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
    private static final int VOICE_ERROR_TOAST = 6;
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
    private boolean isDeviceError = false;
    private float lastCallBackDurationTime = 0;
    private android.media.AudioManager audioManager;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VOICE_MESSAGE:
                    if (durationTime < 60.0) {
                        mDialogManager.updateVoiceLevelAndDurationTime(volumeSize, durationTime);
                    } else if (durationTime >= 60.0) {
                        isRecording = false;
                        voiceRecordUIFinish();
//                        if (AppUtils.getIsVoiceWordOpen()) {
                        mListener.onFinished(60f, audioRecorderManager.getCurrentFilePath());
//                        } else {
//                            mListener.onFinished(60f, mp3FilePath);
//                        }
                        reset();
                    }
                    break;
                case VOICE_DISMISS_DIALOG:
                    voiceRecordUIFinish();
                    break;
                case VOICE_ERROR_TOAST:
                    voiceRecordUIFinish();
                    ToastUtils.show(MyApplication.getInstance(), getContext().getString(R.string.voice_audio_record_unavailiable));
                    break;
            }

        }
    };

    /**
     * 先实现两个参数的构造方法，布局会默认引用这个构造方法， 用一个 构造参数的构造方法来引用这个方法 * @param context
     */
    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(final Context context, AttributeSet attrs) {
        super(context, attrs);

        audioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mDialogManager = new AudioDialogManager(getContext());
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                if (PermissionRequestManagerUtils.getInstance().isHasPermission(getContext(), Permissions.RECORD_AUDIO)) {
//                    startRecordVoice();
//                } else {
//                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
//                        @Override
//                        public void onPermissionRequestSuccess(List<String> permissions) {
//                            voiceRecordUIFinish();
//                            reset();
//                        }
//
//                        @Override
//                        public void onPermissionRequestFail(List<String> permissions) {
//                            ToastUtils.show(context, PermissionRequestManagerUtils.getInstance().getPermissionToast(context, permissions));
//                        }
//
//
//                    });
//                }
                if (VoiceCommunicationManager.getInstance().isVoiceBusy()) {
                    ToastUtils.show(R.string.voice_communication_can_not_use_this_feature);
                } else {
                    startRecordVoice();
                }
                return false;
            }
        });
    }

    private void startRecordVoice() {
        audioManager.requestAudioFocus(null, android.media.AudioManager.STREAM_MUSIC,
                android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        isRecording = true;
        mDialogManager.showRecordingDialog();
//                if (AppUtils.getIsVoiceWordOpen()) {
        //录制wav的分支
        audioRecorderManager = AudioRecorderManager.getInstance();
        audioRecorderManager.setCallBack(new AudioRecorderManager.AudioDataCallBack() {
            @Override
            public void onDataChange(int volume, float duration) {
                if (isRecording) {
                    //超过0.2秒再回调
                    if (duration - durationTime > 0.2) {
                        durationTime = duration;
                        volumeSize = volume;
                        handler.sendEmptyMessage(VOICE_MESSAGE);
                    }
                }
            }

            @Override
            public void onWavAudioPrepareState(int state) {
                switch (state) {
                    case AudioRecordErrorCode.SUCCESS:
                        if (audioRecorderManager != null) {
                            audioRecorderManager.startRecord();
                            mListener.onStartRecordingVoice();
                        }
                        changeState(STATE_RECORDING);
                        break;
                    case AudioRecordErrorCode.E_NOSDCARD:
                        recoveryState();
                        ToastUtils.show(MyApplication.getInstance(), MyApplication.getInstance().getString(R.string.error_no_sdcard));
                        break;
                    case AudioRecordErrorCode.E_ERROR:
                        handler.sendEmptyMessage(VOICE_ERROR_TOAST);
                        break;
                    default:
                        recoveryState();
                        break;
                }
            }

        });
        //按下开关，先调用准备Audio
        audioRecorderManager.prepareWavAudioRecord();
//                } else {
//                    //录制mp3的分支
//                    isDeviceError = false;
//                    String mp3FileDir = getMp3FilePath();
//                    File fileDir = new File(mp3FileDir);
//                    if (!fileDir.exists()) {
//                        fileDir.mkdirs();
//                    }
//                    mp3FilePath = mp3FileDir + AppUtils.generalFileName() + ".mp3";
//                    File file = new File(mp3FilePath);
//                    mp3Recorder = new MP3Recorder(file);
//                    //处理异常
//                    mp3Recorder.setErrorHandler(new Handler() {
//                        @Override
//                        public void handleMessage(Message msg) {
//                            super.handleMessage(msg);
//                            if (msg.what == MP3Recorder.ERROR_TYPE) {
//                                mListener.onErrorRecordingVoice(MP3Recorder.ERROR_TYPE);
//                                resolveMp3Error();
//                                isDeviceError = true;
//                            }
//                        }
//                    });
//                    //设备正常则录音
//                    if (!isDeviceError) {
//                        recorderMp3Voice();
//                    }
//                }
    }

    /**
     * 出现异常后恢复dialog和按钮状态
     */
    private void recoveryState() {
        changeState(STATE_NORMAL);
        voiceRecordUIFinish();
    }

    /**
     * 录音异常
     */
    private void resolveMp3Error() {
        recoveryState();
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
            mp3BeginTime = System.currentTimeMillis();
            mp3Recorder.start();
            changeState(STATE_RECORDING);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while (isRecording) {
                        float time = (System.currentTimeMillis() - mp3BeginTime) / 1000f;
                        float spacingTime = time - lastCallBackDurationTime;
                        durationTime = time;
                        if (spacingTime > 0.2) {
                            lastCallBackDurationTime = time;
                            volumeSize = getMp3Volume(mp3Recorder.getVolume());
                            handler.sendEmptyMessage(VOICE_MESSAGE);
                        }
                    }
                }
            };
            new Thread(runnable).start();
        } catch (Exception e) {
            recoveryState();
            mListener.onErrorRecordingVoice(MP3Recorder.ERROR_TYPE);
            e.printStackTrace();
        }
    }

    /**
     * 获取录制mp3时的音量
     *
     * @param realVolume
     */
    private int getMp3Volume(int realVolume) {
        int db = 1;
        if (realVolume <= 10) {
            db = 1;
        } else if (realVolume <= 30) {
            db = 2;
        } else if (realVolume <= 100) {
            db = 3;
        } else if (realVolume <= 250) {
            db = 4;
        } else if (realVolume <= 500) {
            db = 5;
        } else {
            db = 6;
        }
        return db;
    }

    /**
     * 设置Audio回调
     *
     * @param listener
     */
    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    /**
     * 播放开始录制音效
     */
    private void playRecordStartMusic() {
        if (!VoiceCommunicationManager.getInstance().isVoiceBusy()) {
            MediaPlayerManagerUtils.getManager().play(R.raw.voice_search_on, null);
        }
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
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                voiceRecordToolFinish();
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                if ((!isRecording || durationTime < 0.8f)) {
                    mDialogManager.tooShort();
                    //延迟500毫秒
                    handler.sendEmptyMessageDelayed(VOICE_DISMISS_DIALOG, 500);
                } else if (mCurrentState == STATE_RECORDING) {//正常录制结束
                    voiceRecordUIFinish();
                    if (mListener != null) {
//                        if (AppUtils.getIsVoiceWordOpen()) {
                        mListener.onFinished(durationTime, audioRecorderManager.getCurrentFilePath());
//                        } else if (!isDeviceError) {
//                            mListener.onFinished(durationTime, mp3FilePath);
//                        }
                    }

                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    //保留此状态为了处理上滑取消录音状态
                    voiceRecordUIFinish();
                } else {
                    voiceRecordUIFinish();
                }
                reset();// 恢复标志位
                break;
//            case MotionEvent.ACTION_CANCEL:
//                voiceRecordToolFinish();
//                voiceRecordUIFinish();
//                reset();// 恢复标志位
//                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 结束处理
     */
    private void voiceRecordUIFinish() {
        if (mDialogManager != null) {
            mDialogManager.dismissRecordingDialog();
        }
        if (audioRecorderManager != null) {
            audioRecorderManager.stopRecord();
        }
        audioManager.abandonAudioFocus(null);
    }

    /**
     * 停止录音工具的录音
     */
    private void voiceRecordToolFinish() {
        if (audioRecorderManager != null) {
            audioRecorderManager.stopRecord();
        }
        if (mp3Recorder != null) {
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
        lastCallBackDurationTime = 0;
        volumeSize = 0;
        changeState(STATE_NORMAL);
    }

    /**
     * 根据手指所在的坐标判断用户是否想要取消发送
     *
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
     *
     * @param state
     */
    private void changeState(int state) {
        // TODO Auto-generated method stub
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:

                    setBackgroundResource(R.drawable.design3_bg_corner_ne15_6dp);
                    setText(R.string.hold_to_talk);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.design3_bg_corner_ne07_6dp);
                    setText(R.string.release_to_send);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.design3_bg_corner_ne07_6dp);
                    setText(R.string.release_to_cancel);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }


    /**
     * 获取Mp3文件夹路径
     *
     * @return
     */
    private String getMp3FilePath() {
        return MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
    }

    /**
     * 录音完成后的回调，回调给activiy，可以获得mtime和文件的路径
     *
     * @author nickming
     */
    public interface AudioFinishRecorderListener {
        void onStartRecordingVoice();

        void onFinished(float seconds, String filePath);

        void onErrorRecordingVoice(int errorType);
    }
}
