package com.inspur.emmcloud.login.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.login.util.MDM.MDM;

public class DeviceRegisterFailDetailActivity extends BaseActivity {
    private Bundle bundle;

    @Override
    public void onCreate() {
        bundle = getIntent().getExtras().getBundle("bundle");
        String message = bundle.getString("message");
        ((TextView) findViewById(Res.getWidgetID("reason_text")))
                .setText(message);
    }

    @Override
    public int getLayoutResId() {
        return Res.getLayoutID("login_activity_device_register_fail_detail");
    }

    public void onClick(View v) {
        if (v.getId() == Res.getWidgetID("ibt_back")) {
            onBackPressed();
        } else if (v.getId() == Res.getWidgetID("register_btn")) {
            ARouter.getInstance().build("/web/main").with(bundle).navigation();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        finish();
        new MDM().getMDMListener().MDMStatusNoPass();
    }

}
