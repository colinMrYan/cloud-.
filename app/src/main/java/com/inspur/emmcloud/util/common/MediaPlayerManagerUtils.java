package com.inspur.emmcloud.util.common;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import com.inspur.emmcloud.MyApplication;

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
    private String filePath;

    private int currentMode = MODE_SPEAKER;

    public static MediaPlayerManagerUtils getManager() {
        if (mediaPlayerManagerUtils == null) {
            synchronized (MediaPlayerManagerUtils.class) {
                mediaPlayerManagerUtils = new MediaPlayerManagerUtils();
            }
        }
        return mediaPlayerManagerUtils;
    }

    private MediaPlayerManagerUtils() {
        this.context = MyApplication.getInstance();
        initMediaPlayer();
        initAudioManager();
    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
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
     * 初始化音频管理器
     */
    private void initAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(true);            //默认为扬声器播放
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

    /**
     * 播放音乐
     *
     * @param path     音乐文件路径
     * @param callback 播放回调函数
     */
    public void play(String path, final PlayCallback callback) {
        if (mediaPlayer.isPlaying()&&callback != null){
            callback.onStop();
        }
        this.filePath = path;
        this.callback = callback;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.parse(path));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (callback != null){
                        callback.onPrepared();
                    }
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (callback != null){
                        callback.onComplete();
                    }
                    resetPlayMode();
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

    public void changeToEarpieceModeNoStop(){
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
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
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode() {
//        if (isPlaying()) {
//            try {
//                mediaPlayer.stop();
//            } catch (IllegalStateException e) {
//                e.printStackTrace();
//            }
//            currentMode = MODE_SPEAKER;
//            audioManager.setSpeakerphoneOn(true);
//            play(filePath, callback);
//        }else {
//            currentMode = MODE_SPEAKER;
//            audioManager.setSpeakerphoneOn(true);
//        }
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }

    public void resetPlayMode() {
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
                if (callback != null){
                    callback.onStop();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否正在播放
     *
     * @return 正在播放返回true, 否则返回false
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}