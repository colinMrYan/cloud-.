package com.inspur.emmcloud.bean.contact;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/7/6.
 */

public class GetContactOrgListUpateResult {

    private Long lastQueryTime;
    private List<ContactOrg> contactOrgChangedList = new ArrayList<>();
    private List<String> contactOrgIdDeleteList = new ArrayList<>();
    private String rootID;

    public GetContactOrgListUpateResult(String response) {
        JSONObject obj = JSONUtils.getJSONObject(response);
        lastQueryTime = JSONUtils.getLong(obj, "lastQueryTime", 0L);
        JSONArray array = JSONUtils.getJSONArray(obj, "changed", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject contactOrgObj = JSONUtils.getJSONObject(array, i, new JSONObject());
            contactOrgChangedList.add(new ContactOrg(contactOrgObj));
        }
        JSONArray deleteArray = JSONUtils.getJSONArray(obj, "deleted", new JSONArray());
        for (int i = 0; i < deleteArray.length(); i++) {
            contactOrgIdDeleteList.add(JSONUtils.getString(deleteArray, i, ""));
        }
        rootID = JSONUtils.getString(obj, "rootID", null);
    }

    public Long getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(Long lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public List<ContactOrg> getContactOrgChangedList() {
        return contactOrgChangedList;
    }

    public void setContactOrgChangedList(List<ContactOrg> contactOrgChangedList) {
        this.contactOrgChangedList = contactOrgChangedList;
    }

    public List<String> getContactOrgIdDeleteList() {
        return contactOrgIdDeleteList;
    }

    public void setContactOrgIdDeleteList(List<String> contactOrgIdDeleteList) {
        this.contactOrgIdDeleteList = contactOrgIdDeleteList;
    }

    public String getRootID() {
        return rootID;
    }

    public void setRootID(String rootID) {
        this.rootID = rootID;
    }
}
