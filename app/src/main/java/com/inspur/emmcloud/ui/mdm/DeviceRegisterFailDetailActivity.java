package com.inspur.emmcloud.ui.mdm;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.util.privates.MDM.MDM;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.api.Res;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        return Res.getLayoutID("mdm_activity_device_register_fail_detail");
    }

    public void onClick(View v) {
        if (v.getId() == Res.getWidgetID("ibt_back")) {
            onBackPressed();
        } else if (v.getId() == Res.getWidgetID("register_btn")) {
            Intent intent = new Intent();
            intent.setClass(this, ImpActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("appName", getString(Res.getStringID("device_registe")));
            bundle.putString("function", "mdm");
            bundle.putString("uri", APIUri.getDeviceRegisterUrl(this));
            intent.putExtras(bundle);
            startActivity(intent);
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
