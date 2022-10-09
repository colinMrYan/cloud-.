package com.inspur.emmcloud.basemodule.media.player.basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * 播放器公共逻辑
 */
public abstract class AbsPlayer extends RelativeLayout implements Player {

    protected Callback mControllerCallback; // 播放控制回调

    // 隐藏control view，仿微信播放器
    protected Runnable mHideViewRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public AbsPlayer(Context context) {
        super(context);
    }

    public AbsPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCallback(Callback callback) {
        mControllerCallback = callback;
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void release() {

    }

    @Override
    public void updatePlayState(SuperPlayerDef.PlayerState playState) {

    }

    @Override
    public void updateVideoProgress(long current, long duration) {

    }

    /**
     * 设置控件的可见性
     *
     * @param view      目标控件
     * @param isVisible 显示：true 隐藏：false
     */
    protected void toggleView(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 将秒数转换为hh:mm:ss的格式
     *
     * @param second
     * @return
     */
    protected String formattedTime(long second) {
        String formatTime;
        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h == 0) {
            formatTime = asTwoDigit(m) + ":" + asTwoDigit(s);
        } else {
            formatTime = asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s);
        }
        return formatTime;
    }

    protected String asTwoDigit(long digit) {
        String value = "";
        if (digit < 10) {
            value = "0";
        }
        value += String.valueOf(digit);
        return value;
    }

}
