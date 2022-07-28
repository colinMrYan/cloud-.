package com.inspur.emmcloud.basemodule.media.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.basic.AbsPlayer;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;

/**
 * Date：2022/7/7
 * Author：wang zhen
 * Description 视频控制view：进度条，播放，暂停，进度条等
 */
public class VideoControlView extends AbsPlayer implements View.OnClickListener, PointSeekBar.OnSeekBarChangeListener {
    private boolean isShowing; // 自身是否可见
    private LinearLayout playControlLl; // 底部播放控制布局：进度条，时间，暂停等
    private ImageView pauseIv; // 底部暂停按钮
    private TextView currentTv; // 当前播放时间
    private TextView durationTv; // 总播放时间
    private PointSeekBar playSb; // 进度条
    private ProgressBar playPb; // 圆形加载条
    private ImageView resumeIv; // 继续播放按钮
    private ImageView coverIv; // 封面图
    private long mLastClickTime; // 上次点击事件的时间
    private ImageView videoCloseIv; // 关闭按钮
    private SuperPlayerDef.PlayerState mCurrentPlayState = SuperPlayerDef.PlayerState.END;
    private long mDuration; // 视频总时长
    private long mProgress; // 当前播放进度
    private boolean isDestroy = false; // Activity 是否被销毁
    private GestureDetector mGestureDetector;
    private boolean mIsChangingSeekBarProgress; // 进度条是否正在拖动，避免SeekBar由于视频播放的update而跳动
    private boolean canUseControlView = true; // controlView 是否可用，默认可用

    public VideoControlView(Context context) {
        super(context);
        initView(context);
    }

