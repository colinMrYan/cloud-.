package com.inspur.emmcloud.volume.bean;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/1/24.
 */

public class GroupPrivilege {
    private String groupId;
    private int privilege;

    public GroupPrivilege() {

    }

    public GroupPrivilege(JSONObject obj) {
        groupId = "";
    }
}
