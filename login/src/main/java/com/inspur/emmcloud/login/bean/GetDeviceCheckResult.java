package com.inspur.emmcloud.login.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * 登录信息返回解析类
 */
public class GetDeviceCheckResult implements Serializable {

    private static final String TAG = "LoginResult";
    private Map<String, Object> resultMap;
    private int state = 3;  //0:设备未注册     1：设备正在等待审批中	2：设备被禁用。	3：正常设备 	4：注册失败。
    private JSONArray jsonArray = new JSONArray();
    private String message = "";
    private String error = "";
    private int doubleValidation = -1;
    private ArrayList<String> requiredFieldList = new ArrayList<String>();

    public GetDeviceCheckResult(String response) {
        try {
            JSONObject jObject = new JSONObject(response);

            if (jObject.has("error")) {
                this.error = jObject.getString("error");
            }
            if (jObject.has("state")) {
                this.state = jObject.getInt("state");
            }
            if (jObject.has("required_fields")) {
                this.jsonArray = jObject.getJSONArray("required_fields");
                for (int i = 0; i < jsonArray.length(); i++) {
                    requiredFieldList.add(jsonArray.getString(i));
                }
            }
            if (jObject.has("message")) {
                this.message = jObject.getString("message");
            }
            if (jObject.has("doubleValidation")) {
                this.doubleValidation = jObject.getInt("doubleValidation");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public int getState() {
        return state;
    }

    public ArrayList<String> getRequiredFieldList() {
        return requiredFieldList;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public int getDoubleValidation() {
        return doubleValidation;
    }

    public void setDoubleValidation(int doubleValidation) {
        this.doubleValidation = doubleValidation;
    }
}
