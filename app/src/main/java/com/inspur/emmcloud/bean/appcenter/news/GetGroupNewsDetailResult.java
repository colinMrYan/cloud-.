package com.inspur.emmcloud.bean.appcenter.news;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetGroupNewsDetailResult {


    private List<GroupNews> groupNewsList = new ArrayList<>();

    public GetGroupNewsDetailResult(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("content")) {
                JSONArray jsonArray = jsonObject.getJSONArray("content");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (!TextUtils.isEmpty(jsonObject.toString())) {
                        groupNewsList.add(new GroupNews(jsonObject));
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<GroupNews> getGroupNews() {
        return groupNewsList;
    }
}
