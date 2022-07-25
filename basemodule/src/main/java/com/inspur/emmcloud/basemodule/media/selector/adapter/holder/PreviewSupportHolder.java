package com.inspur.emmcloud.basemodule.media.selector.adapter.holder;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;
import com.inspur.emmcloud.basemodule.media.player.view.VideoPlayerView;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.photoview.PhotoView;


/**
 * Date：2022/6/30
 * Author：wang zhen
 * Description 仿照RecyclerView holder 用于ViewPager
 */
public class PreviewSupportHolder {

    public PhotoView photoView;
    public ImageView playIv;
    public boolean isPicType;
    public VideoPlayerView mPlayerView;

    public PreviewSupportHolder(@NonNull PhotoView photoView, ImageView playIv, boolean picType) {
        this.photoView = photoView;
        this.playIv = playIv;
        this.isPicType = picType;
    }

    public PreviewSupportHolder(@NonNull PhotoView photoView, ImageView playIv, VideoPlayerView playerView, boolean picType) {
        this.photoView = photoView;
        this.playIv = playIv;
        this.isPicType = picType;
        this.mPlayerView = playerView;
    }

    public void setPlayerCallback() {
        if (mPlayerView != null) {
            mPlayerView.setPlayerViewCallback(new VideoPlayerView.OnSuperPlayerViewCallback() {
                @Override
                public void onClickCloseBtn() {
                }

                @Override
                public void onPlaying() {
                    //隐藏封面和暂停按钮
                    photoView.setVisibility(View.INVISIBLE);
                    playIv.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onPlayEnd() {
                    //展示暂停按钮
                    playIv.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(int code) {
                    //展示暂停按钮
                    playIv.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private boolean hasPlayer() {
        return !isPicType;
    }

    // 开始播放视频流
    public void startVideo(LocalMedia media) {
        if (hasPlayer()) {
            SuperPlayerModel model = new SuperPlayerModel();
            model.url = media.getRealPath();
            model.placeholderImage = media.getRealPath();
            mPlayerView.playWithModel(model);
        }
    }

    // 释放视频资源
    public void releaseVideo() {
        if (hasPlayer()) {
            mPlayerView.release();
            mPlayerView.resetPlayer();
            mPlayerView.destroyPlayerView();
        }
    }

    //绑定到adapter
    public void attachVideo() {
        if (hasPlayer()) {
            playIv.setVisibility(View.VISIBLE);
            mPlayerView.setVisibility(View.VISIBLE);
            mPlayerView.setControlCanShow(false);
            setPlayerCallback();
        }
    }

    //从adapter解绑
    public void detachVideo() {
        releaseVideo();
    }

    // 继续,不做处理
    public void resumeVideo(LocalMedia media) {
    }

    // 停止播放，这里参考微信，直接释放播放器
    public void pauseVideo() {
        if (hasPlayer()) {
            // 展示暂停按钮
            playIv.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.VISIBLE);
            mPlayerView.resetPlayer();
        }
    }
}
