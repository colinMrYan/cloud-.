package com.inspur.emmcloud.basemodule.media.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.basic.Player;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayer;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerObserver;
import com.inspur.emmcloud.basemodule.media.player.model.VideoPlayerImpl;
import com.inspur.emmcloud.basemodule.media.selector.utils.ToastUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;

import static com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
import static com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;
import static com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel.PLAY_ACTION_PRELOAD;

/**
 * Date：2022/7/7
 * Author：wang zhen
 * Description 视频播放器view，仿微信样式
 */
public class VideoPlayerView extends RelativeLayout {
    private static final String TAG = "VideoPlayerView";
    private Context mContext;
    private ViewGroup mRootView; // 根布局
    private TXCloudVideoView mTXCloudVideoView; // 腾讯云视频播放view
    private VideoControlView controlView; // 控制view
    private SuperPlayer mVideoPlayer;
    private int mPlayAction; // 播放模式
    private OnSuperPlayerViewCallback mPlayerViewCallback;  // SuperPlayerView回调
    private SuperPlayerModel mCurrentSuperPlayerModel;  // 当前正在播放的SuperPlayerModel

    public VideoPlayerView(Context context) {
        super(context);
        initialize(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }


    private void initialize(Context context) {
        mContext = context;
        initView();
        initPlayer();
    }

    // 初始化view
    private void initView() {
        mRootView = (ViewGroup) inflate(getContext(), R.layout.video_player_view, this);
        mTXCloudVideoView = (TXCloudVideoView) mRootView.findViewById(R.id.video_view);
        controlView = (VideoControlView) mRootView.findViewById(R.id.control_view);
        controlView.setCallback(mControllerCallback);
        controlView.updateVideoProgress(0, 0);
    }

    // 初始化播放器
    private void initPlayer() {
        mVideoPlayer = new VideoPlayerImpl(mContext, mTXCloudVideoView);
        mVideoPlayer.setObserver(new PlayerObserver());
        // 控制是否循环播放，对外开放
//        mVideoPlayer.setLoop(true);
        controlView.hide();
    }

    /**
     * 播放视频
     */
    public void playWithModel(SuperPlayerModel model) {
        mVideoPlayer.stop();
        mCurrentSuperPlayerModel = model;
        playWithModelInner(mCurrentSuperPlayerModel);
    }

    private void playWithModelInner(SuperPlayerModel model) {
        mPlayAction = mCurrentSuperPlayerModel.playAction;
        if (mPlayAction == PLAY_ACTION_AUTO_PLAY || mPlayAction == PLAY_ACTION_PRELOAD) {
            mVideoPlayer.play(model);
        } else {
            mVideoPlayer.reset();
        }
        controlView.preparePlayVideo(model);

        // 播放本地缓存视频的时候/视频没有fileId的时候(不支持url视频)，右上角不显示下载菜单
    }

    /**
     * 初始化controller回调
     */
    private Player.Callback mControllerCallback = new Player.Callback() {

        @Override
        public void onBackPressed(SuperPlayerDef.PlayerMode playMode) {
            if (mPlayerViewCallback != null) {
                mPlayerViewCallback.onClickCloseBtn();
            }
        }

        @Override
        public void onPause() {
            mVideoPlayer.pause();
        }

        @Override
        public void onResume() {
            handleResume();
        }

        @Override
        public void onSeekTo(int position) {
            mVideoPlayer.seek(position);
        }
    };


    public void handleResume() {
        if (mVideoPlayer.getPlayerState() == SuperPlayerDef.PlayerState.LOADING
                && mPlayAction == PLAY_ACTION_PRELOAD) {
            mVideoPlayer.resume();
        } else if (mVideoPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
            if (mPlayAction == PLAY_ACTION_PRELOAD) {
                mVideoPlayer.resume();
            } else if (mPlayAction == PLAY_ACTION_MANUAL_PLAY) {
                mVideoPlayer.play(mCurrentSuperPlayerModel);
            }
        } else if (mVideoPlayer.getPlayerState() == SuperPlayerDef.PlayerState.END) { //重播
            mVideoPlayer.reStart();
        } else if (mVideoPlayer.getPlayerState() == SuperPlayerDef.PlayerState.PAUSE) { //继续播放
            mVideoPlayer.resume();
        }
    }

    /**
     * resume生命周期回调
     */
    public void onResume() {
        if (mPlayAction == PLAY_ACTION_MANUAL_PLAY && mVideoPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
            return;
        }
        mVideoPlayer.resume();
    }

    /**
     * pause生命周期回调
     */
    public void onPause() {
        if (mPlayAction == PLAY_ACTION_MANUAL_PLAY && mVideoPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
            return;
        }
        mVideoPlayer.pauseVod();
    }

    public SuperPlayerDef.PlayerState getPlayerState() {
        return mVideoPlayer.getPlayerState();
    }

    /**
     * 重置播放器
     */
    public void resetPlayer() {
        stopPlay();
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mVideoPlayer.stop();
    }

    /**
     * 渲染 View onDestroy
     */
    public void destroyPlayerView() {
        mTXCloudVideoView.onDestroy();
    }

    /**
     * 设置超级播放器的回掉
     *
     * @param callback
     */
    public void setPlayerViewCallback(OnSuperPlayerViewCallback callback) {
        mPlayerViewCallback = callback;
    }

    class PlayerObserver extends SuperPlayerObserver {
        @Override
        public void onPlayPrepare() {
            controlView.updatePlayState(SuperPlayerDef.PlayerState.INIT);
            if (mPlayAction != PLAY_ACTION_PRELOAD) {
                controlView.prepareLoading();
            }
        }

        @Override
        public void onPlayBegin(String name) {
            controlView.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
            notifyCallbackPlaying();
        }

        @Override
        public void onPlayPause() {
            controlView.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
        }

        @Override
        public void onPlayStop() {
            controlView.updatePlayState(SuperPlayerDef.PlayerState.END);
            notifyCallbackPlayEnd();
        }

        @Override
        public void onPlayLoading() {
            //预加载模式进行特殊处理
            if (mPlayAction == PLAY_ACTION_PRELOAD) {
//                if (isCallResume) {
//                    controlView.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
//                }
            } else {
                controlView.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
            }
        }

        @Override
        public void onPlayProgress(long current, long duration) {
            controlView.updateVideoProgress(current, duration);
        }

        @Override
        public void onError(int code, String message) {
            ToastUtils.showToast(mContext, message);
//            notifyCallbackPlayError(code);
        }

        @Override
        public void onRcvFirstIframe() {
            super.onRcvFirstIframe();
            controlView.toggleCoverView(false);
        }
    }

    /**
     * 通知播放开始，降低圈复杂度，单独提取成一个方法
     */
    private void notifyCallbackPlaying() {
        if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onPlaying();
        }
    }

