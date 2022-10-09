package com.inspur.emmcloud.basemodule.media.record;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.basemodule.application.BaseApplication;

public class AudioFocusManager {
    @Nullable
    private AudioManager mAudioManager;
    private OnAudioFocusListener mListener;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusListener;

    @NonNull
    private static AudioFocusManager instance = new AudioFocusManager();

    private AudioFocusManager() {
    }

    @NonNull
    public static AudioFocusManager getInstance() {
        return instance;
    }


    public void requestAudioFocus() {
        if (null == mAudioManager) {
            mAudioManager = (AudioManager) BaseApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
        }
        if (null == mOnAudioFocusListener) {
            mOnAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

                @Override
                public void onAudioFocusChange(int focusChange) {
                    try {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            onCallback();
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        } else {
                            onCallback();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        try {
            mAudioManager.requestAudioFocus(mOnAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCallback() {
        if (mListener != null) {
            mListener.onAudioFocusChange();
        }
    }

    public void abandonAudioFocus() {
        try {
            if (null != mAudioManager && null != mOnAudioFocusListener) {
                mAudioManager.abandonAudioFocus(mOnAudioFocusListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAudioFocusListener(OnAudioFocusListener listener) {
        mListener = listener;
    }

    public interface OnAudioFocusListener {
        void onAudioFocusChange();
    }
}
