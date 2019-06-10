package com.inspur.imp.plugin.barcode;

import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

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
