package com.inspur.emmcloud.bean.system;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class AppTabTitleBean {
    /**
     * zh-Hans : 应用
     * zh-Hant : 應用
     * en-US : App
     */

    @SerializedName("zh-Hans")
    private String zhHans = "";
    @SerializedName("zh-Hant")
    private String zhHant = "";
    @SerializedName("en-US")
    private String enUS = "";

    public AppTabTitleBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("zh-Hans")) {
                this.zhHans = jsonObject.getString("zh-Hans");
            }
            if (jsonObject.has("zh-Hant")) {
                this.zhHant = jsonObject.getString("zh-Hant");
            }
            if (jsonObject.has("en-US")) {
                this.enUS = jsonObject.getString("en-US");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getZhHans() {
        return zhHans;
    }

    public void setZhHans(String zhHans) {
        this.zhHans = zhHans;
    }

    public String getZhHant() {
        return zhHant;
    }

    public void setZhHant(String zhHant) {
        this.zhHant = zhHant;
    }

    public String getEnUS() {
        return enUS;
    }

    public void setEnUS(String enUS) {
        this.enUS = enUS;
    }
}
