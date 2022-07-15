package com.inspur.emmcloud.basemodule.media.player;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.view.VideoPlayerView;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;

/**
 * Date：2022/7/15
 * Author：wang zhen
 * Description 视频播放activity，单个视频播放
 */
public class VideoPlayerActivity extends BaseFragmentActivity implements NotSupportLand, VideoPlayerView.OnSuperPlayerViewCallback {
    private VideoPlayerView videoPlayerView; // 播放器核心view
    private boolean mIsManualPause = false; // 手动暂停
    private String url; // 测试地址

    @Override
    public void onCreate() {
        setContentView(R.layout.activity_video_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).fullScreen(true).init();
        initView();
        initData();
    }

    private void initData() {
        url = "http://vfx.mtime.cn/Video/2021/07/10/mp4/210710171112971120.mp4";

    }

    private void initView() {
        videoPlayerView = (VideoPlayerView) findViewById(R.id.video_player_view);
        videoPlayerView.setPlayerViewCallback(this);
    }

    @Override
    public void onClickCloseBtn() {
        videoPlayerView.resetPlayer();
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PLAYING
                || videoPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PAUSE) {
            if (!mIsManualPause) {
                videoPlayerView.onResume();
            }
        }
        videoPlayerView.setNeedToPause(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PAUSE) {
            mIsManualPause = true;
        } else {
            mIsManualPause = false;
        }
        videoPlayerView.onPause();
        videoPlayerView.setNeedToPause(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayerView.release();
        videoPlayerView.resetPlayer();
        videoPlayerView.destroyPlayerView();
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPlayEnd() {

    }

    @Override
    public void onError(int code) {

    }
}
