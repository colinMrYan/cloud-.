package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/15.
 */

public class GetVolumeListResult {
    private List<Volume> shareVolumeList = new ArrayList<>();
    private Volume myVolume;

    public GetVolumeListResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = JSONUtils.getJSONObject(array, i, new JSONObject());
            Volume volume = new Volume(object);
            String type = volume.getType();
            if (type.equals("public")) {
                shareVolumeList.add(volume);
            } else if (type.equals("private")) {
                myVolume = volume;
            }
        }
    }

    public List<Volume> getShareVolumeList() {
        return shareVolumeList;
    }

    public Volume getMyVolume() {
        return myVolume;
    }
}
