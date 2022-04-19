package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.componentservice.communication.ServiceChannelInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetServiceChannelInfoListResult {
    private List<ServiceChannelInfo> serviceChannelInfoList = new ArrayList<>();

    public GetServiceChannelInfoListResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
            ServiceChannelInfo conversation = new ServiceChannelInfo(obj);
            serviceChannelInfoList.add(conversation);
        }
    }

    public List<ServiceChannelInfo> getConversationList() {
        return serviceChannelInfoList;
    }

}
