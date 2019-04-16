package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

public class GetIsMeetingAdminResult {

    private boolean isAdmin = false;

    public GetIsMeetingAdminResult(String response) {
        isAdmin = JSONUtils.getBoolean(response, "isAdmin", false);
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
