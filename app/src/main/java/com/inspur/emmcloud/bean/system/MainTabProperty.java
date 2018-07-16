package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class MainTabProperty {
    /**
     * "properties": {
     * "canContact": "false",
     * "canCreate": "true"
     * }
     */
    private boolean canContact = true;
    private boolean canCreate = true;
    private boolean isHaveNavbar = false;

    public MainTabProperty(String response) {
        canContact = JSONUtils.getBoolean(response, "canOpenContact", true);
        canCreate = JSONUtils.getBoolean(response, "canCreateChannel", true);
        isHaveNavbar = JSONUtils.getBoolean(response,"isHaveNavbar",false);
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

    public boolean isHaveNavbar() {
        return isHaveNavbar;
    }

    public void setHaveNavbar(boolean haveNavbar) {
        isHaveNavbar = haveNavbar;
    }
}
