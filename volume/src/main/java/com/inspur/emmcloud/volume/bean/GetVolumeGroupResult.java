package com.inspur.emmcloud.volume.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/1/25.
 */

public class GetVolumeGroupResult {
    private List<String> groupIdList = new ArrayList<>();

    public GetVolumeGroupResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
            String id = JSONUtils.getString(obj, "id", "");
            if (!StringUtils.isBlank(id)) {
                groupIdList.add(id);
            }
        }
    }

    public List<String> getGroupIdList() {
        return groupIdList;
    }
}
