package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetChannelListResult {
    private static final String TAG = "GetSessionListResult";
    private List<Channel> channelList = new ArrayList<Channel>();

    public GetChannelListResult(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                channelList.add(new Channel(obj));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LogUtils.exceptionDebug(TAG, e.toString());
        }
    }

    public List<Channel> getChannelList() {
        return channelList;
    }


}
