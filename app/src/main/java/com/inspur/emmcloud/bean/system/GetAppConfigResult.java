package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.bean.AppConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/10/10.
 */

public class GetAppConfigResult {
    private List<AppConfig> appConfigList = new ArrayList<>();

    public GetAppConfigResult(String response) {
//        isWebAutoRotate = JSONUtils.getBoolean(obj,"WebAutoRotate",false);
//        commonFunctionAppIDList = JSONUtils.getStringList(obj,"CommonFunctions",new ArrayList<String>());
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
            String key = JSONUtils.getString(obj, "key", null);
            String value = JSONUtils.getString(obj, "value", null);
            if (key != null && value != null) {
                appConfigList.add(new AppConfig(key, value));
            }
        }
    }

    public List<AppConfig> getAppConfigList() {
        return appConfigList;
    }
}


