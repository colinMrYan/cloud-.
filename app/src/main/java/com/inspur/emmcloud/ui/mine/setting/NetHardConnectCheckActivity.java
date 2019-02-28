package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

/**
 * Created by libaochao on 2018/11/23.
 */

public class NetHardConnectCheckActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_hard_connect_check);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_show_system_hard_setting:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
        }
    }

}
