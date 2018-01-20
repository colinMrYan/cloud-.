package com.inspur.emmcloud.util.privates;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.inspur.emmcloud.interf.OnVoiceLevelCallBack;
import com.inspur.emmcloud.util.common.LogUtils;

/**
 * Created by yufuchang on 2018/1/20.
 */

public class MediaRecorderLevelUtils {

    private static final String TAG = "AudioRecord";
    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;
    private boolean isGetVoiceRun;
    private Object mLock;
    private OnVoiceLevelCallBack onVoiceLevelCallBack;
    public static MediaRecorderLevelUtils mediaRecorderUtils;

    public static MediaRecorderLevelUtils getInstance(){
        if(mediaRecorderUtils == null){
            synchronized (MediaRecorderLevelUtils.class){
                if(mediaRecorderUtils == null){
                    mediaRecorderUtils = new MediaRecorderLevelUtils();
                }
            }
        }
        return mediaRecorderUtils;
    }

    private MediaRecorderLevelUtils() {
        mLock = new Object();
    }

    public void setOnVoiceLevelCallBack(OnVoiceLevelCallBack onVoiceLevelCallBack) {
        this.onVoiceLevelCallBack = onVoiceLevelCallBack;
    }

    public void getVoiceLevel() {
        if (isGetVoiceRun) {
            LogUtils.YfcDebug( "还在录着呢");
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
            LogUtils.YfcDebug( "mAudioRecord初始化失败");
        }
        isGetVoiceRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
//                    onVoiceLevelCallBack.onVoiceLevelCallBack(volume);
                    LogUtils.YfcDebug(  "分贝值:" + volume);
                    // 大概一秒5次
                    synchronized (mLock) {
                        try {
                            mLock.wait(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                stopRecord();
            }
        }).start();
    }

    public void stopRecord() {
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
    }

}
