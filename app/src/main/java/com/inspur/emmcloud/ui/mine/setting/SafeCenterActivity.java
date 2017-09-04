package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.SwitchView;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;

/**
 * Created by yufuchang on 2017/9/2.
 */

public class SafeCenterActivity extends BaseActivity {

    public static final String FINGER_PRINT_STATE = "finger_print_state";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_center);
        ((MyApplication) getApplicationContext()).addActivity(this);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //因为机型，系统等问题暂时不开启指纹识别功能
//        initFingerPrintState();
    }

    /**
     * 初始化指纹选项状态
     */
    private void initFingerPrintState() {
        FingerprintIdentify cloudFingerprintIdentify = new FingerprintIdentify(this);
        boolean isFingerHardwareAvaiable = getIsHardwareEnable(cloudFingerprintIdentify);
        findViewById(R.id.safe_center_finger_layout).setVisibility(isFingerHardwareAvaiable?View.VISIBLE:View.GONE);
        //有可用指纹硬件的情况下处理下面情况，没有则忽略，避免过度执行无用代码
        if(isFingerHardwareAvaiable){
            handleFingerPrintSwitchView(cloudFingerprintIdentify);
        }
    }

    /**
     * 处理指纹开关状态
     * @param cloudFingerprintIdentify
     */
    private void handleFingerPrintSwitchView(final FingerprintIdentify cloudFingerprintIdentify) {
        SwitchView switchView = (SwitchView) findViewById(R.id.safe_center_finger_switchview);
        final boolean isHasFingerPrint = getIsFingerprintEnable(cloudFingerprintIdentify);
        boolean isOpenFingerPrint = PreferencesByUserAndTanentUtils.getBoolean(SafeCenterActivity.this,FINGER_PRINT_STATE,false);
        switchView.setOpened((isHasFingerPrint&&isOpenFingerPrint)?true:false);
        switchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                if(!isHasFingerPrint){
                    ((SwitchView)view).setOpened(false);
                    LogUtils.YfcDebug("没有指纹，提示设置指纹");
                    ToastUtils.show(SafeCenterActivity.this,"您的设备还未设置指纹，请先在手机设置里为设备设置指纹");
                    PreferencesByUserAndTanentUtils.putBoolean(SafeCenterActivity.this,FINGER_PRINT_STATE,false);
                }else {
                    ((SwitchView)view).setOpened(true);
                    cloudFingerprintIdentify.cancelIdentify();
                    PreferencesByUserAndTanentUtils.putBoolean(SafeCenterActivity.this,FINGER_PRINT_STATE,true);
                    ToastUtils.show(SafeCenterActivity.this,"您的指纹解锁服务已经开启，下次可以使用指纹解锁进入应用");
                }
            }

            @Override
            public void toggleToOff(View view) {
                ((SwitchView)view).setOpened(false);
                PreferencesByUserAndTanentUtils.putBoolean(SafeCenterActivity.this,FINGER_PRINT_STATE,false);
                ToastUtils.show(SafeCenterActivity.this,"您的指纹解锁服务已经关闭");
            }
        });
    }

    /**
     * 检查是否设置了指纹并提示
     * @param cloudFingerprintIdentify
     * @return
     */
    private boolean checkIsHasFingerPrint(FingerprintIdentify cloudFingerprintIdentify) {
        if(!getIsFingerprintEnable(cloudFingerprintIdentify)){
            ToastUtils.show(SafeCenterActivity.this,"未设置指纹",Toast.LENGTH_SHORT);
            LogUtils.YfcDebug("未设置指纹");
        }
        return true;
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

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.safe_center_gesture_layout:
                Intent intentGesture = new Intent();
                intentGesture.setClass(this, SwitchGestureActivity.class);
                startActivity(intentGesture);
                break;
            default:
                break;
        }
    }
}
