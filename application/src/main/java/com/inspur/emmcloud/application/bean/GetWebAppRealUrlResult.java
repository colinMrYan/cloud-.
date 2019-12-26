package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2017/11/1.
 */

public class GetWebAppRealUrlResult {
    private String url = "";

    public GetWebAppRealUrlResult(String response) {
        url = JSONUtils.getString(response, "uri", "");

    }

    public String getUrl() {
        return url;
    }

}
