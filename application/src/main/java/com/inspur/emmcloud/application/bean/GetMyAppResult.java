package com.inspur.emmcloud.application.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取工作中所有应用数据解析类
 *
 * @author Administrator
 */
public class GetMyAppResult {
    private static final String TAG = "GetMyAppResult";
    private List<App> myAppList = new ArrayList<App>();

    public GetMyAppResult(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                myAppList.add(new App(obj));
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public List<App> getMyAppList() {
        return myAppList;
    }

}
