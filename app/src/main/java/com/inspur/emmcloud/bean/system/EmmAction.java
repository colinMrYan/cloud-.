package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * Created by yufuchang on 2018/11/23.
 */

public class EmmAction {

    /**
     * type : open-url
     * url : ...
     */

    private String action;
    private String type;
    private String url;

    public EmmAction(String action) {
        this.action = action;
        type = JSONUtils.getString(action, "type", "");
        url = JSONUtils.getString(action, "url", "");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getCanOpenAction() {
        if (!StringUtils.isBlank(action) && getType().equals(Constant.SF_OPEN_URL)) {
            return true;
        }
        return false;
    }
}
