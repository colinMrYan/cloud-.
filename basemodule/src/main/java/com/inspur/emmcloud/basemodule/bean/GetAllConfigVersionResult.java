package com.inspur.emmcloud.basemodule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by chenmch on 2018/7/25.
 */

public class GetAllConfigVersionResult {
    private JSONObject allConfigVersionObj = new JSONObject();

    public GetAllConfigVersionResult(String response, JSONObject localVersionObj) {
        JSONObject object = JSONUtils.getJSONObject(response);
        Iterator iterator = object.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = JSONUtils.getString(object, key, "");
            try {
                localVersionObj.put(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        allConfigVersionObj = localVersionObj;
    }

    public GetAllConfigVersionResult(String response) {
        allConfigVersionObj = JSONUtils.getJSONObject(response);
    }

    public String getItemVersion(ClientConfigItem clientConfigItem) {
        String itemVersion = "";
        switch (clientConfigItem) {
            case CLIENT_CONFIG_ROUTER:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "router", "");
                break;
            case CLIENT_CONFIG_MAINTAB:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "maintab", "");
                break;
            case CLIENT_CONFIG_SPLASH:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "ad", "");
                break;
            case CLIENT_CONFIG_LANGUAGE:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "lang", "");
                break;
            case CLIENT_CONFIG_MY_APP:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "app", "");
                break;
            case CLIENT_CONFIG_CONTACT_USER:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "contact_user", "");
                break;
            case CLIENT_CONFIG_CONTACT_ORG:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "contact_org", "");
                break;
            case CLIENT_CONFIG_NAVI_TAB:
                itemVersion = JSONUtils.getString(allConfigVersionObj, "multipleLayout", "");
                break;
            default:
                break;
        }
        return itemVersion;

    }


    public String getResponse() {
        return allConfigVersionObj.toString();
    }
}
