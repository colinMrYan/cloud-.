package com.inspur.emmcloud.web.plugin.barcode;

import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.web.R;

@Route(path = Constant.AROUTER_CLASS_WEB_SCANRESULT)
public class ScanResultActivity extends BaseActivity {

    @Override
    public void onCreate() {
        String result = getIntent().getExtras().getString("result");
        ((TextView) findViewById(R.id.scan_result_text)).setText(result);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.web_activity_scan_result;
    }

    public void onClick(View v) {
        finish();
    }
}
