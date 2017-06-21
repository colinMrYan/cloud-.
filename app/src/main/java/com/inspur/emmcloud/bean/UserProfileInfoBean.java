package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/5/12.
 */

public class UserProfileInfoBean {

    /**
     * showHead : 1
     * showUserName : 1
     * showModifyPsd : 0
     */

    private int showHead = 1;
    private int showUserName = 1;
    private int showUserMail = 1;
    private int showUserPhone = 1;
    private int showEpInfo = 1;
    private int showModifyPsd = 1;
    private int showResetPsd = 1;
    private String response;

    public UserProfileInfoBean(String response){
        this.response = response;
        try {
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.has("showHead")){
                this.showHead = JSONUtils.getInt(response,"showHead",1);
            }
            if(jsonObject.has("showUserName")){
                this.showUserName = JSONUtils.getInt(response,"showUserName",1);
            }
            if(jsonObject.has("showUserMail")){
                this.showUserMail = JSONUtils.getInt(response,"showUserMail",1);
            }
            if(jsonObject.has("showUserPhone")){
                this.showUserPhone = JSONUtils.getInt(response,"showUserPhone",1);
            }
            if(jsonObject.has("showEpInfo")){
                this.showEpInfo = JSONUtils.getInt(response,"showEpInfo",1);
            }
            if(jsonObject.has("showModifyPsd")){
                this.showModifyPsd = JSONUtils.getInt(response,"showModifyPsd",1);
            }
            if(jsonObject.has("showResetPsd")){
                this.showResetPsd = JSONUtils.getInt(response,"showResetPsd",1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getShowHead() {
        return showHead;
    }

    public void setShowHead(int showHead) {
        this.showHead = showHead;
    }

    public int getShowUserName() {
        return showUserName;
    }

    public void setShowUserName(int showUserName) {
        this.showUserName = showUserName;
    }

    public int getShowModifyPsd() {
        return showModifyPsd;
    }

    public void setShowModifyPsd(int showModifyPsd) {
        this.showModifyPsd = showModifyPsd;
    }

    public int getShowUserMail() {
        return showUserMail;
    }

    public void setShowUserMail(int showUserMail) {
        this.showUserMail = showUserMail;
    }

    public int getShowUserPhone() {
        return showUserPhone;
    }

    public void setShowUserPhone(int showUserPhone) {
        this.showUserPhone = showUserPhone;
    }

    public int getShowEpInfo() {
        return showEpInfo;
    }

    public void setShowEpInfo(int showEpInfo) {
        this.showEpInfo = showEpInfo;
    }

    public int getShowResetPsd() {
        return showResetPsd;
    }

    public void setShowResetPsd(int showResetPsd) {
        this.showResetPsd = showResetPsd;
    }

    public String getResponse(){
        return response;
    }
}
