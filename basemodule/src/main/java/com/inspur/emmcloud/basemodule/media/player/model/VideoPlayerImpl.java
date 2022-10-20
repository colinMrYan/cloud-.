package com.inspur.emmcloud.basemodule.media.player.model;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.github.zafarkhaja.semver.Version;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.media.player.basic.PlayerGlobalConfig;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayer;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.interfaces.ISuperPlayerListener;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXBitrateItem;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXPlayerGlobalSetting;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.jsoup.Connection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date：2022/7/11
 * Author：wang zhen
 * Description
 */
public class VideoPlayerImpl implements SuperPlayer, ITXVodPlayListener {
    private static final String TAG = "VideoPlayerImpl";
    private Context mContext;
    private TXCloudVideoView mVideoView;
    private TXVodPlayer mVodPlayer;       // 点播播放器
    private TXVodPlayConfig mVodPlayConfig;   // 点播播放器配置
    private SuperPlayerObserver mObserver;
    private boolean mChangeHWAcceleration;  // 切换硬解后接收到第一个关键帧前的标记位

    private SuperPlayerDef.PlayerState mCurrentPlayState = SuperPlayerDef.PlayerState.INIT;  // 当前播放状态
    private float mStartPos;  // 视频开始播放时间
    private int mSeekPos;  // 记录切换硬解时的播放时间
    private boolean isPrepared = false;
    private boolean isNeedResume = false;
    private boolean mNeedToPause = false;
    private ISuperPlayerListener mSuperPlayerListener;
    private int mPlayAction;  //播放模式
    private SuperPlayerModel mCurrentModel;  // 当前播放的model
    private String mCurrentPlayVideoURL;    // 当前播放的URL
    private boolean mIsAutoPlay = true;     // 是否自动播放
    private int videoHeight;
    private int videoWidth;
    private int videoDuration;

    public VideoPlayerImpl(Context context, TXCloudVideoView videoView) {
        initialize(context, videoView);
    }

    private void initialize(Context context, TXCloudVideoView videoView) {
        mContext = context;
        mVideoView = videoView;
        initVodPlayer(mContext);
    }

