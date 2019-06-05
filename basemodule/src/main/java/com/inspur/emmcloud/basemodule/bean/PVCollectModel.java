package com.inspur.emmcloud.basemodule.bean;


import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "WebPVCollect")
public class PVCollectModel {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "functionID")
    private String functionID = "";
    @Column(name = "functionType")
    private String functionType = "";
    @Column(name = "collectTime")
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

    public void setFunctionID(String FunctionID) {
        this.functionID = FunctionID;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String FunctionType) {
        this.functionType = FunctionType;
    }


    public void setCollectTime(long CollectTime) {
        this.collectTime = CollectTime;
    }

    public JSONObject getObj() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("functionID", functionID);
            obj.put("functionType", functionType);
            obj.put("collectTime", collectTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
