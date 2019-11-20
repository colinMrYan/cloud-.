package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity;

import io.agora.rtc.video.VideoCanvas;

import static com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity.COMMUNICATION_STATE_OVER;

/**
 * 悬浮窗管理类封装
 * Created by yufuchang on 2018/8/29.
 */

public class VideoSuspensionWindowManagerUtils {

    private static VideoSuspensionWindowManagerUtils videoSuspensionWindowManagerUtils;
    private View windowView = null;//整个悬浮窗view
    private WindowManager windowManager = null;//WindowManager管理类
    private Context windowContext = null;//传入的上下文
    private boolean isShowing = false;//是否还在显示的标志
    private WindowManager.LayoutParams params;//window相关参数
    private int screenWidthSize = 0;//上一个window的宽度
    private long beginTime = 0;//touch开始时间
    private boolean isTouchEvent = false;//判定touch事件的标志

    public VideoSuspensionWindowManagerUtils() {
        this.windowContext = BaseApplication.getInstance();
    }

    /**
     * 获取悬浮窗实例
     *
     * @return
     */
    public static VideoSuspensionWindowManagerUtils getInstance() {
        if (videoSuspensionWindowManagerUtils == null) {
            synchronized (VideoSuspensionWindowManagerUtils.class) {
                if (videoSuspensionWindowManagerUtils == null) {
                    videoSuspensionWindowManagerUtils = new VideoSuspensionWindowManagerUtils();
                }
            }
        }
        return videoSuspensionWindowManagerUtils;
    }

    /**
     * 显示悬浮窗
     *
     * @param screenWidthSize
     */
    public void showVideoCommunicationSmallWindow(int screenWidthSize) {
        this.screenWidthSize = screenWidthSize;
        if (isShowing) {
            return;
        }
        isShowing = true;
        initSuspensionWindowView();
        initParamsAndListeners();
        windowManager.addView(windowView, params);
    }

    /**
     * 隐藏悬浮窗
     */
    public void hideVideoCommunicationSmallWindow() {
        if (isShowing) {
            try {
                if (null != windowView && null != windowManager) {
                    windowManager.removeView(windowView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isShowing = false;
        }
        NotifyUtil.deleteNotify(windowContext);
    }

    /**
     * 悬浮窗是否还在显示
     *
     * @return
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 组装悬浮窗View
     *
     * @return
     */
    private void initSuspensionWindowView() {
        windowView = LayoutInflater.from(windowContext).inflate(R.layout.service_video_communication,
                null);
        if (VoiceCommunicationManager.getInstance().getCommunicationState() == COMMUNICATION_STATE_OVER) {
            hideVideoCommunicationSmallWindow();
        }
        RelativeLayout localVideoContainer = windowView.findViewById(R.id.rl_video_view_container);
        if (VoiceCommunicationManager.getInstance().getCommunicationState() == VoiceCommunicationActivity.COMMUNICATION_STATE_PRE) {
            SurfaceView localView = VoiceCommunicationManager.getInstance().getLocalView();
            localVideoContainer.removeAllViews();
            if (localView.getParent() instanceof ViewGroup) {
                ((ViewGroup) localView.getParent()).removeView(localView);
            }
            localVideoContainer.addView(localView);
            VoiceCommunicationManager.getInstance().getRtcEngine().setupLocalVideo(new VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        } else if (VoiceCommunicationManager.getInstance().getCommunicationState() == VoiceCommunicationActivity.COMMUNICATION_STATE_ING) {
            SurfaceView remoteView = VoiceCommunicationManager.getInstance().getRemoteView();
            localVideoContainer.removeAllViews();
            if (remoteView.getParent() instanceof ViewGroup) {
                ((ViewGroup) remoteView.getParent()).removeView(remoteView);
            }
            localVideoContainer.addView(remoteView);
            VoiceCommunicationManager.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(remoteView,
                    VideoCanvas.RENDER_MODE_HIDDEN, VoiceCommunicationManager.getInstance().getVideoFirstFrameUid()));
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTouchEvent) {
                    goBackVoiceCommunicationActivity();
                    hideVideoCommunicationSmallWindow();
                    isTouchEvent = false;
                }
            }
        };
        windowView.setOnClickListener(clickListener);
        //更新窗口位置的监听
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouchEvent = false;
                        beginTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        params.x = (int) event.getRawX() - DensityUtil.dip2px(windowContext, 32);
                        params.y = (int) event.getRawY() - DensityUtil.dip2px(windowContext, 42) - getStatusBarHeight();
                        windowManager.updateViewLayout(windowView, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouchEvent = (System.currentTimeMillis() - beginTime) > 300;
                        break;
                }
                return false;
            }
        };
        windowView.setOnTouchListener(touchListener);
    }

    /**
     * 设置参数
     *
     * @return
     */
    private void initParamsAndListeners() {
        // 获取应用的Context
        // 获取WindowManager
        windowManager = (WindowManager) windowContext.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // 悬浮窗类型
        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置flag
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        // 设置WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM可以拦截back事件
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        //设置悬浮窗口长宽数据.
        params.width = DensityUtil.dip2px(windowContext, 94);
        params.height = DensityUtil.dip2px(windowContext, 167);
        //设置悬浮窗位置
        params.x = screenWidthSize - DensityUtil.dip2px(windowContext, 74);
        params.y = DensityUtil.dip2px(windowContext, 4);
        //设置悬浮窗位置和滑动参数
        params.gravity = Gravity.LEFT | Gravity.TOP;

        //窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
    }

    /**
     * 获取statusBar高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = windowContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = windowContext.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 回到语音通话界面n
     */
    private void goBackVoiceCommunicationActivity() {
        try {
            if (VoiceCommunicationManager.getInstance().getCommunicationState() == COMMUNICATION_STATE_OVER) {
                hideVideoCommunicationSmallWindow();
                return;
            }
            if (VoiceCommunicationManager.getInstance().getWaitAndConnectedNumber() >= 2) {
                Intent intent = Intent.parseUri("ecc-cloudplus-cmd-voice-call://voice_call", Intent.URI_INTENT_SCHEME);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constant.VOICE_IS_FROM_SMALL_WINDOW, true);
                intent.putExtra(Constant.VOICE_COMMUNICATION_STATE,
                        VoiceCommunicationManager.getInstance().getCommunicationState());
                windowContext.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

