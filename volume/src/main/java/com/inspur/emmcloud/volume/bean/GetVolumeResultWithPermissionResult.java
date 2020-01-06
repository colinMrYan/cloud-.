package com.inspur.emmcloud.volume.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/3/1.
 */

public class GetVolumeResultWithPermissionResult {

    private List<Group> volumeGroupList = new ArrayList<>();

    public GetVolumeResultWithPermissionResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            volumeGroupList.add(new Group(JSONUtils.getJSONObject(array, i, new JSONObject())));
        }
    }

    public List<Group> getVolumeGroupList() {
        return volumeGroupList;
    }

    public void setVolumeGroupList(List<Group> volumeGroupList) {
        this.volumeGroupList = volumeGroupList;
    }
}
