package com.inspur.emmcloud.bean;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2017/10/11.
 * [{"appId":"inspur_news_esg","badge":2}]
 */

public class GetAppBadgeResult {
    private Map<String,AppBadgeBean> appBadgeBeanMap = new HashMap<>();
    private int tabBadgeNumber = 0;
    public GetAppBadgeResult(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++){
                AppBadgeBean appBadgeBean = new AppBadgeBean(jsonArray.getJSONObject(i));
                appBadgeBeanMap.put(appBadgeBean.getAppId(),appBadgeBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, AppBadgeBean> getAppBadgeBeanMap() {
        return appBadgeBeanMap;
    }

    public void setAppBadgeBeanMap(Map<String, AppBadgeBean> appBadgeBeanMap) {
        this.appBadgeBeanMap = appBadgeBeanMap;
    }

    public int getTabBadgeNumber() {
        return tabBadgeNumber;
    }

    public void setTabBadgeNumber(int tabBadgeNumber) {
        this.tabBadgeNumber = tabBadgeNumber;
    }
}
