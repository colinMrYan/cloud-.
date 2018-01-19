package com.inspur.emmcloud.util.privates;

import android.media.MediaRecorder;

/**
 * Created by yufuchang on 2018/1/19.
 */

public class MediaRecorderUtils {
    private MediaRecorder recorder;
    public MediaRecorderUtils(){
        recorder=new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//声音来源是话筒
        try {
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
