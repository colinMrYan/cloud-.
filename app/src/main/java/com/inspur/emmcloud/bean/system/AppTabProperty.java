package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class AppTabProperty {
    /**
     * "properties": {
     * "canContact": "false",
     * "canCreate": "true"
     * }
     */
    private boolean canContact = true;
    private boolean canCreate = true;

    public AppTabProperty(String response) {
        canContact = JSONUtils.getBoolean(response, "canOpenContact", true);
        canCreate = JSONUtils.getBoolean(response, "canCreateChannel", true);
    }

    public boolean isCanContact() {
        return canContact;
    }

    public void setCanContact(boolean canContact) {
        this.canContact = canContact;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }
}
