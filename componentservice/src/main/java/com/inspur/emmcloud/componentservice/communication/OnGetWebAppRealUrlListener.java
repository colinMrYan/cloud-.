package com.inspur.emmcloud.componentservice.communication;

/**
 * Created by: yufuchang
 * Date: 2019/12/16
 */
public interface OnGetWebAppRealUrlListener {
    void getWebAppRealUrlSuccess(String webAppUrl);

    void getWebAppRealUrlFail();
}
