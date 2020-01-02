package com.inspur.emmcloud.volume.bean;

/**
 * Created by yufuchang on 2018/3/13.
 */

public class GetVolumeGroupPermissionResult {
    private int privilege = -1;

    public GetVolumeGroupPermissionResult(String response) {

    }

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }
}
