package com.inspur.emmcloud.ui.login;

import android.os.Bundle;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.StateBarUtils;

import org.xutils.view.annotation.ContentView;

/**
 * 短信登录
 */

@ContentView(R.layout.activity_login_via_sms)
public class LoginViaSmsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.translucent(this,R.color.white);
        StateBarUtils.setStateBarTextColor( this,true );
    }
}
