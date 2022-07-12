package com.inspur.emmcloud.basemodule.media.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.basic.Player;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.tencent.rtmp.ui.TXCloudVideoView;

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
//
//        removeAllViews();
//        mRootView.removeView(mTXCloudVideoView);
//        mRootView.removeView(controlView);

        addView(mTXCloudVideoView);
        addView(controlView);

    }

    // 初始化播放器
    private void initPlayer() {


    }

    /**
     * 初始化controller回调
     */
    private Player.Callback mControllerCallback = new Player.Callback() {

        @Override
        public void onBackPressed(SuperPlayerDef.PlayerMode playMode) {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onResume() {

        }

        @Override
        public void onSeekTo(int position) {

        }
    };
}
