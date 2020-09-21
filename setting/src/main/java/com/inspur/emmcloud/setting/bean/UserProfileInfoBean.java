package com.inspur.emmcloud.setting.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

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
    private int showModifyPsd = 0;
    private int showResetPsd = 0;

    private int showEmpNum = 0;      // 初始值不显示
    private int showTelePhone = 0; // 初始值不显示  为0


    private String globalName = "";
    private String orgId = "";
    private String userName = "";
    private String userMail = "";
    private String userPhone = "";
    private String empNum = "";
    private String telePhone = "";
    private String id = "";
    private String orgName = "";


    private String response;

    public UserProfileInfoBean() {

    }

    public UserProfileInfoBean(String response) {
        this.response = response;
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("profile")) {
                JSONObject jsonObjectSubProfile = jsonObject.getJSONObject("profile");
                String jsonObjectSubProfileStr = jsonObjectSubProfile.toString();
                this.id = JSONUtils.getString(jsonObjectSubProfileStr, "id", "");
                this.userName = JSONUtils.getString(jsonObjectSubProfileStr, "userName", "");
                this.userMail = JSONUtils.getString(jsonObjectSubProfileStr, "email", "");
                this.userPhone = JSONUtils.getString(jsonObjectSubProfileStr, "mobile", "");
                this.globalName = JSONUtils.getString(jsonObjectSubProfileStr, "globalName", "");
                this.orgId = JSONUtils.getString(jsonObjectSubProfileStr, "orgId", "");
                this.empNum = JSONUtils.getString(jsonObjectSubProfileStr, "empNo", "");
                this.telePhone = JSONUtils.getString(jsonObjectSubProfileStr, "tel", "");
                this.orgName = JSONUtils.getString(jsonObjectSubProfileStr, "orgName", "");
            }

            if (jsonObject.has("display")) {
                JSONObject jsonObjectSubDisplaty = jsonObject.getJSONObject("display");
                String jsonObjectSubDisplatyStr = jsonObjectSubDisplaty.toString();
                this.showHead = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showHead", 1);
                this.showUserName = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showUserName", 1);
                this.showUserMail = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showUserMail", 1);
                this.showUserPhone = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showUserPhone", 1);
                this.showEpInfo = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showEpInfo", 1);
                this.showModifyPsd = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showModifyPsd", 0);
                this.showResetPsd = JSONUtils.getInt(jsonObjectSubDisplatyStr, "showResetPsd", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getGlobalName() {
        return globalName;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getEmpNum() {
        return empNum;
    }

    public String getTelePhone() {
        return telePhone;
    }

    public String getId() {
        return id;
    }

    public String getOrgName() {
        return orgName;
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

    public String getResponse() {
        return response;
    }

    public int getShowEmpNum() {
        return showEmpNum;
    }

    public void setShowEmpNum(int showEmpNum) {
        this.showEmpNum = showEmpNum;
    }
}
