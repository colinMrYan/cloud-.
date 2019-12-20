package com.inspur.emmcloud.schedule.bean.meeting;

import com.inspur.emmcloud.baselib.util.JSONUtils;

public class GetIsMeetingAdminResult {

    private boolean isAdmin = false;

    public GetIsMeetingAdminResult(String response) {
        isAdmin = JSONUtils.getBoolean(response, "isAdmin", false);
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
