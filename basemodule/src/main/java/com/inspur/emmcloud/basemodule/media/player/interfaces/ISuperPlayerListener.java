package com.inspur.emmcloud.basemodule.media.player.interfaces;

import android.os.Bundle;

import com.tencent.rtmp.TXVodPlayer;

public interface ISuperPlayerListener {
    public void onVodPlayEvent(final TXVodPlayer player, final int event, final Bundle param);

    public void onVodNetStatus(final TXVodPlayer player, final Bundle status);

}