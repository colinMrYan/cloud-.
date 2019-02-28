package com.inspur.emmcloud.ui.work.calendar;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

public class CalDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_detail);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;

            default:
                break;
        }
    }
}
