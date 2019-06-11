package com.inspur.emmcloud.bean.contact;

import com.inspur.emmcloud.bean.chat.ChannelGroup;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class GetSearchChannelGroupResult {
    private List<ChannelGroup> searchChannelGroupList = new ArrayList<ChannelGroup>();

    public GetSearchChannelGroupResult(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                ChannelGroup searchChannelGroup = new ChannelGroup(array.getJSONObject(i));
                searchChannelGroupList.add(searchChannelGroup);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public List<ChannelGroup> getSearchChannelGroupList() {
        return searchChannelGroupList;
    }
}
