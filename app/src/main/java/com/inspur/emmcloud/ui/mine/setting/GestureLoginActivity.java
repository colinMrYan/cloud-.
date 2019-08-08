package com.inspur.emmcloud.ui.mine.setting;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternUtil;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternView;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Sym on 2015/12/24.
 */
public class GestureLoginActivity extends BaseActivity {

    private static final int GESTURE_CODE_TIMES = 5;
    private static final long DELAYTIME = 600l;
    public final static String GESTURE_CODE_CHANGE = "gesture_code_change";
    @BindView(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @BindView(R.id.gestrue_message_text)
    TextView gestureMessage;
    @BindView(R.id.forget_gesture_btn)
    Button forgetGestureBtn;
    private String gesturePassword;
    private boolean isLogin = false;
    private int errorTime = 0;
    private FingerprintIdentify cloudFingerprintIdentify;
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
                    if (getIntent().hasExtra(GESTURE_CODE_CHANGE)) {
                        String command = getIntent().getStringExtra(GESTURE_CODE_CHANGE);
                        if (command.equals("reset")) {
                            IntentUtils.startActivity(GestureLoginActivity.this, CreateGestureActivity.class);
                            finish();
                        } else if (command.equals("login")) {
                            afterUnLockSuccess();
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

    /**
     * 通过手势解锁或指纹解锁成功后
     */
    private void afterUnLockSuccess() {
        isLogin = true;
        //发送解锁广播是，SchemeHandleActivity中接收处理
        MyApplication.getInstance().setSafeLock(false);
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SAFE_UNLOCK));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_SAFE_UNLOCK)) {
            finish();
        }
    }


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        init();
        ImmersionBar.with(this).statusBarColor(R.color.grey_f6f6f6).statusBarDarkFont(true, 0.2f).init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_gesture_login;
    }

    protected int getStatusType() {
        return STATUS_NO_SET;
    }


    private void init() {
        //得到当前用户的手势密码
        gesturePassword = CreateGestureActivity.getGestureCodeByUser(GestureLoginActivity.this);
        lockPatternView.setOnPatternListener(patternListener);
        updateStatus(Status.DEFAULT);
        if (getIntent().hasExtra(GESTURE_CODE_CHANGE)) {
            String command = getIntent().getStringExtra(GESTURE_CODE_CHANGE);
            if (command.equals("login")) {
                isLogin = true;
            }
        }
        String userHeadImgUri = APIUri
                .getChannelImgUrl(GestureLoginActivity.this, ((MyApplication) getApplication()).getUid());
        CircleTextImageView circleImageView = findViewById(R.id.gesture_login_user_head_img);
        ImageDisplayUtils.getInstance().displayImage(circleImageView,
                userHeadImgUri, R.drawable.icon_person_default);
        LogUtils.YfcDebug("是否开启指纹开关：" + PreferencesByUserAndTanentUtils.getBoolean(this, Constant.SAFE_CENTER_FINGER_PRINT, false));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PreferencesByUserAndTanentUtils.getBoolean(this, Constant.SAFE_CENTER_FINGER_PRINT, false)) {
            cloudFingerprintIdentify = new FingerprintIdentify(this);
            LogUtils.YfcDebug("启用指纹识别");
            initFingerPrint();
        }
    }

    private void showFingerPrintDialog() {
//        if(cloudFingerprintIdentify != null && cloudFingerprintIdentify.isRegisteredFingerprint()){
//            LogUtils.YfcDebug("恢复指纹识别功能");
//            cloudFingerprintIdentify.resumeIdentify();
//        }
        final MyDialog myDialog = new MyDialog(GestureLoginActivity.this, R.layout.safe_finger_print_dialog);
        myDialog.setCancelable(false);
        TextView cancelBtn = myDialog.findViewById(R.id.tv_finger_print_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                if (cloudFingerprintIdentify != null) {
                    LogUtils.YfcDebug("关闭指纹识别功能");
                    cloudFingerprintIdentify.cancelIdentify();
                }
            }
        });
        myDialog.show();
    }

    /**
     * 初始化指纹识别
     */
    private void initFingerPrint() {

        if (!isFingerPrintAvaiable(cloudFingerprintIdentify)) {
            LogUtils.YfcDebug("设备指纹不可用");
            return;
        }
        cloudFingerprintIdentify.startIdentify(5, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                // 验证成功，自动结束指纹识别
                afterUnLockSuccess();
                LogUtils.YfcDebug("指纹识别成功");

            }

            @Override
            public void onNotMatch(int availableTimes) {
                // 指纹不匹配，并返回可用剩余次数并自动继续验证
                LogUtils.YfcDebug("指纹识别剩余次数：" + availableTimes);
                if (availableTimes <= 0) {
                    ToastUtils.show(GestureLoginActivity.this, "您的识别次数用尽，请尝试手势解锁，或者一段时间后重试");
                } else {
                    ToastUtils.show(GestureLoginActivity.this, "指纹认证失败，您还可以尝试" + availableTimes + "次");
                }
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                // 错误次数达到上限或者API报错停止了验证，自动结束指纹识别
                // isDeviceLocked 表示指纹硬件是否被暂时锁定
                // 通常情况错误五次后会锁定三十秒，不同硬件也不一定完全如此，有资料介绍有的硬件也会锁定长达两分钟
                LogUtils.YfcDebug("isDeviceLocked:" + isDeviceLocked);
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                // 第一次调用startIdentify失败，因为设备被暂时锁定
                LogUtils.YfcDebug("设备被锁定");
            }

        });
        showFingerPrintDialog();
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


    /**
     * 判断指纹识别成功，识别成功后关闭锁屏Activity
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFingerPrintSuccess(String fingerPrintSuccess) {
        if (fingerPrintSuccess.equals("success")) {
            finish();
        }
    }

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
    @OnClick({R.id.forget_gesture_btn, R.id.btn_use_finger_print})
    public void forgetGesturePasswrod(View view) {
        switch (view.getId()) {
            case R.id.forget_gesture_btn:
                clearGestureInfo();
                ((MyApplication) getApplication()).signout();
                break;
            case R.id.btn_use_finger_print:
                initFingerPrint();
                break;
        }
    }

    /**
     * 清理手势信息
     */
    private void clearGestureInfo() {
        CreateGestureActivity.putFingerPrint(GestureLoginActivity.this, false);
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
        EventBus.getDefault().unregister(this);
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
