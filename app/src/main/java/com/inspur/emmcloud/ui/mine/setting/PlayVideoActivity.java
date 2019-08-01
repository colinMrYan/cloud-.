package com.inspur.emmcloud.ui.mine.setting;

import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

public class PlayVideoActivity extends BaseActivity {
    VideoView videoView;

    @Override
    public void onCreate() {
        Uri uri = getIntent().getData();
        videoView = findViewById(R.id.video_view);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.layout_play_video;
    }
}
