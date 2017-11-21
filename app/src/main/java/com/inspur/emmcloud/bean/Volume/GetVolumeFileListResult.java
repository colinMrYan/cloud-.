package com.inspur.emmcloud.bean.Volume;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/16.
 */

public class GetVolumeFileListResult {
    private List<VolumeFile> volumeFileList = new ArrayList<>();
    public GetVolumeFileListResult(String response){
        JSONArray array = JSONUtils.getJSONArray(response,new JSONArray());
        for (int i=0;i<array.length();i++){
            JSONObject object = JSONUtils.getJSONObject(array,i,new JSONObject());
            volumeFileList.add(new VolumeFile(object));
        }
    }

    public List<VolumeFile> getVolumeFileList() {
        return volumeFileList;
    }
}
