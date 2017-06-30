package com.inspur.emmcloud.bean;

import android.content.Context;

import com.lidroid.xutils.db.annotation.Table;

import org.json.JSONObject;

@Table(name = "WebPVCollect")
public class PVCollectModel {
    private int id;
    private String functionID = "";
    private String functionType = "";
//    private String userID = "";
//    private String functionName = "";
//    private String tanentID = "";
    private long collectTime = 0L;

    public PVCollectModel() {

    }

    public PVCollectModel(Context context, String functionID, String functionType, String functionName) {
        this.functionID = functionID;
        this.functionType = functionType;
//        userID = ((MyApplication) context.getApplicationContext()).getUid();
//        tanentID = UriUtils.tanent;
//        this.functionName = functionName;
        collectTime = System.currentTimeMillis();
    }

    public String getFunctionID() {
        return functionID;
    }

    public String getFunctionType() {
        return functionType;
    }

//    public String getUserID() {
//        return userID;
//    }
//
//    public String getTanentID() {
//        return tanentID;
//    }
//
//    public long getCollectTime() {
//        return collectTime;
//    }
//
//    public String getFunctionName() {
//        return functionName;
//    }


    public void setFunctionID(String FunctionID) {
        this.functionID = FunctionID;
    }

    public void setFunctionType(String FunctionType) {
        this.functionType = FunctionType;
    }
//    public void setFunctionName(String  FunctionName){
//        this.functionName = FunctionName;
//    }
//
//    public void setUserID(String UserID) {
//        this.userID = UserID;
//    }
//
//    public void setTanentID(String TanentID) {
//        this.tanentID = TanentID;
//    }

    public void setCollectTime(long CollectTime) {
        this.collectTime = CollectTime;
    }

    public JSONObject getObj(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("functionID",functionID);
            obj.put("functionType",functionType);
//            obj.put("userID",userID);
//            obj.put("functionName",functionName);
//            obj.put("tanentID",tanentID);
            obj.put("collectTime",collectTime);
        }catch (Exception e){e.printStackTrace();}
        return  obj;
    }
}
