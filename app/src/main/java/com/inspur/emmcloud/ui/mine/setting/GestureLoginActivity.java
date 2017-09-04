package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.CalEventNotificationUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.ninelock.LockPatternUtil;
import com.inspur.emmcloud.util.ninelock.LockPatternView;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Sym on 2015/12/24.
 */
public class GestureLoginActivity extends BaseActivity {

    private static final String TAG = "LoginGestureActivity";

    @Bind(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @Bind(R.id.messageTv)
    TextView messageTv;
    @Bind(R.id.forgetGestureBtn)
    Button forgetGestureBtn;
    private static final long DELAYTIME = 600l;
    private String gesturePassword;
    private boolean isLogin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this, R.color.grey_f6f6f6);
        setContentView(R.layout.activity_gesture_login);
        ((MyApplication) getApplicationContext()).addActivity(this);
        ButterKnife.bind(this);
        this.init();
//        EventBus.getDefault().register(this);
    }

    private void init() {
        //得到当前用户的手势密码
        gesturePassword = PreferencesByUserAndTanentUtils.getString(GestureLoginActivity.this, "gesture_code");
        lockPatternView.setOnPatternListener(patternListener);
        updateStatus(Status.DEFAULT);
        if (getIntent().hasExtra("gesture_code_change")) {
            String command = getIntent().getStringExtra("gesture_code_change");
            if(command.equals("login")){
                isLogin = true;
            }
        }
        //由于机型，系统等问题，目前不开启指纹识别功能
//        initFingerPrint();
    }

    /**
     * 初始化指纹识别
     */
    private void initFingerPrint() {
        FingerprintIdentify cloudFingerprintIdentify = new FingerprintIdentify(this);
        if(!isFingerPrintAvaiable(cloudFingerprintIdentify)){
            LogUtils.YfcDebug("设备指纹不可用");
            return;
        }
        if(!PreferencesByUserAndTanentUtils.getBoolean(GestureLoginActivity.this,SafeCenterActivity.FINGER_PRINT_STATE,false)){
            LogUtils.YfcDebug("用户没有开启指纹解锁");
            return;
        }
        cloudFingerprintIdentify.startIdentify(10, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                // 验证成功，自动结束指纹识别
//                EventBus.getDefault().post("success");
                finish();
                LogUtils.YfcDebug("指纹识别成功");
            }

            @Override
            public void onNotMatch(int availableTimes) {
                // 指纹不匹配，并返回可用剩余次数并自动继续验证
                LogUtils.YfcDebug("指纹识别剩余次数："+availableTimes);
                ToastUtils.show(GestureLoginActivity.this,"指纹认证失败，您还可以尝试"+availableTimes+"次");
                if(availableTimes == 0){
                    ToastUtils.show(GestureLoginActivity.this,"您的识别次数用尽，请尝试手势解锁，或者一段时间后重试");
                }
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                // 错误次数达到上限或者API报错停止了验证，自动结束指纹识别
                // isDeviceLocked 表示指纹硬件是否被暂时锁定
                LogUtils.YfcDebug("isDeviceLocked:"+isDeviceLocked);
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                // 第一次调用startIdentify失败，因为设备被暂时锁定
                LogUtils.YfcDebug("设备被锁定");
            }

        });
    }

    /**
     * 判断指纹是否可用
     * @param cloudFingerprintIdentify
     * @return
     */
    private boolean isFingerPrintAvaiable(FingerprintIdentify cloudFingerprintIdentify) {
        boolean isHardwareEnable = getIsHardwareEnable(cloudFingerprintIdentify);
        boolean isFingerprintEnable = getIsFingerprintEnable(cloudFingerprintIdentify);
        return isHardwareEnable && isFingerprintEnable;
    }

    /**
     * 判断是否设置了指纹
     * @param cloudFingerprintIdentify
     * @return
     */
    private boolean getIsFingerprintEnable(FingerprintIdentify cloudFingerprintIdentify) {
        return cloudFingerprintIdentify.isFingerprintEnable();
    }

    /**
     * 硬件是否可用
     * @return
     */
    private boolean getIsHardwareEnable(FingerprintIdentify cloudFingerprintIdentify) {
        return cloudFingerprintIdentify.isHardwareEnable();
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onFingerPrintSuccess(String fingerPrintSuccess){
//        if(fingerPrintSuccess.equals("success")){
//            finish();
//        }
//    }

    private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            lockPatternView.removePostClearPatternRunnable();
        }

        @Override
        public void onPatternComplete(List<LockPatternView.Cell> pattern) {
            if (pattern != null) {
                if (LockPatternUtil.checkPattern(pattern, gesturePassword)) {
                    updateStatus(Status.CORRECT);
                    if (getIntent().hasExtra("gesture_code_change")) {
                        String command = getIntent().getStringExtra("gesture_code_change");
                        LogUtils.YfcDebug("command："+command);
                        if (command.equals("reset")) {
                            IntentUtils.startActivity(GestureLoginActivity.this, CreateGestureActivity.class);
                            finish();
                        } else if (command.equals("login")) {
                            isLogin = true;
                            finish();
                        } else if(command.equals("close")){
                            clearGestureInfo();
                            finish();
                        }
                    }
                } else {
                    updateStatus(Status.ERROR);
                }
            }
        }
    };

    /**
     * 更新状态
     *
     * @param status
     */
    private void updateStatus(Status status) {
        messageTv.setText(status.strId);
        messageTv.setTextColor(getResources().getColor(status.colorId));
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case ERROR:
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                loginGestureSuccess();
                break;
        }
    }

    /**
     * 手势登录成功（去首页）
     */
    private void loginGestureSuccess() {
        Toast.makeText(GestureLoginActivity.this, "success", Toast.LENGTH_SHORT).show();
    }

    /**
     * 忘记手势密码（去账号登录界面）
     */
    @OnClick(R.id.forgetGestureBtn)
    void forgetGesturePasswrod() {
        signout();
        clearGestureInfo();
        finish();
    }

    /**
     * 清理手势信息
     */
    private void clearGestureInfo() {
        PreferencesByUserAndTanentUtils.putBoolean(GestureLoginActivity.this,"gesture_code_isopen",false);
        PreferencesByUserAndTanentUtils.putString(GestureLoginActivity.this, "gesture_code","");
    }

    private enum Status {
        //默认的状态
        DEFAULT(R.string.gesture_default, R.color.grey_a5a5a5),
        //密码输入错误
        ERROR(R.string.gesture_error, R.color.red_f4333c),
        //密码输入正确
        CORRECT(R.string.gesture_correct, R.color.grey_a5a5a5);

        private Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
        private int strId;
        private int colorId;
    }

    @Override
    public void onBackPressed() {
        if(isLogin){
            ((MyApplication)getApplication()).exit();
        }else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    //登出逻辑
    private void signout() {
        // TODO Auto-generated method stub
        if (((MyApplication) getApplicationContext()).getWebSocketPush() != null) {
            ((MyApplication) getApplicationContext()).getWebSocketPush()
                    .webSocketSignout();
        }
        //清除日历提醒极光推送本地通知
        CalEventNotificationUtils.cancelAllCalEventNotification(GestureLoginActivity.this);
        ((MyApplication) getApplicationContext()).stopPush();
        ((MyApplication) getApplicationContext()).clearNotification();
        ((MyApplication) getApplicationContext()).removeAllCookie();
        ((MyApplication) getApplicationContext()).clearUserPhotoMap();
        PreferencesUtils.putString(GestureLoginActivity.this, "tokenType", "");
        PreferencesUtils.putString(GestureLoginActivity.this, "accessToken", "");
        ((MyApplication) getApplicationContext()).setAccessToken("");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
}
