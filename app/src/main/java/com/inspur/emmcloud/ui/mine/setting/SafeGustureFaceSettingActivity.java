package com.inspur.emmcloud.ui.mine.setting;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.widget.SwitchView;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/1/25.
 */

public class SafeGustureFaceSettingActivity extends BaseActivity {
    @BindView(R.id.switch_view_setting_safe_start_guesture)
    SwitchView guestureSwitchView;
    @BindView(R.id.switch_view_setting_safe_start_face)
    SwitchView faceSwitchView;
    @BindView(R.id.rl_setting_safe_reset_guesture)
    RelativeLayout resetGuestureLayout;
    @BindView(R.id.rl_setting_safe_reset_face)
    RelativeLayout resetFaceLayout;

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
        guestureSwitchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                guestureSwitchView.setOpened(true);
                IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, CreateGestureGuideActivity.class);
            }

            @Override
            public void toggleToOff(View view) {
                int doubleValidation = PreferencesByUserAndTanentUtils.getInt(MyApplication.getInstance(), Constant.PREF_MNM_DOUBLE_VALIADATION, -1);
                if (doubleValidation != 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("gesture_code_change", "close");
                    IntentUtils.startActivity(SafeGustureFaceSettingActivity.this, GestureLoginActivity.class, bundle);
                } else {
                    guestureSwitchView.setOpened(true);
                    ToastUtils.show(SafeGustureFaceSettingActivity.this, R.string.setting_gesture_force_open);
                }


            }
        });
        faceSwitchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                intentFaceVerifyActivity(true);
            }

            @Override
            public void toggleToOff(View view) {
                intentFaceVerifyActivity(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isGestureOpen = isGestureOpen();
        if (guestureSwitchView.isOpened() != isGestureOpen) {
            guestureSwitchView.setOpened(isGestureOpen);
        }
        resetGuestureLayout.setVisibility(isGestureOpen() ? View.VISIBLE : View.GONE);

        faceSwitchView.setOpened(FaceVerifyActivity.getFaceVerifyIsOpenByUser(this));
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
                bundle.putString("gesture_code_change", "reset");
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
