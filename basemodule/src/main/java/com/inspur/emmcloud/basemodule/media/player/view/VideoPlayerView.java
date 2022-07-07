package com.inspur.emmcloud.basemodule.media.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;

/**
 * Date：2022/7/7
 * Author：wang zhen
 * Description 视频播放器view，仿微信样式
 */
public class VideoPlayerView extends RelativeLayout {
    private Context mContext;
    private ViewGroup mRootView; // 根布局

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
    }

    // 初始化播放器
    private void initPlayer() {

    }
}