    /**
     * 初始化点播播放器
     *
     * @param context
     */
    private void initVodPlayer(Context context) {
        mVodPlayer = new TXVodPlayer(context);
        PlayerGlobalConfig config = PlayerGlobalConfig.getInstance();
        mVodPlayConfig = new TXVodPlayConfig();

        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir != null) {
            TXPlayerGlobalSetting.setCacheFolderPath(sdcardDir.getPath() + "/txcache");
        }
        TXPlayerGlobalSetting.setMaxCacheSize(200);
        setUrlHead();
        mVodPlayer.setConfig(mVodPlayConfig);
        mVodPlayer.setRenderMode(config.renderMode);
        mVodPlayer.setVodListener(this);
        mVodPlayer.enableHardwareDecode(config.enableHWAcceleration);
        mVodPlayer.setRate(config.playRate);
        mVodPlayer.setMute(config.mute);
        mVodPlayer.setMirror(config.mirror);
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle param) {
        switch (event) {
            case TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED://视频播放开始
                onVodPlayPrepared();
                videoHeight = txVodPlayer.getHeight();
                videoWidth = txVodPlayer.getWidth();
                videoDuration = (int) txVodPlayer.getDuration();
                break;
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME:
                Log.i(TAG, "PLAY_EVT_RCV_FIRST_I_FRAME");
                if (mNeedToPause) {
                    return;
                }
                if (mChangeHWAcceleration) { //切换软硬解码器后，重新seek位置
                    Log.i(TAG, "seek pos:" + mSeekPos);
                    seek(mSeekPos);
                    mChangeHWAcceleration = false;
                }
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                mObserver.onRcvFirstIframe();
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_END:
                Log.i(TAG, "PLAY_EVT_PLAY_END");
                updatePlayerState(SuperPlayerDef.PlayerState.END);
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS:
                int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
                int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
                if (duration != 0) {
                    updatePlayProgress(progress / 1000, duration / 1000);
                }
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                Log.i(TAG, "PLAY_EVT_PLAY_LOADING");
                updatePlayerState(SuperPlayerDef.PlayerState.LOADING);
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN:
                if (mNeedToPause) {
                    pause();
                    return;
                }
                updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
                break;
            default:
                break;
        }
        if (event < 0) {// 播放点播文件失败
            mVodPlayer.stopPlay(true);
            updatePlayerState(SuperPlayerDef.PlayerState.PAUSE);
            onError(4001, param.getString(TXLiveConstants.EVT_DESCRIPTION));
        }
        if (mSuperPlayerListener != null) {
            mSuperPlayerListener.onVodPlayEvent(txVodPlayer, event, param);
        }
    }

    private void onError(int code, String message) {
        if (mObserver != null) {
            mObserver.onError(code, message);
        }
    }

    private void onVodPlayPrepared() {
        Log.i(TAG, "PLAY_EVT_VOD_PLAY_PREPARED");
        isPrepared = true;
        if (mNeedToPause) {
            pauseVod();
            return;
        }
        if (isNeedResume) {
            mVodPlayer.resume();
        }
    }

    /**
     * 更新播放进度
     *
     * @param current  当前播放进度(秒)
     * @param duration 总时长(秒)
     */
    private void updatePlayProgress(long current, long duration) {
        if (mObserver != null) {
            mObserver.onPlayProgress(current, duration);
        }
    }

    /**
     * 更新播放状态
     *
     * @param playState
     */
    private void updatePlayerState(SuperPlayerDef.PlayerState playState) {
        mCurrentPlayState = playState;
        if (mObserver == null) {
            return;
        }
        switch (playState) {
            case INIT:
                mObserver.onPlayPrepare();
                break;
            case PLAYING:
                mObserver.onPlayBegin("");
                break;
            case PAUSE:
                mObserver.onPlayPause();
                break;
            case LOADING:
                mObserver.onPlayLoading();
                break;
            case END:
                mObserver.onPlayStop();
                break;
        }
    }

    /**
     * 播放视频
     *
     * @param model
     */
    public void playWithModel(SuperPlayerModel model) {
        reset();
//        mCurrentProtocol = null;//置空
        if (!TextUtils.isEmpty(model.url)
                || (model.multiURLs != null
                && !model.multiURLs.isEmpty())) {
            playWithUrl(model);
        }
    }

    private void playWithUrl(SuperPlayerModel model) {
        String videoURL = null;
        if (!TextUtils.isEmpty(model.url)) { // 传统URL模式播放
            videoURL = model.url;
        }
        if (TextUtils.isEmpty(videoURL)) {
            onError(20001, "播放视频失败，播放链接为空");
            return;
        }
        // 点播播放器：播放点播文件
        mVodPlayer.setPlayerView(mVideoView);
        playVodURL(videoURL);
        updatePlayProgress(0, model.duration);
    }

    /**
     * 播放点播url
     */
    private void playVodURL(String url) {
        if (url == null || "".equals(url)) {
            return;
        }
        mCurrentPlayVideoURL = url;
        // 不支持m3u8
//        if (url.contains(".m3u8")) {
//            mIsMultiBitrateStream = true;
//        } else {
//            mIsMultiBitrateStream = false;
//        }
        if (mVodPlayer != null) {
            mVodPlayer.setStartTime(mStartPos);
            mVodPlayer.setAutoPlay(mIsAutoPlay);
            if (mPlayAction == SuperPlayerModel.PLAY_ACTION_AUTO_PLAY || mPlayAction == SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY) {
                mVodPlayer.setAutoPlay(true);
            } else if (mPlayAction == SuperPlayerModel.PLAY_ACTION_PRELOAD) {
                mVodPlayer.setAutoPlay(false);
                mPlayAction = SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
            }
            mVodPlayer.setVodListener(this);
            mVodPlayer.startPlay(url);
        }
    }

    private void resetPlayer() {
        isPrepared = false;
        isNeedResume = false;
        if (mVodPlayer != null) {
            mVodPlayer.setVodListener(null);
            mVodPlayer.stopPlay(true);
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {
        if (mSuperPlayerListener != null) {
            mSuperPlayerListener.onVodNetStatus(txVodPlayer, bundle);
        }
    }

    @Override
    public void play(SuperPlayerModel model) {
        mPlayAction = model.playAction;
        mCurrentModel = model;
        playWithModel(model);
    }

    @Override
    public void reStart() {
        if (mCurrentPlayVideoURL != null) {
            playVodURL(mCurrentPlayVideoURL);
        }
    }

    @Override
    public void pause() {
        mVodPlayer.pause();
        updatePlayerState(SuperPlayerDef.PlayerState.PAUSE);
    }

    @Override
    public void pauseVod() {
        mVodPlayer.pause();
        updatePlayerState(SuperPlayerDef.PlayerState.PAUSE);
    }

    @Override
    public void resume() {
        isNeedResume = true;
        if (isPrepared) {
            mVodPlayer.resume();
        }
        updatePlayerState(SuperPlayerDef.PlayerState.PLAYING);
    }

    @Override
    public void stop() {
        resetPlayer();
        updatePlayerState(SuperPlayerDef.PlayerState.END);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void reset() {
        resetPlayer();
        updatePlayerState(SuperPlayerDef.PlayerState.INIT);
    }

    @Override
    public void revertSettings() {
        mVodPlayer.setMirror(false);
        mVodPlayer.setRate(1.0f);
    }

    @Override
    public void enableHardwareDecode(boolean enable) {

    }

    @Override
    public void setPlayerView(TXCloudVideoView videoView) {
        mVideoView = videoView;
        mVodPlayer.setPlayerView(videoView);
    }

    @Override
    public void seek(int position) {
        if (mVodPlayer != null) {
            mVodPlayer.seek(position);
            if (!mVodPlayer.isPlaying()) {
                mVodPlayer.resume();
            }
        }
    }

    @Override
    public void setRate(float speedLevel) {

    }

    @Override
    public void setRenderMode(int mode) {
        mVodPlayer.setRenderMode(mode);
    }

    @Override
    public String getPlayURL() {
        return mCurrentPlayVideoURL;
    }

    @Override
    public SuperPlayerDef.PlayerState getPlayerState() {
        return mCurrentPlayState;
    }

    @Override
    public void setObserver(SuperPlayerObserver observer) {
        mObserver = observer;
    }

    @Override
    public void setSuperPlayerListener(ISuperPlayerListener superPlayerListener) {
        mSuperPlayerListener = superPlayerListener;
    }

    @Override
    public void setLoop(boolean isLoop) {
        mVodPlayer.setLoop(isLoop);
    }

    @Override
    public void setStartTime(float startPos) {
        this.mStartPos = startPos;
        mVodPlayer.setStartTime(startPos);
    }

    @Override
    public void setAutoPlay(boolean isAutoPlay) {
        this.mIsAutoPlay = isAutoPlay;
        mVodPlayer.setAutoPlay(isAutoPlay);
    }

    @Override
    public void setNeedToPause(boolean value) {
        mNeedToPause = value;
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }

    @Override
    public int getVideoDuration() {
        return videoDuration;
    }

    public void setUrlHead() {
        String versionValue = AppUtils.getVersion(BaseApplication.getInstance());
        try {
            Version version = Version.valueOf(versionValue);
            versionValue = version.getNormalVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
        headers.put("Authorization", BaseApplication.getInstance().getToken());
//        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Content-Disposition", "inline");
        headers.put("User-Agent", "Android/" + AppUtils.getReleaseVersion() + "("
                + AppUtils.GetChangShang() + " " + AppUtils.GetModel()
                + ") " + "CloudPlus_Phone/"
                + versionValue);
//        headers.put("User-Agent","iOS/15.3.1(Apple iPhone9,1) CloudPlus_Phone/5.0.0");
        mVodPlayConfig.setHeaders(headers);
    }
}
