package com.inspur.emmcloud.application.bean;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * classes : com.inspur.emmcloud.bean.appcenter.GetAppGroupResult
 * Create at 2016年12月14日 下午7:49:58
 */
public class GetAppGroupResult {

    private List<AppGroupBean> appGroupBeanList = new ArrayList<AppGroupBean>();

    public GetAppGroupResult(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                AppGroupBean appGroupBean = new AppGroupBean(jsonArray.getJSONObject(i).toString());
                appGroupBeanList.add(appGroupBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AppGroupBean> getAppGroupBeanList() {
        return appGroupBeanList;
    }
}
 