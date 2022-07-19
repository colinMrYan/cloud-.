package com.inspur.emmcloud.basemodule.media.player;

import android.view.WindowManager;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.basic.PlayerGlobalConfig;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;
import com.inspur.emmcloud.basemodule.media.player.view.VideoPlayerView;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.tencent.rtmp.TXLiveConstants;

import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.VIDEO_PATH;

/**
 * Date：2022/7/19
 * Author：wang zhen
 * Description 录制视频时预览
 */
public class VideoPreviewActivity extends BaseFragmentActivity implements NotSupportLand, VideoPlayerView.OnSuperPlayerViewCallback {
    private VideoPlayerView videoPlayerView; // 播放器核心view
    private boolean mIsManualPause = false; // 手动暂停

    @Override
    public void onCreate() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).fullScreen(true).init();
        setContentView(R.layout.activity_video_player);

        initView();
        initData();
    }

    private void initData() {
        String recordUrl = getIntent().getStringExtra(VIDEO_PATH);
        SuperPlayerModel model = new SuperPlayerModel();
//        model.url = "http://vfx.mtime.cn/Video/2021/07/10/mp4/210710171112971120.mp4";
        model.url = recordUrl;
        model.placeholderImage = "http://xiaozhibo-10055601.file.myqcloud.com/coverImg.jpg";
        videoPlayerView.playWithModel(model);
    }

    private void initView() {
        videoPlayerView = (VideoPlayerView) findViewById(R.id.video_player_view);
        videoPlayerView.setControlCanShow(false);
        videoPlayerView.setPlayerViewCallback(this);
    }

    @Override
    public void onClickCloseBtn() {
        videoPlayerView.resetPlayer();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
