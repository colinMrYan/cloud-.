package com.inspur.emmcloud.bean.appcenter.volume;


import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/1/20.
 */

public class VolumeDetail {

    private String id;
    private String name;
    private ArrayList<String> memberUidList = new ArrayList<>();
    private String owner;
    private List<Group> groupWriteList = new ArrayList<>();
    private List<Group> groupReadList = new ArrayList<>();


    public VolumeDetail(String response) {
        JSONObject obj = JSONUtils.getJSONObject(response);
        id = JSONUtils.getString(obj, "id", "");
        name = JSONUtils.getString(obj, "name", "");
        owner = JSONUtils.getString(obj, "owner", "");
        JSONArray memberArray = JSONUtils.getJSONArray(obj, "members", new JSONArray());
        for (int i = 0; i < memberArray.length(); i++) {
            String uid = JSONUtils.getString(memberArray, i, "");
            memberUidList.add(uid);
        }
        JSONArray groupArray = JSONUtils.getJSONArray(obj, "groups", new JSONArray());
        for (int i = 0; i < groupArray.length(); i++) {
            JSONObject groupObj = JSONUtils.getJSONObject(groupArray, i, new JSONObject());
            Group group = new Group(groupObj);
            if (group.getPrivilege() > 4) {
                groupWriteList.add(group);
            } else {
                groupReadList.add(group);
            }
        }
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getMemberUidList() {
        return memberUidList;
    }

    public void setMemberUidList(ArrayList<String> memberUidList) {
        this.memberUidList = memberUidList;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Group> getGroupWriteList() {
        return groupWriteList;
    }

    public void setGroupWriteList(List<Group> groupWriteList) {
        this.groupWriteList = groupWriteList;
    }

    public List<Group> getGroupReadList() {
        return groupReadList;
    }

    public void setGroupReadList(List<Group> groupReadList) {
        this.groupReadList = groupReadList;
    }
}