package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/1/19.
 * 透传消息实体
 */

public class TransparentBean {

    private int badgeNumber = 0;

    public TransparentBean(String transparentMsg) {
        this.badgeNumber = JSONUtils.getInt(transparentMsg, "badge", 0);
    }

    public int getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(int badgeNumber) {
        this.badgeNumber = badgeNumber;
    }
}
