package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/11/10.
 */

public class GetRecommendAppWidgetListResult {
    private String response = "";
    private long expiredDate = 0L;
    private List<RecommendAppWidgetBean> recommendAppWidgetBeanList = new ArrayList<>();

    public GetRecommendAppWidgetListResult(String response) {
        this.response = response;
        expiredDate = JSONUtils.getLong(response, "expiredDate", 0);
        JSONArray jsonArray = JSONUtils.getJSONArray(response, "recommends", new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            recommendAppWidgetBeanList.add(new RecommendAppWidgetBean(JSONUtils.getJSONObject(jsonArray, i, new JSONObject())));
        }
    }


    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(long expiredDate) {
        this.expiredDate = expiredDate;
    }

    public List<RecommendAppWidgetBean> getRecommendAppWidgetBeanList() {
        return recommendAppWidgetBeanList;
    }

    public void setRecommendAppWidgetBeanList(List<RecommendAppWidgetBean> recommendAppWidgetBeanList) {
        this.recommendAppWidgetBeanList = recommendAppWidgetBeanList;
    }
}
