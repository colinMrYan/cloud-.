
package com.inspur.emmcloud.web.runalone.ui;

import android.content.Intent;

import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.web.ui.ImpActivity;

/**
 * Created by chenmch on 2019/6/14.
 */

public class WebTestMainActivity extends BaseActivity {
    @Override
    public void onCreate() {
        Intent intent = new Intent(this, ImpActivity.class);
        intent.putExtra("uri", "http://emm.inspuronline.com:83/JSApi/index.html");
        intent.putExtra(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
        startActivity(intent);
        finish();
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }
}
