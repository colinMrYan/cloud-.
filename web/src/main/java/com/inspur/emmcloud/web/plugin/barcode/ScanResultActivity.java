package com.inspur.emmcloud.web.plugin.barcode;

import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.web.R;

public class ScanResultActivity extends BaseActivity {

    @Override
    public void onCreate() {
        String result = getIntent().getExtras().getString("result");
        ((TextView) findViewById(R.id.scan_result_text)).setText(result);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_scan_result;
    }

    public void onClick(View v) {
        finish();
    }
}
