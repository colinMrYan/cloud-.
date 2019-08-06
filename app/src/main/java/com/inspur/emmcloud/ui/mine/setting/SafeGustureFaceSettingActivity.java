package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/1/25.
 */

public class SafeGustureFaceSettingActivity extends BaseActivity {
    @BindView(R.id.switch_view_setting_safe_start_guesture)
    SwitchCompat guestureSwitchView;
    @BindView(R.id.switch_view_setting_safe_start_face)
    SwitchCompat faceSwitchView;
    @BindView(R.id.rl_setting_safe_reset_guesture)
    RelativeLayout resetGuestureLayout;
    @BindView(R.id.rl_setting_safe_reset_face)
    RelativeLayout resetFaceLayout;
    @BindView(R.id.rl_finger_print)
    RelativeLayout fingerPrintLayout;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_safe_guestur_face_setting;
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
                    int doubleValidation = PreferencesByUserAndTanentUtils.getInt(MyApplication.getInstance(), Constant.PREF_MNM_DOUBLE_VALIADATION, -1);
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

        faceSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesByUserAndTanentUtils.putBoolean(SafeGustureFaceSettingActivity.this, Constant.SAFE_CENTER_FINGER_PRINT, b);
                if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(getApplication()) != b) {
//                    intentFaceVerifyActivity(b);
                    faceSwitchView.setChecked(b);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isGestureOpen = isGestureOpen();
        fingerPrintLayout.setVisibility(isGestureOpen ? View.VISIBLE : View.GONE);
        if (guestureSwitchView.isChecked() != isGestureOpen) {
            guestureSwitchView.setChecked(isGestureOpen);
        }
        resetGuestureLayout.setVisibility(isGestureOpen() ? View.VISIBLE : View.GONE);

        faceSwitchView.setChecked(FaceVerifyActivity.getFaceVerifyIsOpenByUser(this));
        resetFaceLayout.setVisibility(FaceVerifyActivity.getFaceVerifyIsOpenByUser(this) ? View.VISIBLE : View.GONE);
    }

    public boolean isGestureOpen() {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(SafeGustureFaceSettingActivity.this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_setting_safe_reset_guesture:
                Bundle bundle = new Bundle();
                bundle.putString(GestureLoginActivity.GESTURE_CODE_CHANGE, "reset");
                IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, GestureLoginActivity.class, bundle);
                break;
            case R.id.rl_setting_safe_reset_face:
                intentFaceVerifyActivity(true);
                break;
        }
    }

    private void intentFaceVerifyActivity(boolean isFaceSettingOpen) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFaceSettingOpen", isFaceSettingOpen);
        IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, FaceVerifyActivity.class, bundle);
    }
}
