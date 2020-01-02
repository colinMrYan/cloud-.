package com.inspur.emmcloud.setting.ui.setting;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.util.FingerPrintUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * Created by chenmch on 2019/1/25.
 */

public class SafeGustureFaceSettingActivity extends BaseActivity {
    @BindView(R2.id.switch_view_setting_safe_start_guesture)
    SwitchCompat guestureSwitchView;
    @BindView(R2.id.switch_view_setting_safe_start_face)
    SwitchCompat fingerPrintSwitchView;
    @BindView(R2.id.rl_setting_safe_reset_guesture)
    RelativeLayout resetGuestureLayout;
    @BindView(R2.id.rl_finger_print)
    RelativeLayout fingerPrintLayout;
    @BindView(R2.id.tv_fingerprint_face_unlock)
    TextView fingerPrintFaceText;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_safe_guestur_face_setting_activity;
    }

    private void initView() {
        guestureSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == isGestureOpen()) {
                    return;
                }
                if (b) {
                    IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, CreateGestureGuideActivity.class);
                } else {
                    int doubleValidation = PreferencesByUserAndTanentUtils.getInt(BaseApplication.getInstance(), Constant.PREF_MNM_DOUBLE_VALIADATION, -1);
                    if (doubleValidation != 1) {
                        Bundle bundle = new Bundle();
                        bundle.putString(GestureLoginActivity.GESTURE_CODE_CHANGE, "close");
                        IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, GestureLoginActivity.class, bundle);
                    } else {
                        guestureSwitchView.setChecked(true);
                        ToastUtils.show(SafeGustureFaceSettingActivity.this, R.string.setting_gesture_force_open);
                    }
                }

            }
        });
        fingerPrintFaceText.setText(FingerPrintUtils.getFingerPrintInstance().isFingerPrintAvaiable(this) ?
                R.string.setting_safe_gesture_face : R.string.setting_safe_center_gesture);

        fingerPrintSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesByUserAndTanentUtils.putBoolean(SafeGustureFaceSettingActivity.this, Constant.SAFE_CENTER_FINGER_PRINT, b);
                if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(getApplication()) != b) {
//                    intentFaceVerifyActivity(b);
                    fingerPrintSwitchView.setChecked(b);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isGestureOpen = isGestureOpen();
        fingerPrintLayout.setVisibility(FingerPrintUtils.getFingerPrintInstance().isFingerPrintAvaiable(this) && isGestureOpen ? View.VISIBLE : View.GONE);
        if (guestureSwitchView.isChecked() != isGestureOpen) {
            guestureSwitchView.setChecked(isGestureOpen);
        }
        resetGuestureLayout.setVisibility(isGestureOpen() ? View.VISIBLE : View.GONE);
        fingerPrintSwitchView.setChecked(PreferencesByUserAndTanentUtils.getBoolean(SafeGustureFaceSettingActivity.this, Constant.SAFE_CENTER_FINGER_PRINT, false));
    }

    /**
     * 判断是否能显示指纹解锁
     *
     * @return
     */
    public boolean isGestureOpen() {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(SafeGustureFaceSettingActivity.this);
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.rl_setting_safe_reset_guesture) {
            Bundle bundle = new Bundle();
            bundle.putString(GestureLoginActivity.GESTURE_CODE_CHANGE, "reset");
            IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, GestureLoginActivity.class, bundle);

        } else if (i == R.id.rl_setting_safe_reset_face) {
            intentFaceVerifyActivity(true);

        }
    }

    private void intentFaceVerifyActivity(boolean isFaceSettingOpen) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFaceSettingOpen", isFaceSettingOpen);
        IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, FaceVerifyActivity.class, bundle);
    }
}
