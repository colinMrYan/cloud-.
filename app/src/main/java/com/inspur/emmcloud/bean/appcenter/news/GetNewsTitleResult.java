package com.inspur.emmcloud.bean.appcenter.news;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GetNewsTitleResult implements Serializable {

    private List<NewsTitle> titleList = new ArrayList<>();

    public GetNewsTitleResult(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                titleList.add(new NewsTitle(jsonObject));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<NewsTitle> getTitleList() {
        return titleList;
    }
}
