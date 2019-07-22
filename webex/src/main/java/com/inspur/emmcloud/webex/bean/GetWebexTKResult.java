package com.inspur.emmcloud.webex.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2018/10/13.
 */

public class GetWebexTKResult {
    private String tk;

    public GetWebexTKResult(String response) {
        tk = JSONUtils.getString(response, "tk", "");
    }

    public String getTk() {
        return tk;
    }
}
