package com.inspur.emmcloud.setting.ui.setting;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;

/**
 * 服务条款页面
 *
 * @author Administrator
 */
@Route(path = Constant.AROUTER_CLASS_SETTING_SERVICE_TERM)
public class ServiceTermActivity extends BaseActivity {

    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_service_term_activity;
    }

    public void onClick(View v) {
        finish();
    }
}
