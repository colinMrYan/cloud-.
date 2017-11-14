package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by yufuchang on 2017/11/14.
 */

public class RecommendAppWidgetBean {

    /**
     * period : 9
     * appIdList : ["app11","app51"]
     */

    private String period;
    private List<String> appIdList;

    public RecommendAppWidgetBean(JSONObject response) {
        try {
            period = JSONUtils.getString(response, "Period", "");
            JSONArray jsonArray = JSONUtils.getJSONArray(response, "AppIds", new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                appIdList.add(jsonArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void setAppIdList(List<String> AppIds) {
        this.appIdList = AppIds;
    }
}
