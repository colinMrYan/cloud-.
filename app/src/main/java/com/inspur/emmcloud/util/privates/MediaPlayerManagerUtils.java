package com.inspur.emmcloud.util.privates;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.interf.CommonCallBack;

/**
 * 音乐播放管理类
 * Created by Administrator on 2015/8/27 0027.
 */
public class MediaPlayerManagerUtils {

    /**
     * 外放模式
     */
    public static final int MODE_SPEAKER = 0;

    /**
     * 耳机模式
     */
    public static final int MODE_HEADSET = 1;

    /**
     * 听筒模式
     */
    public static final int MODE_EARPIECE = 2;

    private static MediaPlayerManagerUtils mediaPlayerManagerUtils;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private PlayCallback callback;
    private Context context;

    private boolean isPause = false;
    private String path;
    private int currentMode = MODE_SPEAKER;

    private boolean isLooping = false;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = null;
    private CommonCallBack wakeLockCallBack;//当视频播放完成时息屏需要释放

    private MediaPlayerManagerUtils() {
        this.context = MyApplication.getInstance();
        initMediaPlayer();
        //初始化音频管理器
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                LogUtils.jasonDebug("focusChange=" + focusChange);
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        if (callback != null) {
                            callback.onPrepared();
                        }
                        if (!isPlaying()) {
                            mediaPlayer.start();
                        }
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS:
                        //暂停操作
                        if (isPlaying()) {
                            stop();
                            if (wakeLockCallBack != null) {
                                wakeLockCallBack.execute();
                            }
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        //暂停操作
                        if (isPlaying()) {
                            pause();
                            if (wakeLockCallBack != null) {
                                wakeLockCallBack.execute();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

        };
    }

    public static MediaPlayerManagerUtils getManager() {
        if (mediaPlayerManagerUtils == null) {
            synchronized (MediaPlayerManagerUtils.class) {
                mediaPlayerManagerUtils = new MediaPlayerManagerUtils();
            }
        }
        return mediaPlayerManagerUtils;
    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setWakeMode(MyApplication.getInstance(), PowerManager.SCREEN_DIM_WAKE_LOCK);
        //保险起见，设置报错监听
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                mediaPlayer.reset();
                return false;
            }
        });
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * 播放音乐
     *
     * @param rawResId raw目录下的文件id
     * @param callback
     */
    public void play(int rawResId, PlayCallback callback) {
        String rawFileUri = "android.resource://" + MyApplication.getInstance().getPackageName() + "/" + rawResId;
        setMediaPlayerLooping(false);
        play(rawFileUri, callback);
    }

    /**
     * 播放音乐
     *
     * @param rawResId raw目录下的文件id
     * @param callback
     */
    public void play(int rawResId, PlayCallback callback, boolean isLooping) {
        String rawFileUri = "android.resource://" + MyApplication.getInstance().getPackageName() + "/" + rawResId;
        setMediaPlayerLooping(isLooping);
        play(rawFileUri, callback);
    }

    /**
     * 设置息屏释放监听
     *
     * @param wakeLockCallBack
     */
    public void setWakeLockReleaseListener(CommonCallBack wakeLockCallBack) {
        this.wakeLockCallBack = wakeLockCallBack;
    }

    /**
     * 播放音乐
     *
     * @param path     音乐文件路径
     * @param callback 播放回调函数
     */
    public void play(String path, final PlayCallback callback) {
        stop();
        this.path = path;
        this.callback = callback;
        try {
            audioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            boolean isBluetoothConnected = !(BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET));
            //耳机模式下直接返回
            if (isBluetoothConnected || MediaPlayerManagerUtils.getManager().getCurrentMode() == MediaPlayerManagerUtils.MODE_HEADSET) {
                changeToHeadsetMode();
            } else {
                changeToSpeakerMode();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.parse(path));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (callback != null) {
                        callback.onPrepared();
                    }
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (callback != null) {
                        callback.onComplete();
                    }
                    if (wakeLockCallBack != null) {
                        wakeLockCallBack.execute();
                    }
                    if (isLooping) {
                        mediaPlayer.start();
                        mediaPlayer.setLooping(true);
                    } else {
                        resetPlayMode();
                        audioManager.abandonAudioFocus(mAudioFocusChangeListener);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPause() {
        return isPause;
    }

    public void pause() {
        if (isPlaying()) {
            isPause = true;
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (isPause) {
            isPause = false;
            mediaPlayer.start();
        }
    }

    /**
     * 获取当前播放模式
     *
     * @return
     */
    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * 切换到听筒模式
     */
    public void changeToEarpieceMode() {
        changeToEarpieceModeNoStop();
//        if (isPlaying()){
//            try {
//                mediaPlayer.stop();
//            } catch (IllegalStateException e) {
//                e.printStackTrace();
//            }
//            changeToEarpieceModeNoStop();
//            play(filePath, callback);
//        }else {
//            changeToEarpieceModeNoStop();
//        }

    }

    public void changeToEarpieceModeNoStop() {
        LogUtils.jasonDebug("changeToEarpieceModeNoStop---------------------");
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION), AudioManager.FX_KEY_CLICK);
//        } else {
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL), AudioManager.FX_KEY_CLICK);
//        }
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode() {
        LogUtils.jasonDebug("changeToHeadsetMode---------------------");
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode() {
        LogUtils.jasonDebug("changeToSpeakerMode---------------------");
        currentMode = MODE_SPEAKER;
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    public void resetPlayMode() {
        LogUtils.jasonDebug("resetPlayMode======================");
        if (audioManager.isWiredHeadsetOn()) {
            changeToHeadsetMode();
        } else {
            changeToSpeakerMode();
        }
    }

    /**
     * 调大音量
     */
    public void raiseVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 调小音量
     */
    public void lowerVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (isPlaying()) {
            try {
                mediaPlayer.stop();
                if (callback != null) {
                    callback.onStop();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        audioManager.abandonAudioFocus(mAudioFocusChangeListener);
    }

    /**
     * 设置是否循环播放
     *
     * @param looping
     */
    public void setMediaPlayerLooping(boolean looping) {
        isLooping = looping;
    }

    /**
     * 设置左右声道的音量
     *
     * @param leftVolume
     * @param rightVolume
     */
    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    /**
     * 调到指定播放位置，以毫秒为单位
     *
     * @param time
     */
    public void setSeekTo(int time) {
        mediaPlayer.seekTo(time);
    }

    /**
     * 是否正在播放
     *
     * @return 正在播放返回true, 否则返回false
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 是否正在播放某个文件
     *
     * @param path
     * @return
     */
    public boolean isPlaying(String path) {
        return isPlaying() && this.path.equals(path);
    }

    /**
     * 播放回调接口
     */
    public interface PlayCallback {

        /**
         * 音乐准备完毕
         */
        void onPrepared();

        /**
         * 音乐播放完成
         */
        void onComplete();

        /**
         * 音乐停止播放
         */
        void onStop();
    }
}