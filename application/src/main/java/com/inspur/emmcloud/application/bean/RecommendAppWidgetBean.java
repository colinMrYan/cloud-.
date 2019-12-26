package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/11/14.
 */

public class RecommendAppWidgetBean {

    /**
     * period : 9
     * appIdList : ["app11","app51"]
     */

    private String period = "";
    private List<String> appIdList = new ArrayList<>();

    public RecommendAppWidgetBean(JSONObject response) {
        period = JSONUtils.getString(response, "period", "");
        JSONArray jsonArray = JSONUtils.getJSONArray(response, "appIds", new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            appIdList.add(JSONUtils.getString(jsonArray, i, ""));
        }
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String Period) {
        this.period = Period;
    }

    public List<String> getAppIdList() {
        return appIdList;
    }

    public void setAppIdList(List<String> appIdList) {
        this.appIdList = appIdList;
    }
}
