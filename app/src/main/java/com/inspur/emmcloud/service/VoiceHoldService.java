package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;

public class VoiceHoldService extends Service {


    RelativeLayout relativeLayoutVoiceHold;
    WindowManager.LayoutParams params;
    WindowManager windowManager;

    ImageButton imageButtonVoiceCommunication;
    Chronometer chronometer;

    //状态栏高度.
    int statusBarHeight = -1;

    //不与Activity进行绑定.
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.YfcDebug("VoiceHoldService Created");
        createToucher();
    }

    private void createToucher() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        params.width = DensityUtil.dip2px(this,64);
        params.height = DensityUtil.dip2px(this,84);

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        relativeLayoutVoiceHold = (RelativeLayout) inflater.inflate(R.layout.service_voice_communication, null);
        //添加toucherlayout
        windowManager.addView(relativeLayoutVoiceHold, params);

        LogUtils.YfcDebug("toucherlayout-->left:" + relativeLayoutVoiceHold.getLeft());
        LogUtils.YfcDebug("toucherlayout-->right:" + relativeLayoutVoiceHold.getRight());
        LogUtils.YfcDebug("toucherlayout-->top:" + relativeLayoutVoiceHold.getTop());
        LogUtils.YfcDebug("toucherlayout-->bottom:" + relativeLayoutVoiceHold.getBottom());

        //主动计算出当前View的宽高信息.
        relativeLayoutVoiceHold.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        LogUtils.YfcDebug("状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButtonVoiceCommunication = (ImageButton) relativeLayoutVoiceHold.findViewById(R.id.img_btn_voice_window);
        chronometer = (Chronometer) relativeLayoutVoiceHold.findViewById(R.id.chronometer_voice_communication_time);
        chronometer.start();
        imageButtonVoiceCommunication.setOnClickListener(new View.OnClickListener() {
            long[] hints = new long[2];

            @Override
            public void onClick(View v) {
                LogUtils.YfcDebug("点击了View");
                System.arraycopy(hints, 1, hints, 0, hints.length - 1);
                hints[hints.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - hints[0] >= 700) {
                    LogUtils.YfcDebug("要执行");
                    Toast.makeText(VoiceHoldService.this, "连续点击两次以退出", Toast.LENGTH_SHORT).show();
                } else {
                    LogUtils.YfcDebug("即将关闭");
                    stopSelf();
                }
            }
        });

        relativeLayoutVoiceHold.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                params.x = (int) event.getRawX() - 150;
                params.y = (int) event.getRawY() - 150 - statusBarHeight;
                windowManager.updateViewLayout(relativeLayoutVoiceHold, params);
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        if (imageButtonVoiceCommunication != null) {
            windowManager.removeView(relativeLayoutVoiceHold);
        }
        super.onDestroy();
    }
}
