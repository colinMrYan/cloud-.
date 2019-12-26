package com.inspur.emmcloud.setting.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2018/11/21.
 */

public class GetExperienceUpgradeFlagResult {
    private int status = 0;

    public GetExperienceUpgradeFlagResult(String response) {
        status = JSONUtils.getInt(response, "state", 0);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