    /**
     * 通知播放结束，降低圈复杂度，单独提取成一个方法
     */
    private void notifyCallbackPlayEnd() {
        if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onPlayEnd();
        }
    }

    public void release() {
        if (mVideoPlayer != null) {
            controlView.release();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            release();
        } catch (Throwable e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * SuperPlayerView的回调接口
     */
    public interface OnSuperPlayerViewCallback {

        /**
         * 点击返回按钮
         */
        void onClickCloseBtn();

        /**
         * 开始播放回调
         */
        void onPlaying();

        /**
         * 播放结束
         */
        void onPlayEnd();

        /**
         * 当播放失败的时候回调
         *
         * @param code
         */
        void onError(int code);

    }

    public void setNeedToPause(boolean value) {
        mVideoPlayer.setNeedToPause(value);
    }

    public void setLoop(boolean b) {
        mVideoPlayer.setLoop(b);
    }

    public void setIsAutoPlay(boolean b) {
        mVideoPlayer.setAutoPlay(b);
    }

    public void setControlCanShow(boolean b) {
        controlView.canUseControlView(b);
    }

    public int getVideoDuration() {
        return mVideoPlayer.getVideoDuration();
    }

    public int getVideoWidth() {
        return mVideoPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return mVideoPlayer.getVideoHeight();
    }

    public void showProgressLoading(){
        controlView.prepareLoading();
    }
}
