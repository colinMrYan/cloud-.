package com.inspur.emmcloud.basemodule.media.player.model;

import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.tencent.rtmp.TXLivePlayer;

public abstract class SuperPlayerObserver {

    /**
     * 准备播放
     */
    public void onPlayPrepare() {

    }

    /**
     * 开始播放
     *
     * @param name 当前视频名称
     */
    public void onPlayBegin(String name) {
    }

    /**
     * 播放暂停
     */
    public void onPlayPause() {
    }

    /**
     * 播放器停止
     */
    public void onPlayStop() {
    }

    /**
     * 播放器进入Loading状态
     */
    public void onPlayLoading() {
    }

    /**
     * 播放进度回调
     *
     * @param current
     * @param duration
     */
    public void onPlayProgress(long current, long duration) {
    }

    public void onError(int code, String message) {
    }

    public void onRcvFirstIframe() {

    }
}
