package com.inspur.emmcloud.web.api;

import com.inspur.emmcloud.web.bean.AppRedirectResult;

/**
 * Created by chenmch on 2019/6/14.
 */

public interface WebAPIInterface {
    void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult);

    void returnGetAppAuthCodeResultFail(String error, int errorCode);
}
