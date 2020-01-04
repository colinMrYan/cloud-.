package com.inspur.emmcloud.setting.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/15.
 */

public class GetBindingDeviceResult {
    private List<BindingDevice> currentDeviceList = new ArrayList<>();
    private List<BindingDevice> historyDeviceList = new ArrayList<>();

    public GetBindingDeviceResult(String response) {
        JSONArray currentDeviceArray = JSONUtils.getJSONArray(response, "currentDevices", new JSONArray());
        JSONArray historyDeviceArray = JSONUtils.getJSONArray(response, "historyDevices", new JSONArray());
        for (int i = 0; i < currentDeviceArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(currentDeviceArray, i, new JSONObject());
            currentDeviceList.add(new BindingDevice(obj));
        }
        for (int i = 0; i < historyDeviceArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(historyDeviceArray, i, new JSONObject());
            historyDeviceList.add(new BindingDevice(obj));
        }

    }

    public List<BindingDevice> getCurrentDeviceList() {
        return currentDeviceList;
    }

    public List<BindingDevice> getHistoryDeviceList() {
        return historyDeviceList;
    }
}
