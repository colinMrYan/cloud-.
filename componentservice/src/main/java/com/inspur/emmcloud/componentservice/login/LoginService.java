package com.inspur.emmcloud.componentservice.login;

import android.content.Context;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface LoginService extends CoreService {
    void logout(Context context);
}
