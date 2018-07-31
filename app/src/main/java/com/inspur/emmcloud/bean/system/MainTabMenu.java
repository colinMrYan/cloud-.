package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yufuchang on 2018/7/26.
 */

public class MainTabMenu implements Serializable{
    private String ico;
    private String action;

    public MainTabMenu(JSONObject jsonObject){
        this.ico = JSONUtils.getString(jsonObject,"ico","");
        this.action = JSONUtils.getString(jsonObject,"action","");
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
