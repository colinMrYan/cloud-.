package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/1/20.
 */

public class Group {
    private String id;
    private String name;
    private String owner;
    private int privilege;
    private List<String>  memberUidList = new ArrayList<>();

    public Group(JSONObject obj){
        id = JSONUtils.getString(obj,"id","");
        name = JSONUtils.getString(obj,"name","");
        owner = JSONUtils.getString(obj,"owner","");
        privilege = JSONUtils.getInt(obj,"privilege",0);
        JSONArray array = JSONUtils.getJSONArray(obj,"members",new JSONArray());
        for (int i=0;i<array.length();i++){
            String uid = JSONUtils.getString(array,i,"");
            memberUidList.add(uid);
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }

    public List<String> getMemberUidList() {
        return memberUidList;
    }

    public void setMemberUidList(List<String> memberUidList) {
        this.memberUidList = memberUidList;
    }
}
