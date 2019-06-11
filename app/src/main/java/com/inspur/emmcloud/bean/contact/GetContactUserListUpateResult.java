package com.inspur.emmcloud.bean.contact;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/7/6.
 */

public class GetContactUserListUpateResult {
    private Long lastQueryTime;
    private List<ContactUser> contactUserChangedList = new ArrayList<>();
    private List<String> contactUserIdDeleteList = new ArrayList<>();

    public GetContactUserListUpateResult(String response) {

        JSONObject obj = JSONUtils.getJSONObject(response);
        lastQueryTime = JSONUtils.getLong(obj, "lastQueryTime", 0L);
        JSONArray array = JSONUtils.getJSONArray(obj, "changed", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject contactUserObj = JSONUtils.getJSONObject(array, i, new JSONObject());
            contactUserChangedList.add(new ContactUser(contactUserObj, lastQueryTime + ""));
        }
        JSONArray deleteArray = JSONUtils.getJSONArray(obj, "deleted", new JSONArray());
        for (int i = 0; i < deleteArray.length(); i++) {
            contactUserIdDeleteList.add(JSONUtils.getString(deleteArray, i, ""));
        }
    }

    public Long getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(Long lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public List<ContactUser> getContactUserChangedList() {
        return contactUserChangedList;
    }

    public void setContactUserChangedList(List<ContactUser> contactUserChangedList) {
        this.contactUserChangedList = contactUserChangedList;
    }

    public List<String> getContactUserIdDeleteList() {
        return contactUserIdDeleteList;
    }

    public void setContactUserIdDeleteList(List<String> contactUserIdDeleteList) {
        this.contactUserIdDeleteList = contactUserIdDeleteList;
    }
}