    public VideoControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 初始化控件
     */
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.video_control_view, this);
        playControlLl = (LinearLayout) findViewById(R.id.ll_play);
        pauseIv = (ImageView) findViewById(R.id.iv_pause);
        currentTv = (TextView) findViewById(R.id.tv_current);
        durationTv = (TextView) findViewById(R.id.tv_duration);
        playSb = (PointSeekBar) findViewById(R.id.sb_play);
        playPb = (ProgressBar) findViewById(R.id.pb_player);
        resumeIv = (ImageView) findViewById(R.id.iv_resume);
        coverIv = (ImageView) findViewById(R.id.iv_cover);
        videoCloseIv = (ImageView) findViewById(R.id.iv_video_close);

        playSb.setProgress(0);
        playSb.setMax(100);
        videoCloseIv.setOnClickListener(this);
        pauseIv.setOnClickListener(this);
        resumeIv.setOnClickListener(this);
        playSb.setOnSeekBarChangeListener(this);

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggle();
                return false;
            }
        });
    }

    public void preparePlayVideo(SuperPlayerModel superPlayerModel) {
        if (!isDestroy) {
            if (superPlayerModel.coverPictureUrl != null) {
//                Glide.with(getContext()).load(superPlayerModel.coverPictureUrl)
//                        .into(coverIv);
                Glide.with(getContext())
                        .setDefaultRequestOptions(new RequestOptions().frame(0))
                        .load(superPlayerModel.url)
                        .into(coverIv);
            }
        }
        toggleView(coverIv, true);
        pauseIv.setImageResource(R.drawable.ic_vod_play_center);
        updateVideoProgress(0, superPlayerModel.duration);
        playSb.setEnabled(superPlayerModel.playAction != SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY);
        updateStartUI();
    }

    // 开始时的UI
    private void updateStartUI() {
        toggleView(resumeIv, true);
        toggleView(playPb, false);
    }

    /**
     * 显示控件
     */
    @Override
    public void show() {
        isShowing = true;
        playControlLl.setVisibility(View.VISIBLE);
        videoCloseIv.setVisibility(View.VISIBLE);

    }

    /**
     * 隐藏控件
     */
    @Override
    public void hide() {
        isShowing = false;
        playControlLl.setVisibility(View.GONE);
        videoCloseIv.setVisibility(View.GONE);
    }

    // 封面图可见性
    public void toggleCoverView(boolean isVisible) {
        toggleView(coverIv, isVisible);
    }

    public void prepareLoading() {
        toggleView(playPb, true);
        toggleView(resumeIv, false);
    }

    // 更新用户状态
    @Override
    public void updatePlayState(SuperPlayerDef.PlayerState playState) {
        switch (playState) {
            case INIT:
                pauseIv.setImageResource(R.drawable.ic_vod_play_center);
                break;
            case PLAYING:
                playSb.setEnabled(true);
                pauseIv.setImageResource(R.drawable.ic_play_pause_normal);
                toggleView(resumeIv, false);
                toggleView(playPb, false);
                break;
            case LOADING:
                playSb.setEnabled(true);
                pauseIv.setImageResource(R.drawable.ic_play_pause_normal);
                toggleView(playPb, true);
                break;
            case PAUSE:
                pauseIv.setImageResource(R.drawable.ic_vod_play_center);
                toggleView(playPb, false);
                toggleView(resumeIv, true);
                break;
            case END:
                pauseIv.setImageResource(R.drawable.ic_vod_play_center);
                toggleView(playPb, false);
                break;
        }
        mCurrentPlayState = playState;
    }

    /**
     * 更新视频播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    @Override
    public void updateVideoProgress(long current, long duration) {
        mProgress = current < 0 ? 0 : current;
        mDuration = duration < 0 ? 0 : duration;
        currentTv.setText(formattedTime(mProgress));
        float percentage = mDuration > 0 ? ((float) mProgress / (float) mDuration) : 1.0f;
        if (mProgress == 0) {
            percentage = 0;
        }
        if (percentage >= 0 && percentage <= 1) {
            int progress = Math.round(percentage * playSb.getMax());
            if (!mIsChangingSeekBarProgress) {
                playSb.setProgress(progress);
            }
            durationTv.setText(formattedTime(mDuration));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 切换自身的可见性
     */
    private void toggle() {
        if (!canUseControlView) {
            return;
        }
        if (isShowing) {
            hide();
        } else {
            show();
            if (mHideViewRunnable != null) {
                removeCallbacks(mHideViewRunnable);
                postDelayed(mHideViewRunnable, 7000);
            }
        }
    }

    /**
     * 切换播放状态
     * <p>
     * 点击播放/暂停按钮会触发此方法
     */
    private void togglePlayState() {
        switch (mCurrentPlayState) {
            case INIT:
            case PAUSE:
            case END:
                if (mControllerCallback != null) {
                    mControllerCallback.onResume();
                }
                break;
            case PLAYING:
            case LOADING:
                if (mControllerCallback != null) {
                    mControllerCallback.onPause();
                }
                break;
        }
        show();
    }

    @Override
    public void onClick(View view) {
        if (System.currentTimeMillis() - mLastClickTime < 300) { //限制点击频率
            return;
        }
        mLastClickTime = System.currentTimeMillis();
        int id = view.getId();
        if (id == R.id.iv_video_close) {
            if (mControllerCallback != null) {
                mControllerCallback.onBackPressed(SuperPlayerDef.PlayerMode.WINDOW);
            }
        } else if (id == R.id.iv_pause || id == R.id.iv_resume) {
            togglePlayState();
        }
    }


    // 播放进度条相关
    @Override
    public void onProgressChanged(PointSeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            float percentage = ((float) progress) / seekBar.getMax();
            float currentTime = (mDuration * percentage);
            currentTv.setText(formattedTime((long) currentTime));
        }
    }

    @Override
    public void onStartTrackingTouch(PointSeekBar seekBar) {
        removeCallbacks(mHideViewRunnable);
    }

    @Override
    public void onStopTrackingTouch(PointSeekBar seekBar) {
        int curProgress = seekBar.getProgress();
        int maxProgress = seekBar.getMax();
        if (curProgress >= 0 && curProgress <= maxProgress) {
            float percentage = ((float) curProgress) / maxProgress;
            int position = (int) (mDuration * percentage);
            if (mControllerCallback != null) {
                mControllerCallback.onSeekTo(position);
            }
        }
        postDelayed(mHideViewRunnable, 7000);
    }

    @Override
    public void release() {
        isDestroy = true;
    }

    // 控制整个控件可见性
    public void canUseControlView(boolean visible) {
        this.canUseControlView = visible;
    }
}
