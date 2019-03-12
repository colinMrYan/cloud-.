package com.inspur.emmcloud.util.common;

import android.content.Context;
import android.media.MediaPlayer;

import com.inspur.emmcloud.R;

/**
 * Created by yufuchang on 2018/1/25.
 */

public class MediaPlayerUtils {
    private static final int VOICE_ON = 1;
    private static final int VOICE_OFF = 2;

    private MediaPlayer mediaPlayerVoiceOn;
    private MediaPlayer mediaPlayerVoiceOff;

    public MediaPlayerUtils(Context context) {
        mediaPlayerVoiceOn = MediaPlayer.create(context, R.raw.voice_search_on);
        mediaPlayerVoiceOff = MediaPlayer.create(context, R.raw.voice_search_off);
    }

    /**
     * 播放开始提示音
     */
    public void playVoiceOn() {
        mediaPlayerVoiceOn.start();
    }

    /**
     * 播放结束提示音
     */
    public void playVoiceOff() {
        mediaPlayerVoiceOff.start();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mediaPlayerVoiceOn.isPlaying()) {
            mediaPlayerVoiceOn.stop();
        }
        if (mediaPlayerVoiceOff.isPlaying()) {
            mediaPlayerVoiceOff.stop();
        }
        mediaPlayerVoiceOn.release();
        mediaPlayerVoiceOff.release();
    }
}
