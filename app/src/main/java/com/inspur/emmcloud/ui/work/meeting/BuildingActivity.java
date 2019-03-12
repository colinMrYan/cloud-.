package com.inspur.emmcloud.ui.work.meeting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

/**
 * 建筑位置图片
 *
 * @author sunqx
 */
public class BuildingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_building);
        ((TextView) (findViewById(R.id.header_text))).setText(getIntent().getStringExtra("building"));
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
