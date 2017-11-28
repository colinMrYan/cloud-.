package com.inspur.emmcloud.bean.Volume;

import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/16.
 */

public class GetVolumeFileListResult {
    private List<VolumeFile> volumeFileList = new ArrayList<>();
    private List<VolumeFile> volumeFileDirectortList = new ArrayList<>();
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

    /**
     * 获取目录中所有的文件夹
     * @return
     */
    public List<VolumeFile> getVolumeFileDirectortList() {
        LogUtils.jasonDebug("0000000000000000000000");
        for (int i=0;i<volumeFileList.size();i++){
            VolumeFile volumeFile = volumeFileList.get(i);
            LogUtils.jasonDebug("volumeFile.getType()"+volumeFile.getType());
            if (volumeFile.getType().equals("directory")){
                volumeFileDirectortList.add(volumeFile);

            }
        }
        return volumeFileDirectortList;
    }
}
