package com.inspur.emmcloud.login.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/2/6.
 */

public class LoginMoreBean {
    private String url = "";
    private String name = "";

    public LoginMoreBean(String content) {
        url = JSONUtils.getString(content, "u", "");
        name = JSONUtils.getString(content, "n", "");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
