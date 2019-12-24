package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * classes : com.inspur.emmcloud.bean.application.GetSearchAppResult
 * Create at 2017年1月17日 下午2:21:11
 */
public class GetSearchAppResult {
    private static final String TAG = "GetAllAppResult";
    private int total = -1;
    private List<App> allAppList = new ArrayList<App>();

    public GetSearchAppResult(String response) {
        try {
            JSONObject jObject = new JSONObject(response);
            if (jObject.has("total")) {
                this.total = jObject.getInt("total");
            }

            if (jObject.has("rows")) {
                JSONArray array = (JSONArray) jObject.get("rows");
                int len = array.length();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    allAppList.add(new App(obj));
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LogUtils.exceptionDebug(TAG, e.toString());
        }
    }

    public int getTotal() {
        return total;
    }

    public List<App> getAllAppList() {
        return allAppList;
    }

}
 