package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {
 * "id": "2154771d55a54e40a78f567f4c2dc940",
 * "creator": "99999",
 * "channelType": 0,
 * "users": [{
 * "id": "99999",
 * "name": "于富昌",
 * "headImgUrl": "http://172.31.2.36:88/img/userhead/99999",
 * "agoraUid": 924600,
 * "token": "00636f73eb839f440a3a297a5c3b3977c13IAAQ64iSF6d7myIKrKo+JiFUSeniVi19LrcC1Vk3XNKRYuyWjdqr7GbvEADLnAQA0QB9WwEAAQAAAAAA",
 * "connectState": 0
 * }, {
 * "id": "11605",
 * "name": "苗传伟",
 * "headImgUrl": "http://172.31.2.36:88/img/userhead/11605",
 * "agoraUid": 712501,
 * "token": "",
 * "connectState": 0
 * }, {
 * "id": "257140",
 * "name": "单文政",
 * "headImgUrl": "http://172.31.2.36:88/img/userhead/257140",
 * "agoraUid": 669702,
 * "token": "",
 * "connectState": 0
 * }],
 * "state": 1
 * }
 * Created by yufuchang on 2018/8/15.
 */

public class GetVoiceCommunicationResult {

    private String channelId;
    private String creator;
    private String channelType;
    private int state;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList = new ArrayList<>();

    public GetVoiceCommunicationResult(String response) {
        channelId = JSONUtils.getString(response, "id", "");
        creator = JSONUtils.getString(response, "creator", "");
        channelType = JSONUtils.getString(response, "channelType", "");
        state = JSONUtils.getInt(response, "state", -1);
        JSONArray jsonArray = JSONUtils.getJSONArray(response, "users", new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            voiceCommunicationJoinChannelInfoBeanList.add(new VoiceCommunicationJoinChannelInfoBean(JSONUtils.getJSONObject(jsonArray, i, new JSONObject())));
        }
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<VoiceCommunicationJoinChannelInfoBean> getVoiceCommunicationJoinChannelInfoBeanList() {
        return voiceCommunicationJoinChannelInfoBeanList;
    }

    public void setVoiceCommunicationJoinChannelInfoBeanList(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList) {
        this.voiceCommunicationJoinChannelInfoBeanList = voiceCommunicationJoinChannelInfoBeanList;
    }
}
