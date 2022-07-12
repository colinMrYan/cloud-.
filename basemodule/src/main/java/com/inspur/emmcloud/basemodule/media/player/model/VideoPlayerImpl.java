package com.inspur.emmcloud.basemodule.media.player.model;

import android.os.Bundle;

import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayer;
import com.inspur.emmcloud.basemodule.media.player.basic.SuperPlayerDef;
import com.inspur.emmcloud.basemodule.media.player.interfaces.ISuperPlayerListener;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * Date：2022/7/11
 * Author：wang zhen
 * Description
 */
public class VideoPlayerImpl implements SuperPlayer , ITXVodPlayListener {



    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int i, Bundle bundle) {

    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }

    @Override
    public void play(SuperPlayerModel model) {

    }

    @Override
    public void reStart() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void pauseVod() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void revertSettings() {

    }

    @Override
    public void enableHardwareDecode(boolean enable) {

    }

    @Override
    public void setPlayerView(TXCloudVideoView videoView) {

    }

    @Override
    public void seek(int position) {

    }

    @Override
    public void setRate(float speedLevel) {

    }

    @Override
    public String getPlayURL() {
        return null;
    }

    @Override
    public SuperPlayerDef.PlayerState getPlayerState() {
        return null;
    }

    @Override
    public SuperPlayerDef.PlayerType getPlayerType() {
        return null;
    }

    @Override
    public void setObserver(SuperPlayerObserver observer) {

    }

    @Override
    public void setSuperPlayerListener(ISuperPlayerListener superPlayerListener) {

    }

    @Override
    public void setLoop(boolean isLoop) {

    }

    @Override
    public void setStartTime(float startPos) {

    }

    @Override
    public void setAutoPlay(boolean isAutoPlay) {

    }

    @Override
    public void setNeedToPause(boolean value) {

    }
}
