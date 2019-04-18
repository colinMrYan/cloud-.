package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternUtil;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternView;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.List;


/**
 * Created by Sym on 2015/12/24.
 */
@ContentView(R.layout.activity_gesture_login)
public class GestureLoginActivity extends BaseActivity {

    private static final int GESTURE_CODE_TIMES = 5;
    private static final long DELAYTIME = 600l;
    @ViewInject(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @ViewInject(R.id.gestrue_message_text)
    TextView gestureMessage;
    @ViewInject(R.id.forget_gesture_btn)
    Button forgetGestureBtn;
    private String gesturePassword;
    private boolean isLogin = false;
    private int errorTime = 0;
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
                        if (command.equals("reset")) {
                            IntentUtils.startActivity(GestureLoginActivity.this, CreateGestureActivity.class);
                            finish();
                        } else if (command.equals("login")) {
                            isLogin = true;
                            //发送解锁广播是，SchemeHandleActivity中接收处理
                            Intent intent = new Intent();
                            intent.setAction(Constant.ACTION_SAFE_UNLOCK);
                            MyApplication.getInstance().setIsActive(true);
                            LocalBroadcastManager.getInstance(GestureLoginActivity.this).sendBroadcast(intent);
                            finish();
                        } else if (command.equals("close")) {
                            clearGestureInfo();
                            finish();
                        }
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                } else {
                    errorTime = errorTime + 1;
                    updateStatus(Status.ERROR);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        ImmersionBar.with(this).statusBarColor(R.color.grey_f6f6f6).statusBarDarkFont(true, 0.2f).init();
    }

    private void init() {
        //得到当前用户的手势密码
        gesturePassword = CreateGestureActivity.getGestureCodeByUser(GestureLoginActivity.this);
        lockPatternView.setOnPatternListener(patternListener);
        updateStatus(Status.DEFAULT);
        if (getIntent().hasExtra("gesture_code_change")) {
            String command = getIntent().getStringExtra("gesture_code_change");
            if (command.equals("login")) {
                isLogin = true;
            }
        }
        String userHeadImgUri = APIUri
                .getChannelImgUrl(GestureLoginActivity.this, ((MyApplication) getApplication()).getUid());
        CircleTextImageView circleImageView = (CircleTextImageView) findViewById(R.id.gesture_login_user_head_img);
        ImageDisplayUtils.getInstance().displayImage(circleImageView,
                userHeadImgUri, R.drawable.icon_person_default);
        //由于机型，系统等问题，目前不开启指纹识别功能
//        initFingerPrint();
    }

    /**
     * 初始化指纹识别
     */
    private void initFingerPrint() {
        FingerprintIdentify cloudFingerprintIdentify = new FingerprintIdentify(this);
        if (!isFingerPrintAvaiable(cloudFingerprintIdentify)) {
            LogUtils.YfcDebug("设备指纹不可用");
            return;
        }
        if (!PreferencesByUserAndTanentUtils.getBoolean(GestureLoginActivity.this, "finger_print_state", false)) {
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
                LogUtils.YfcDebug("指纹识别剩余次数：" + availableTimes);
                ToastUtils.show(GestureLoginActivity.this, "指纹认证失败，您还可以尝试" + availableTimes + "次");
                if (availableTimes == 0) {
                    ToastUtils.show(GestureLoginActivity.this, "您的识别次数用尽，请尝试手势解锁，或者一段时间后重试");
                }
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                // 错误次数达到上限或者API报错停止了验证，自动结束指纹识别
                // isDeviceLocked 表示指纹硬件是否被暂时锁定
                LogUtils.YfcDebug("isDeviceLocked:" + isDeviceLocked);
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
     *
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
     *
     * @param cloudFingerprintIdentify
     * @return
     */
    private boolean getIsFingerprintEnable(FingerprintIdentify cloudFingerprintIdentify) {
        return cloudFingerprintIdentify.isFingerprintEnable();
    }


//    /**
//     * 判断指纹识别成功，识别成功后关闭锁屏Activity
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onFingerPrintSuccess(String fingerPrintSuccess){
//        if(fingerPrintSuccess.equals("success")){
//            finish();
//        }
//    }

    /**
     * 硬件是否可用
     *
     * @return
     */
    private boolean getIsHardwareEnable(FingerprintIdentify cloudFingerprintIdentify) {
        return cloudFingerprintIdentify.isHardwareEnable();
    }

    /**
     * 更新状态
     *
     * @param status
     */
    private void updateStatus(Status status) {
        gestureMessage.setText(status.strId);
        gestureMessage.setTextColor(getResources().getColor(status.colorId));
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case ERROR:
                gestureMessage.setText(getString(R.string.gesture_code_error) + " " +
                        ((GESTURE_CODE_TIMES - errorTime) < 0 ? 0 : (GESTURE_CODE_TIMES - errorTime)) + " " + getString(R.string.gesture_code_time));
                findViewById(R.id.gesture_code_tips).setVisibility(View.VISIBLE);
                Animation shake = AnimationUtils.loadAnimation(this, R.anim.left_right_shake);
                gestureMessage.startAnimation(shake);
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                if ((GESTURE_CODE_TIMES - errorTime) == 0) {
                    clearGestureInfo();
                    ((MyApplication) getApplication()).signout();
                }
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
        }
    }

    /**
     * 忘记手势密码（去账号登录界面）
     */
    @Event(R.id.forget_gesture_btn)
    private void forgetGesturePasswrod(View view) {
        switch (view.getId()) {
            case R.id.forget_gesture_btn:
                clearGestureInfo();
                ((MyApplication) getApplication()).signout();
                break;
            default:
                break;
        }

    }

    /**
     * 清理手势信息
     */
    private void clearGestureInfo() {
        CreateGestureActivity.putGestureCodeByUser(GestureLoginActivity.this, "");
        CreateGestureActivity.putGestureCodeIsOpenByUser(GestureLoginActivity.this, false);
    }

    @Override
    public void onBackPressed() {
        if (isLogin) {
            ((MyApplication) getApplication()).exit();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    private enum Status {
        //默认的状态
        DEFAULT(R.string.gesture_default, R.color.grey_a5a5a5),
        //密码输入错误
        ERROR(R.string.gesture_error, R.color.red_f4333c),
        //密码输入正确
        CORRECT(R.string.gesture_correct, R.color.grey_a5a5a5);

        private int strId;
        private int colorId;

        Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
    }

}
