package com.inspur.emmcloud.bean.system;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * 版本返回解析类
 */
public class GetUpgradeResult implements Serializable {

    private static final String TAG = "GetVersionUpdateResult";
    private String resultText = "";
    private Map<String, Object> resultMap;
    private int upgradeCode = -1; // 0 无需升级 1 可选升级 2 必须升级
    private String upgradeUrl = "";
    private String upgradeMsg = "";
    private String changeLog = "";
    private String latestVersion = "";

    public GetUpgradeResult(String response) {
        try {
            JSONObject jObject = new JSONObject(response);
            if (jObject.has("statusCode")) {
                upgradeCode = jObject.getInt("statusCode");
            }
            if (jObject.has("upgradeUrl")) {
                upgradeUrl = jObject.getString("upgradeUrl");
            }
            if (jObject.has("upgradeMsg")) {
                upgradeMsg = jObject.getString("upgradeMsg");
            }
            if (jObject.has("changelog")) {
                changeLog = jObject.getString("changelog");
            }
            if (jObject.has("latestVersion")) {
                latestVersion = jObject.getString("latestVersion");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getChangeLog() {
        return changeLog;
    }

    public String getUpgradeMsg() {
        return upgradeMsg;
    }

    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    public int getUpgradeCode() {
        return upgradeCode;
    }

    public String getresultText() {
        return resultText;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

}
