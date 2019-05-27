package com.inspur.emmcloud.ui.mine.setting;

import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

/**
 * 服务条款页面
 *
 * @author Administrator
 */
public class ServiceTermActivity extends BaseActivity {

    @Override
    public int getLayoutResId() {
        return R.layout.activity_service_term;
    }

    public void onClick(View v) {
        finish();
    }
}
