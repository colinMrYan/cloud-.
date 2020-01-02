package com.inspur.emmcloud.volume.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * 云盘中包含我的群组
 */


@Table(name = "VolumeGroupContainMe")
public class VolumeGroupContainMe {
    @Column(name = "volumeId", isId = true)
    private String volumeId;

    @Column(name = "groupIds")
    private String groupIds;

    private List<String> groupIdList = new ArrayList<>();

    public VolumeGroupContainMe() {
    }

    public VolumeGroupContainMe(String volumeId, List<String> groupIdList) {
        this.groupIds = JSONUtils.toJSONString(groupIdList);
        this.volumeId = volumeId;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public List<String> getGroupIdList() {
        groupIdList = JSONUtils.JSONArray2List(groupIds, new ArrayList<String>());
        return groupIdList;
    }

    public void setGroupIdList(List<String> groupIdList) {
        this.groupIds = JSONUtils.toJSONString(groupIdList);
        this.groupIdList = groupIdList;
    }
}
