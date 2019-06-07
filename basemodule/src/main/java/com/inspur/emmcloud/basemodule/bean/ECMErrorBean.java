package com.inspur.emmcloud.basemodule.bean;

import org.json.JSONObject;

/**
 * 错误信息实体类</br>
 * 数据格式：</br>
 * {</br>
 * &nbsp&nbsp"code": 41002,</br>
 * &nbsp&nbsp"error": "MEETING_USER_PROFILE_BROKEN",</br>
 * &nbsp&nbsp"error_description": "DEPART_INFO_MISSING"</br>
 * }</br>
 * code：错误代码，唯一</br>
 * error：错误类型，与错误代码是一对多关系</br>
 * error_description：错误描述，服务端对错误的描述</br>
 */
public class ECMErrorBean {

    private int errorCode = -1;
    private String errorType = "";
    private String errorDescription = "";

    public ECMErrorBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("code")) {
                this.errorCode = jsonObject.getInt("code");
            }

            if (jsonObject.has("error")) {
                this.errorType = jsonObject.getString("error");
            }

            if (jsonObject.has("error_description")) {
                this.errorDescription = jsonObject.getString("error_description");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getErrorCode() {
        return errorCode;
    }


    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }


    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

}
