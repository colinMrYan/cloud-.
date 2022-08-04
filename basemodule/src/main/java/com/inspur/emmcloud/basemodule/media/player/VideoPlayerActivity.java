package com.inspur.emmcloud.basemodule.media.player;

import android.view.WindowManager;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;
import com.inspur.emmcloud.basemodule.media.player.view.VideoPlayerView;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;

import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.VIDEO_PATH;

/**
 * Date：2022/7/15
 * Author：wang zhen
 * Description 视频播放activity，单个视频播放
 */
public class VideoPlayerActivity extends BaseFragmentActivity implements NotSupportLand, VideoPlayerView.OnSuperPlayerViewCallback {
    private VideoPlayerView videoPlayerView; // 播放器核心view
    private boolean mIsManualPause = false; // 手动暂停
    private String recordUrl;

    @Override
    public void onCreate() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).fullScreen(true).init();
        setContentView(R.layout.activity_video_player);
        recordUrl = getIntent().getStringExtra(VIDEO_PATH);
        initView();
        initData();
    }

    private void initData() {
//        if (recordUrl.startsWith("https:")) {
            SuperPlayerModel model = new SuperPlayerModel();
            model.url = recordUrl;
            videoPlayerView.playWithModel(model);
//        } else {
//            videoPlayerView.showProgressLoading();
//            getUrl();
//        }


//        SuperPlayerModel model = new SuperPlayerModel();
//        model.url = recordUrl;
//        model.url = "https://ecmcloud.oss-cn-beijing.aliyuncs.com/ossdemo/aaa6f73c6879cc84284d40257795648d.mp4";
//        model.url = "https://chat-test.inspuronline.com/chat/3a7/dfa/11-/3a7dfa11-16d3-4ec2-891a-05765b194310?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=chat-test-minio-app%2F20220729%2Fap-northeast-1%2Fs3%2Faws4_request&X-Amz-Date=20220729T072343Z&X-Amz-Expires=900&X-Amz-Signature=697a39718baeebd3863a204d0185fcb4a56d7c72d369389832adb92bf6d88bb0&X-Amz-SignedHeaders=host&response-content-disposition=inline&response-content-type=video%2Fmp4";
//        videoPlayerView.playWithModel(model);
    }

    private void getUrl() {
        BaseModuleApiService appAPIService = new BaseModuleApiService(this);
        appAPIService.setAPIInterface(new WebService());
        appAPIService.getVideoUrl(recordUrl);
    }

    class WebService extends BaseModuleAPIInterfaceInstance {
        @Override
        public void returnVideoSuccess(final String realUrl) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SuperPlayerModel model = new SuperPlayerModel();
                    model.url = realUrl;
                    videoPlayerView.playWithModel(model);

                }
            });
        }

        @Override
        public void returnVideoFail(String error, int errorCode) {
            if (errorCode != 302) {
                videoPlayerView.destroyPlayerView();
            }
        }

    }

    private void initView() {
        videoPlayerView = (VideoPlayerView) findViewById(R.id.video_player_view);
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
