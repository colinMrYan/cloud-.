package com.inspur.mvp_demo.meeting.model.api;

import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;

public interface ApiService {
    interface IMeetingHistory {
        void getMeetingList(BaseModuleAPICallback apiCallback, int pageNum);
    }
}
