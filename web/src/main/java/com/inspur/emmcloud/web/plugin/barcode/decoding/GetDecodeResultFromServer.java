package com.inspur.emmcloud.web.plugin.barcode.decoding;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by chenmch on 2018/7/13.
 */

public class GetDecodeResultFromServer {
    private String data;

    public GetDecodeResultFromServer(String response) {
        JSONArray dataArray = JSONUtils.getJSONArray(response, "data", null);
        if (dataArray != null && dataArray.length() > 0) {
            JSONObject object = JSONUtils.getJSONObject(dataArray, 0, null);
            if (object != null) {
                data = JSONUtils.getString(object, "data", null);
            }

        }
    }

    public String getData() {
        return data;
    }
}
