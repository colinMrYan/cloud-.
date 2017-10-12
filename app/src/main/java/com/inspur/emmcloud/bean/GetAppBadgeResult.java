package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.LogUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/10/11.
 */

public class GetAppBadgeResult {
    private List<AppBadgeBean> appBadgeList = new ArrayList<>();
    public GetAppBadgeResult(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++){
                appBadgeList.add(new AppBadgeBean(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AppBadgeBean> getAppBadgeList() {
        return appBadgeList;
    }

    public void setAppBadgeList(List<AppBadgeBean> appBadgeList) {
        this.appBadgeList = appBadgeList;
    }
}
