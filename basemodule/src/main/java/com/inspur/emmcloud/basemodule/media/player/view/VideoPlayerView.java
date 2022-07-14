package com.inspur.emmcloud.basemodule.media.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.basic.Player;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerObserver;
import com.inspur.emmcloud.basemodule.media.player.model.VideoPlayerImpl;
import com.inspur.emmcloud.basemodule.media.selector.utils.ToastUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;

import static com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;
import static com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel.PLAY_ACTION_PRELOAD;

/**
 * Date：2022/7/7
 * Author：wang zhen
 * Description 视频播放器view，仿微信样式
 */
public class VideoPlayerView extends RelativeLayout {
    private Context mContext;
    private ViewGroup mRootView; // 根布局
    private TXCloudVideoView mTXCloudVideoView; // 腾讯云视频播放view
    private VideoControlView controlView; // 控制view
    private VideoPlayerImpl mVideoPlayer;
    private int mPlayAction; // 播放模式
    private OnSuperPlayerViewCallback mPlayerViewCallback;  // SuperPlayerView回调
    private SuperPlayerModel mCurrentSuperPlayerModel;  // 当前正在播放的SuperPlayerModel
    private long mDuration;   // 时长
    private long mProgress;   // 进度

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
        mRootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.video_player_view, null);
        mTXCloudVideoView = (TXCloudVideoView) mRootView.findViewById(R.id.video_view);
        controlView = (VideoControlView) mRootView.findViewById(R.id.control_view);
        controlView.setCallback(mControllerCallback);
    }

    // 初始化播放器
    private void initPlayer() {
        mVideoPlayer = new VideoPlayerImpl(mContext, mTXCloudVideoView);
        mVideoPlayer.setObserver(new PlayerObserver());
        controlView.hide();
    }

    /**
     * 初始化controller回调
     */
    private Player.Callback mControllerCallback = new Player.Callback() {

        @Override
        public void onBackPressed(SuperPlayerDef.PlayerMode playMode) {
            if (mPlayerViewCallback != null) {
                mPlayerViewCallback.onClickSmallReturnBtn();
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


    private void handleResume() {
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
//                    mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
//                    mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
//                }
            } else {
                controlView.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
            }
        }

        @Override
        public void onPlayProgress(long current, long duration) {
            mProgress = current;
            mDuration = duration;
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

    /**
     * SuperPlayerView的回调接口
     */
    public interface OnSuperPlayerViewCallback {

        /**
         * 点击返回按钮
         */
        void onClickSmallReturnBtn();

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
}
