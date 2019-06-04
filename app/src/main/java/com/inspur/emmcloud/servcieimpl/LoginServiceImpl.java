package com.inspur.emmcloud.servcieimpl;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.privates.OauthUtils;

/**
 * Created by chenmch on 2019/6/3.
 */

public class LoginServiceImpl implements LoginService {
    @Override
    public void logout(Context context) {
        OauthUtils.getInstance().cancelToken();
        PreferencesUtils.putString(context, "accessToken", "");
        PreferencesUtils.putString(context, "refreshToken", "");
        MyApplication.getInstance().setAccessToken("");
        MyApplication.getInstance().setRefreshToken("");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, LoginActivity.class);
        context.startActivity(intent);
    }
}
