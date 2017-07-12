package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Table;

import org.json.JSONObject;

@Table(name = "WebPVCollect")
public class PVCollectModel {
    private int id;
    private String functionID = "";
    private String functionType = "";
    private long collectTime = 0L;

    public PVCollectModel() {

    }

    public PVCollectModel(String functionID, String functionType) {
        this.functionID = functionID;
        this.functionType = functionType;
        collectTime = System.currentTimeMillis();
    }

    public String getFunctionID() {
        return functionID;
    }

    public String getFunctionType() {
        return functionType;
    }


    public void setFunctionID(String FunctionID) {
        this.functionID = FunctionID;
    }

    public void setFunctionType(String FunctionType) {
        this.functionType = FunctionType;
    }


    public void setCollectTime(long CollectTime) {
        this.collectTime = CollectTime;
    }

    public JSONObject getObj(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("functionID",functionID);
            obj.put("functionType",functionType);
            obj.put("collectTime",collectTime);
        }catch (Exception e){e.printStackTrace();}
        return  obj;
    }
}
