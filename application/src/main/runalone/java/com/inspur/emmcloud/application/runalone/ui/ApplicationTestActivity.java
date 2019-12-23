package com.inspur.emmcloud.application.runalone.ui;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.ui.MyAppFragment;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

/**
 * Created by: yufuchang
 * Date: 2019/12/16
 */
@Route(path = Constant.AROUTER_CLASS_APPCENTER_TEST)
public class ApplicationTestActivity extends BaseActivity {
    MyAppFragment fragment;

    @Override
    public void onCreate() {
        fragment = new MyAppFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_app_test;
    }
}
