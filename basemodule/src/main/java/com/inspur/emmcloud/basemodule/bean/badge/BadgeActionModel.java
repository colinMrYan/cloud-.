package com.inspur.emmcloud.basemodule.bean.badge;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class BadgeActionModel {


    /**
     * method : put
     * path : /unread-count
     */

    private String method;
    private String path;

    public BadgeActionModel(String action) {
        method = JSONUtils.getString(action, "method", "");
        path = JSONUtils.getString(action, "path", "");
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
