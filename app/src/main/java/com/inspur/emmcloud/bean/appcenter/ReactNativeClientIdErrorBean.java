package com.inspur.emmcloud.bean.appcenter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/2/24.
 */

public class ReactNativeClientIdErrorBean {

    /**
     * code : 78001
     * error : VIEW_CLIENT_ID_NOT_REGISTRIED
     * error_description : ClientId is not registried
     */

    private int code;
    private String error;
    private String error_description;

    public ReactNativeClientIdErrorBean(String reactNativeClientIdErrorBean) {
        try {
            JSONObject jsonCientIdErrorBean = new JSONObject(reactNativeClientIdErrorBean);
            if (jsonCientIdErrorBean.has("code")) {
                this.code = jsonCientIdErrorBean.getInt("code");
            }
            if (jsonCientIdErrorBean.has("error")) {
                this.error = jsonCientIdErrorBean.getString("error");
            }
            if (jsonCientIdErrorBean.has("")) {
                this.error_description = jsonCientIdErrorBean.getString("error_description");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }
}
