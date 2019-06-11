package com.inspur.emmcloud.login.login;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.inspur.emmcloud.login.CoreService;


/**
 * Created by chenmch on 2019/5/31.
 */

public interface LoginService extends CoreService {
    void logout(Context context);

    void refreshToken(OauthCallBack callBack, long requestTime);

    void MDMCheck(Activity context, String tanent, String userCode, String userName, String state, boolean impActivity);

    void autoLogin(Activity activity, Handler handler);

    void setMDMStatusNoPass();
}
