package com.inspur.imp.plugin.audio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AudioPlayer implements OnCompletionListener, OnPreparedListener, OnErrorListener {

    public enum MODE { NONE, PLAY};

    public enum STATE { MEDIA_NONE,
                        MEDIA_STARTING,
                        MEDIA_RUNNING,
                        MEDIA_PAUSED,
                        MEDIA_STOPPED,
                        MEDIA_LOADING
                      };

    private static final String LOG_TAG = "AudioPlayer";

    private static int MEDIA_STATE = 1;
    private static int MEDIA_DURATION = 2;
    private static int MEDIA_POSITION = 3;
    private static int MEDIA_ERROR = 9;

    private static int MEDIA_ERR_NONE_ACTIVE    = 0;
    private static int MEDIA_ERR_ABORTED        = 1;
    private static int MEDIA_ERR_NETWORK        = 2;
    private static int MEDIA_ERR_DECODE         = 3;
    private static int MEDIA_ERR_NONE_SUPPORTED = 4;

    private AudioService handler;
    private String id;                      
    private MODE mode = MODE.NONE;          
    private STATE state = STATE.MEDIA_NONE;

    private String audioFile = null;       
    private float duration = -1;          

    private MediaPlayer player = null;     
    private boolean prepareOnly = true;     
    private int seekOnPrepared = 0;    

    
    public AudioPlayer(AudioService handler, String id, String file) {
        this.handler = handler;
        this.id = id;
        this.audioFile = file;
    }

    public void destroy() {
        if (this.player != null) {
            if ((this.state == STATE.MEDIA_RUNNING) || (this.state == STATE.MEDIA_PAUSED)) {
                this.player.stop();
                this.setState(STATE.MEDIA_STOPPED);
            }
            this.player.release();
            this.player = null;
        }
    }

    /**
     * 播放音乐
     *
     * @param file          
     * 			传递的音乐路径
     */
    public void startPlaying(String file) {
        if (this.readyPlayer(file) && this.player != null) {
            this.player.start();
            this.setState(STATE.MEDIA_RUNNING);
            this.seekOnPrepared = 0; 
        } else {
            this.prepareOnly = false;
        }
    }

    /**
     * 跳到指定的时间点
     * 	
     */
    public void seekToPlaying(int milliseconds) {
        if (this.readyPlayer(this.audioFile)) {
            this.player.seekTo(milliseconds);
        }
        else {
            this.seekOnPrepared = milliseconds;
        }
    }

    /**
     * 暂停音乐播放
     */
    public void pausePlaying() {
    	//如果正在播放就暂停
        if (this.state == STATE.MEDIA_RUNNING && this.player != null) {
            this.player.pause();
            this.setState(STATE.MEDIA_PAUSED);
        }else if(this.state == STATE.MEDIA_STOPPED && this.player != null){
        	this.handler.jsCallback("iAudio.errorCallback","当前状态不能暂停");
        }
        //否则由暂停改为播放
        else {
            this.player.start();
            this.setState(STATE.MEDIA_RUNNING);
        }
    }
    
    /**
     * 重新播放
     */
    public void resetPlaying() {
        if (this.player != null) {
            this.player.seekTo(0);
            this.setState(STATE.MEDIA_RUNNING);
            this.player.start();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlaying() {
        if ((this.state == STATE.MEDIA_RUNNING) || (this.state == STATE.MEDIA_PAUSED)) {
            this.player.pause();
            this.player.seekTo(0);
            this.setState(STATE.MEDIA_STOPPED);
        }
        else {
        	this.handler.jsCallback("iAudio.errorCallback","当前状态不能停止");
        }
    }

    /**
     * 音乐播放完毕
     */
    public void onCompletion(MediaPlayer player) {
        this.setState(STATE.MEDIA_STOPPED);
    }


    /**
     * 判断该音乐是否是网络地址
     */
    public boolean isStreaming(String file) {
        if (file.contains("http://") || file.contains("https://")) {
            return true;
        }
        else {
            return false;
        }
    }

    
    public void onPrepared(MediaPlayer player) {
        this.player.setOnCompletionListener(this);
        this.seekToPlaying(this.seekOnPrepared);
        if (!this.prepareOnly) {
            this.player.start();
            this.setState(STATE.MEDIA_RUNNING);
            this.seekOnPrepared = 0; 
        } else {
            this.setState(STATE.MEDIA_STARTING);
        }
        this.duration = getDurationInSeconds();
        this.prepareOnly = true;
    }

    private float getDurationInSeconds() {
        return (this.player.getDuration() / 1000.0f);
    }

    public int getState() {
        return this.state.ordinal();
    }

    private void setState(STATE state) {
        this.state = state;
    }

   
    private void setMode(MODE mode) {
        this.mode = mode;
    }

    private boolean playMode() {
        switch(this.mode) {
        case NONE:
            this.setMode(MODE.PLAY);
            break;
        case PLAY:
            break;
        }
        return true;
    }

    private boolean readyPlayer(String file) {
        if (playMode()) {
            switch (this.state) {
                case MEDIA_NONE:
                    if (this.player == null) {
                        this.player = new MediaPlayer();
                    }
                    try {
                        this.loadAudioFile(file);
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                    return false;
                case MEDIA_LOADING:
                    this.prepareOnly = false;
                    return false;
                case MEDIA_STARTING:
                case MEDIA_RUNNING:
                case MEDIA_PAUSED:
                    return true;
                case MEDIA_STOPPED:
                    if (this.audioFile.compareTo(file) == 0) {
                        player.seekTo(0);
                        player.pause();
                        return true;
                    } else {
                        this.player.reset();
                        try {
                            this.loadAudioFile(file);
                        } catch (Exception e) {
                        	e.printStackTrace();
                        }
                        return false;
                    }
                default:
            }
        }
        return false;
    }

    /**
     * 通过路径判断该地址是网络还是本地还是其他
     * @param file
     * 			文件的路径
     * 
     */
     
    private void loadAudioFile(String file) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        if (this.isStreaming(file)) {
            this.player.setDataSource(file);
            this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.setMode(MODE.PLAY);
            this.setState(STATE.MEDIA_STARTING);
            this.player.setOnPreparedListener(this);
            this.player.prepareAsync();
        }
        else {
            if (file.startsWith("/android_asset/")) {
                String f = file.substring(15);
                android.content.res.AssetFileDescriptor fd = this.handler.getActivity().getAssets().openFd(f);
                this.player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            }
            else {
                File fp = new File(file);
                if (fp.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    this.player.setDataSource(fileInputStream.getFD());
                    fileInputStream.close();
                }
                else {
                    this.player.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/" + file);
                }
            }
                this.setState(STATE.MEDIA_STARTING);
                this.player.setOnPreparedListener(this);
                this.player.prepare();
                this.duration = getDurationInSeconds();
            }
    }

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if(this.player!=null){
			this.handler.jsCallback("iAudio.errorCallback",""+what+" "+extra+"错误");
		}
		
		return false;
	}

}
