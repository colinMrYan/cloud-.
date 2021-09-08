package com.inspur.emmcloud.basemodule.bean.badge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chenhao on 2021/8/30.
 * 角标服务App角标逻辑
 */

public class AppBadgeModel {

    //[{"appId":"0c1f0660-0c57-11eb-a4a6-171a7207d05c","badge":9},{"appId":"87c05b30-0931-11eb-be02-1554c79bd8ba","badge":0}]
    private Map<String, Integer> mAppBadgeMap = new HashMap<>();
    private String body = "";
    private int appBadgeCount = 0;

    public AppBadgeModel(String body) {
        this.body = body;
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(body);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String appId = jsonObject.getString("appId");
                int num = jsonObject.optInt("badge", 0);
                mAppBadgeMap.put(appId, num);
                appBadgeCount += num;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Integer> getAppBadgeMap() {
        return mAppBadgeMap;
    }


    public String getBody() {
        return body;
    }

    public int getAppBadgeCount() {
        return appBadgeCount;
    }
}
