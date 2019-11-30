
package com.inspur.emmcloud.web.runalone.ui;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;

/**
 * Created by chenmch on 2019/6/14.
 */

@Route(path = Constant.AROUTER_CLASS_WEB_MAIN_TEST)
public class WebTestMainActivity extends BaseActivity {
    @Override
    public void onCreate() {
//        Intent intent = new Intent(this, ImpActivity.class);
//        intent.putExtra("uri", "http://emm.inspuronline.com:83/JSApi/index.html");
//        intent.putExtra(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
//        startActivity(intent);

        AppUtils.openCamera(this, "a.jpg", 1);
        finish();
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }
}
