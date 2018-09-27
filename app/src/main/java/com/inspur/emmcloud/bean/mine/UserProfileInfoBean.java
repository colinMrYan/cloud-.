package com.inspur.emmcloud.bean.mine;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;

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

    private  int showEmpNum =0;      // 初始值不显示
    private  int showTelePhone   =0; // 初始值不显示  为0


    private String globalName="";
    private String userName  ="";
    private String userMail  ="";
    private String userPhone ="";
    private String empNum    ="";
    private String telePhone ="";
    private String id        ="";
    private String orgName   ="";


    private String response;


    public UserProfileInfoBean(String response){
        this.response = response;
        LogUtils.LbcDebug("UserProfileInfoBean"+response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.has("profile")) {
              JSONObject  jsonObjectSubProfile =  jsonObject.getJSONObject("profile");
              String  jsonObjectSubProfileStr= jsonObjectSubProfile.toString();
                if(jsonObjectSubProfile.has("id")){
                    this.id = JSONUtils.getString(jsonObjectSubProfileStr,"id","");
                }
                if(jsonObjectSubProfile.has("userName")){
                    this.userName = JSONUtils.getString(jsonObjectSubProfileStr,"userName","");
                }
                if(jsonObjectSubProfile.has("email")){
                    this.userMail = JSONUtils.getString(jsonObjectSubProfileStr,"email","");
                }
                if(jsonObjectSubProfile.has("mobile")){
                    this.userPhone = JSONUtils.getString(jsonObjectSubProfileStr,"mobile","");
                }
                if(jsonObjectSubProfile.has("globalName")){
                    this.globalName = JSONUtils.getString(jsonObjectSubProfileStr,"globalName","");
                }
                if(jsonObjectSubProfile.has("empNo")){
                    this.empNum = JSONUtils.getString(jsonObjectSubProfileStr,"empNo","");
                }
                if(jsonObjectSubProfile.has("tel")){
                    this.telePhone = JSONUtils.getString(jsonObjectSubProfileStr,"tel","");
                }

                if(jsonObjectSubProfile.has("orgName")){
                    this.orgName = JSONUtils.getString(jsonObjectSubProfileStr,"orgName","");
                }

            }

           if(jsonObject.has("display")) {
               JSONObject  jsonObjectSubDisplaty =  jsonObject.getJSONObject("display");
               String jsonObjectSubDisplatyStr = jsonObjectSubDisplaty.toString();
               if(jsonObjectSubDisplaty.has("showHead")){
                   this.showHead = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showHead",1);
               }
               if(jsonObjectSubDisplaty.has("showUserName")){
                   this.showUserName = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showUserName",1);
               }
               if(jsonObjectSubDisplaty.has("showUserMail")){
                   this.showUserMail = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showUserMail",1);
               }
               if(jsonObjectSubDisplaty.has("showUserPhone")){
                   this.showUserPhone = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showUserPhone",1);
               }
               if(jsonObjectSubDisplaty.has("showEpInfo")){
                   this.showEpInfo = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showEpInfo",1);
               }
               if(jsonObjectSubDisplaty.has("showModifyPsd")){
                   this.showModifyPsd = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showModifyPsd",1);
               }
               if(jsonObjectSubDisplaty.has("showResetPsd")){
                   this.showResetPsd = JSONUtils.getInt(jsonObjectSubDisplatyStr,"showResetPsd",1);
               }
           }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public  String getUserNameStr(){
        return  userName;
    }

    public  String getIDStr() {
        return  id;
    }

    public  String getUserMailStr(){
        return  userMail;
    }

    public  String getTelPhoneStr() {
        return  telePhone;
    }

    public  String getGlobalNameStr() {
        return  globalName;
    }

    public  String getEmpNumStr() {
        return  empNum;
    }

    public  String getMobileStr() {
        return  userPhone;
    }

    public  String getOrgName () {
        return  orgName;
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
