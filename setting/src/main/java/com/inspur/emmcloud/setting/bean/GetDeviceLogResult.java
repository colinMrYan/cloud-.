package com.inspur.emmcloud.setting.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/8/17.
 */

public class GetDeviceLogResult {
    private List<BindingDeviceLog> bindingDeviceLogList = new ArrayList<>();

    public GetDeviceLogResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, "logs", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = JSONUtils.getJSONObject(array, i, new JSONObject());
            BindingDeviceLog bindingDeviceLog = new BindingDeviceLog(jsonObject);
            bindingDeviceLogList.add(bindingDeviceLog);
        }
    }

    public List<BindingDeviceLog> getBindingDeviceLogList() {
        return bindingDeviceLogList;
    }

}
