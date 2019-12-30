package com.inspur.emmcloud.setting.runalone.application;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.ui.MoreFragment;

@Route(path = Constant.AROUTER_CLASS_SETTING_TEST)
public class SettingModelMainActivity extends BaseActivity {

    MoreFragment moreFragment;

    @Override
    public void onCreate() {
        moreFragment = new MoreFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container_test1, moreFragment).commitAllowingStateLoss();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_model_main_activity;
    }
}
