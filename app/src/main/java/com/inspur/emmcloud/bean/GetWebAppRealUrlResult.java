package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

/**
 * Created by chenmch on 2017/11/1.
 */

public class GetWebAppRealUrlResult {
    private String url= "";
    public GetWebAppRealUrlResult(String response){
        url = JSONUtils.getString(response,"uri","");

    }

    public String getUrl() {
        return url;
    }

}
