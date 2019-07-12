package com.inspur.emmcloud.componentservice.communication;

/**
 * Created by chenmch on 2019/7/10.
 */

public interface ShareToConversationListener {
    void shareSuccess(String cid);

    void shareFail();

    void shareCancel();
}
