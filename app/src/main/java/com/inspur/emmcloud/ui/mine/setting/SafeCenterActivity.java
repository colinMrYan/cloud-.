package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

/**
 * 账号、设备安全
 */

public class SafeCenterActivity extends BaseActivity {

    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_safe_center;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_setting_safe_gesture_face:
                IntentUtils.startActivity(this, SafeGustureFaceSettingActivity.class);
                break;
            case R.id.rl_setting_face_unlock:
                intentFaceVerifyActivity(true);
                break;
            case R.id.rl_setting_safe_account_device:
                IntentUtils.startActivity(this, DeviceManagerActivity.class);
                break;
            default:
                break;
        }
    }

    /**
     * 打开设置人脸识别页面
     *
     * @param isFaceSettingOpen
     */
    private void intentFaceVerifyActivity(boolean isFaceSettingOpen) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFaceSettingOpen", isFaceSettingOpen);
        IntentUtils.startActivity(SafeCenterActivity.this, FaceVerifyActivity.class, bundle);
    }
}
