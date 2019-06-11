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

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity;

public class VoiceHoldService extends Service {
    private RelativeLayout relativeLayoutVoiceHold;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private ImageButton imageButtonVoiceCommunication;
    private Chronometer chronometer;
    //状态栏高度.
    private int statusBarHeight = -1;
    private LayoutInflater inflater;
    private long baseTime = 0;
    private int screenSize;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inflater = LayoutInflater.from(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        baseTime = intent.getLongExtra(ChannelVoiceCommunicationActivity.VOICE_TIME, 0);
        screenSize = intent.getIntExtra(ChannelVoiceCommunicationActivity.SCREEN_SIZE, 0);
        initViews();
        createToucher();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化
     */
    private void initViews() {
        initParams();
        //获取浮动窗口视图所在布局.
        relativeLayoutVoiceHold = (RelativeLayout) inflater.inflate(R.layout.service_voice_communication, null);
        imageButtonVoiceCommunication = (ImageButton) relativeLayoutVoiceHold.findViewById(R.id.img_btn_voice_window);
        chronometer = (Chronometer) relativeLayoutVoiceHold.findViewById(R.id.chronometer_voice_communication_time);
        //添加toucherlayout
        windowManager.addView(relativeLayoutVoiceHold, params);
    }

    /**
     * 设置参数
     */
    private void initParams() {
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
        params.x = screenSize - DensityUtil.dip2px(this, 74);
        params.y = DensityUtil.dip2px(this, 4);
        //设置悬浮窗口长宽数据.
        params.width = DensityUtil.dip2px(this, 64);
        params.height = DensityUtil.dip2px(this, 84);
    }

    /**
     * 创建悬浮窗
     */
    private void createToucher() {
        //主动计算出当前View的宽高信息.
        relativeLayoutVoiceHold.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        chronometer.setBase(SystemClock.elapsedRealtime() - (baseTime * 1000));
        chronometer.start();
        initLinsters();
    }

    /**
     * 初始化监听器
     */
    private void initLinsters() {
        View.OnClickListener listener = new View.OnClickListener() {
            long[] hints = new long[2];

            @Override
            public void onClick(View v) {
                System.arraycopy(hints, 1, hints, 0, hints.length - 1);
//                hints[hints.length - 1] = SystemClock.uptimeMillis();
//                if (SystemClock.uptimeMillis() - hints[0] >= 700) {
//                    Intent intent = new Intent(getBaseContext(), CreateGestureActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    getApplication().startActivity(intent);
//                    LogUtils.YfcDebug("要执行");
//                    Toast.makeText(VoiceHoldService.this, "连续点击两次以退出", Toast.LENGTH_SHORT).show();
//                } else {
//                    LogUtils.YfcDebug("即将关闭");
//                    stopSelf();
//                }
                goBackVoiceCommunicationActivity();
            }
        };
        relativeLayoutVoiceHold.setOnClickListener(listener);
        imageButtonVoiceCommunication.setOnClickListener(listener);

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                params.x = (int) event.getRawX() - DensityUtil.dip2px(VoiceHoldService.this, 32);
                params.y = (int) event.getRawY() - DensityUtil.dip2px(VoiceHoldService.this, 42) - statusBarHeight;
                windowManager.updateViewLayout(relativeLayoutVoiceHold, params);
                return false;
            }
        };
        relativeLayoutVoiceHold.setOnTouchListener(touchListener);
        imageButtonVoiceCommunication.setOnTouchListener(touchListener);
    }

    /**
     * 回到
     */
    private void goBackVoiceCommunicationActivity() {
        Intent intent = new Intent(getBaseContext(), ChannelVoiceCommunicationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.COME_BACK_FROM_SERVICE);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_TIME, Long.parseLong(TimeUtils.getChronometerSeconds(chronometer.getText().toString())));
        getApplication().startActivity(intent);
        stopSelf();
    }


    @Override
    public void onDestroy() {
        if (imageButtonVoiceCommunication != null) {
            windowManager.removeView(relativeLayoutVoiceHold);
        }
        super.onDestroy();
    }
}
