package com.inspur.emmcloud.web.plugin.video;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.R2;
import com.inspur.emmcloud.web.plugin.video.view.CustomMediaController;
import com.inspur.emmcloud.web.plugin.video.view.CustomVideoView;

import butterknife.BindView;
import butterknife.ButterKnife;

@Route(path = "/web/VideoActivity")
public class VideoPlayActivity extends Activity implements CustomVideoView.VideoViewCallback {
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";
    //    private static final String VIDEO_URL = "https://vdse.bdstatic.com//ce44ed85d9608aaf01970e496fbc63f2?authorization=bce-auth-v1%2Ffb297a5cc0fb434c971b8fa103e8dd7b%2F2017-05-11T09%3A02%3A31Z%2F-1%2F%2F1a46fa9e9c912d194a415dc7269541e05105a0aab7777de5a9e96b8a7bdb4997";
    private String VIDEO_URL = Environment.getExternalStorageDirectory().getPath() + "/myvideo/V096752.mp4";
    CustomVideoView mVideoView;
    CustomMediaController mMediaController;
    View mVideoLayout;
    @BindView(R2.id.rl_header)
    View headLayout;
    private int mSeekPosition;
    private int cachedHeight;
    private boolean isFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity_video_play);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        VIDEO_URL = getIntent().getStringExtra("path");
        mVideoLayout = findViewById(R.id.video_layout);
        mVideoView = (CustomVideoView) findViewById(R.id.videoView);
        mMediaController = (CustomMediaController) findViewById(R.id.media_controller);
        mVideoView.setMediaController(mMediaController);
        setVideoAreaSize();
        mVideoView.setVideoViewCallback(this);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null/* && mVideoView.isPlaying()*/) {
            mSeekPosition = mVideoView.getCurrentPosition();
            mVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
            if (mSeekPosition > 0) {
                mVideoView.seekTo(mSeekPosition);
            }
        }
    }

    /**
     * 置视频区域大小
     */
    private void setVideoAreaSize() {
        mVideoLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = mVideoLayout.getWidth();
                cachedHeight = (int) (width * 405f / 720f);
//                cachedHeight = (int) (width * 3f / 4f);
//                cachedHeight = (int) (width * 9f / 16f);
                ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
                videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                videoLayoutParams.height = cachedHeight;
                mVideoLayout.setLayoutParams(videoLayoutParams);

                mVideoView.setVideoPath(VIDEO_URL);
                mVideoView.requestFocus();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        mSeekPosition = outState.getInt(SEEK_POSITION_KEY);
    }

    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);
        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onPause(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mVideoView.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibt_back) {
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("zhang", "onConfigurationChanged: isFullscreen = " + isFullscreen);
        headLayout.setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
        if (isFullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
