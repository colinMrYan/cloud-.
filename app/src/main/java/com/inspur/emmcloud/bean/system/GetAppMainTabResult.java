package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2018/7/12.
 */

public class GetAppMainTabResult {

    private String response;
    private String version;
    private String command;
    private ArrayList<MainTabResult> mainTabResultList = new ArrayList<>();
    public GetAppMainTabResult(String response){
        this.version = JSONUtils.getString(response,"version","");
        this.command = JSONUtils.getString(response,"command","");
        this.response = response;
        JSONArray jsonArray = JSONUtils.getJSONArray(response,"tabs",new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            mainTabResultList.add(new MainTabResult(JSONUtils.getJSONObject(jsonArray,i,new JSONObject())));
        }

    }

    public String getAppTabInfo(){
        return response;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ArrayList<MainTabResult> getMainTabResultList() {
        return mainTabResultList;
    }

    public void setMainTabResultList(ArrayList<MainTabResult> mainTabResultList) {
        this.mainTabResultList = mainTabResultList;
    }
}
