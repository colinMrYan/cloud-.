package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.util.privates.FingerPrintUtils;

/**
 * 账号、设备安全
 */

public class SafeCenterActivity extends BaseActivity {


    @Override
    public void onCreate() {
        ((TextView) findViewById(R.id.tv_setting_safe_gesture_face)).setText(FingerPrintUtils.getFingerPrintInstance().isFingerPrintAvaiable(this) ?
                R.string.setting_safe_gesture_face : R.string.safe_center_gesture);
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
//        IntentUtils.startActivity(SafeCenterActivity.this, FaceVerifyActivity.class, bundle);
        if (CreateGestureActivity.getGestureCodeIsOpenByUser(this)) {
            IntentUtils.startActivity(SafeCenterActivity.this, FaceSettingActivity.class, bundle);
        } else {
            IntentUtils.startActivity(SafeCenterActivity.this, CreateGestureActivity.class, bundle);
        }

    }
}
