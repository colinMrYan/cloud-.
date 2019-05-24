package com.inspur.emmcloud.ui.mine.setting;

import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.IntentUtils;

/**
 * 账号、设备安全
 */

public class SafeCenterActivity extends BaseActivity {

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
            case R.id.rl_setting_safe_account_device:
                IntentUtils.startActivity(this, DeviceManagerActivity.class);
                break;
            default:
                break;
        }
    }
}
