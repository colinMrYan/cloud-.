package com.inspur.emmcloud.login.login;

import android.content.Context;

import com.inspur.emmcloud.login.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface LoginService extends CoreService {
    void logout(Context context);

    void refreshToken(OauthCallBack callBack, long requestTime);
}
