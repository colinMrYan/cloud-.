package com.inspur.emmcloud.volume.ui;

import android.os.Bundle;
import com.alibaba.android.arouter.facade.annotation.Route;;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;


/**
 * 云盘首页
 */
@Route(path = Constant.AROUTER_CLASS_VOLUME_HOME)
public class VolumeHomePageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_volume_homepage;
    }



}
