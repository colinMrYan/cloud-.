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
    private List<VolumeFile> volumeFileDirectoryList = new ArrayList<>();
    private List<VolumeFile> volumeFileRegularList = new ArrayList<>();
    public GetVolumeFileListResult(String response){
        JSONArray array = JSONUtils.getJSONArray(response,new JSONArray());
        for (int i=0;i<array.length();i++){
            JSONObject object = JSONUtils.getJSONObject(array,i,new JSONObject());
            VolumeFile volumeFile = new VolumeFile(object);
            volumeFileList.add(volumeFile);
            if (volumeFile.getType().equals("directory")){
                volumeFileDirectoryList.add(volumeFile);
            }else {
                volumeFileRegularList.add(volumeFile);
            }
        }
    }

    public List<VolumeFile> getVolumeFileList() {
        return volumeFileList;
    }

    /**
     * 获取目录中所有的文件夹
     * @return
     */
    public List<VolumeFile> getVolumeFileDirectoryList() {
        return volumeFileDirectoryList;
    }

    public List<VolumeFile> getVolumeFileRegularList() {
        return volumeFileRegularList;
    }
}
