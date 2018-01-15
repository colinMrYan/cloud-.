package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/16.
 */

public class GetVolumeFileListResult {
    private String name = "";
    private String owner = "";
    private String id = "";
    private int ownerPrivilege = 0;
    private int othersPrivilege = 0;
    private List<VolumeFile> volumeFileList = new ArrayList<>();
    private List<VolumeFile> volumeFileDirectoryList = new ArrayList<>();
    private List<VolumeFile> volumeFileRegularList = new ArrayList<>();
    public GetVolumeFileListResult(String response){
        this.name = JSONUtils.getString(response,"name","");
        this.owner = JSONUtils.getString(response,"owner","");
        this.id = JSONUtils.getString(response,"id","");
        this.ownerPrivilege = JSONUtils.getInt(response,"ownerPrivilege",0);
        this.othersPrivilege = JSONUtils.getInt(response,"othersPrivilege",0);
        JSONArray array = JSONUtils.getJSONArray(response,"children",new JSONArray());
        for (int i=0;i<array.length();i++){
            JSONObject object = JSONUtils.getJSONObject(array,i,new JSONObject());
            VolumeFile volumeFile = new VolumeFile(object);
            volumeFileList.add(volumeFile);
            if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)){
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

    public List<VolumeFile> getVolumeFileFilterList(String filterType){
        List<VolumeFile> volumeFileFilterList;
        List<VolumeFile> volumeFileListFilterDocunemnt = new ArrayList<>();
        List<VolumeFile> volumeFileListFilterImage = new ArrayList<>();
        List<VolumeFile> volumeFileListFilterAudio = new ArrayList<>();
        List<VolumeFile> volumeFileListFilterVideo = new ArrayList<>();
        List<VolumeFile> volumeFileListFilterOther = new ArrayList<>();
        for (int i=0;i<volumeFileRegularList.size();i++){
            VolumeFile volumeFile = volumeFileRegularList.get(i);
            String format = volumeFile.getFormat();
            if (format.startsWith("application/vnd.openxmlformats-officedocument") || format.equals("text/plain") || format.equals("application/pdf")){
                volumeFileListFilterDocunemnt.add(volumeFile);
            }else if(format.startsWith("image/")){
                volumeFileListFilterImage.add(volumeFile);
            }else if(format.startsWith("audio/")){
                volumeFileListFilterAudio.add(volumeFile);
            }else if(format.startsWith("video/")){
                volumeFileListFilterVideo.add(volumeFile);
            }else {
                volumeFileListFilterOther.add(volumeFile);
            }
        }

        switch (filterType){
            case VolumeFile.FILTER_TYPE_DOCUNMENT:
                volumeFileFilterList = volumeFileListFilterDocunemnt;
                break;
            case VolumeFile.FILTER_TYPE_AUDIO:
                volumeFileFilterList = volumeFileListFilterAudio;
                break;
            case VolumeFile.FILTER_TYPE_VIDEO:
                volumeFileFilterList = volumeFileListFilterVideo;
                break;
            case VolumeFile.FILTER_TYPE_IMAGE:
                volumeFileFilterList = volumeFileListFilterImage;
                break;
            default:
                volumeFileFilterList = volumeFileListFilterOther;
                break;
        }
        return volumeFileFilterList;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOwnerPrivilege() {
        return ownerPrivilege;
    }

    public void setOwnerPrivilege(int ownerPrivilege) {
        this.ownerPrivilege = ownerPrivilege;
    }

    public int getOthersPrivilege() {
        return othersPrivilege;
    }

    public void setOthersPrivilege(int othersPrivilege) {
        this.othersPrivilege = othersPrivilege;
    }

    public boolean getHaveModifyPrivilege(){
        return owner.equals(MyApplication.getInstance().getUid()) || (othersPrivilege > 5);
    }
}
