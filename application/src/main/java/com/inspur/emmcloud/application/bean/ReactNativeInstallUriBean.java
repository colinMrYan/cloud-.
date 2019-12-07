package com.inspur.emmcloud.application.bean;


import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/3/29.
 */

public class ReactNativeInstallUriBean {
    private String installUri = "";

    public ReactNativeInstallUriBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("install_uri")) {
                this.installUri = jsonObject.getString("install_uri");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getInstallUri() {
        return installUri;
    }

    public void setInstallUri(String installUri) {
        this.installUri = installUri;
    }
}
