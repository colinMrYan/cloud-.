package com.inspur.emmcloud.setting.ui.setting;

import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;

/**
 * Created by libaochao on 2018/11/23.
 */

public class NetHardConnectCheckActivity extends BaseActivity {

    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_net_hard_connect_check_activity;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.tv_show_system_hard_setting) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));

        }
    }

}
