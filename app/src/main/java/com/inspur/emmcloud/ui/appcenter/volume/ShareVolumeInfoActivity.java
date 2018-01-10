package com.inspur.emmcloud.ui.appcenter.volume;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;


/**
 * 共享网盘详情页面
 */

@ContentView(R.layout.activity_share_volumel_info)
public class ShareVolumeInfoActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            default:
                break;
        }
    }
}
