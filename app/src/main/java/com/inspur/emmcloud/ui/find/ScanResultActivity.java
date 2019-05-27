package com.inspur.emmcloud.ui.find;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ScanResultActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

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
