package com.inspur.emmcloud.schedule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by libaochao on 2019/4/15.
 */


public class RemindEvent implements Serializable {
    private int advanceTimeSpan = 600;
    private String remindType = "in_app";
    private String name ="";

    public RemindEvent() {
    }
    public RemindEvent(String json) {
        this(JSONUtils.getJSONObject(json));
    }
    public RemindEvent(JSONObject object) {
        advanceTimeSpan = JSONUtils.getInt(object, "advanceTimeSpan", -1);
        remindType = JSONUtils.getString(object, "remindType", "in_app");
    }

    public RemindEvent(String remindType,int advanceTimeSpan,String name) {
        this.remindType = remindType;
        this.advanceTimeSpan = advanceTimeSpan;
        this.name=name;
    }

    public int getAdvanceTimeSpan() {
        return advanceTimeSpan;
    }

    public void setAdvanceTimeSpan(int advanceTimeSpan) {
        this.advanceTimeSpan = advanceTimeSpan;
    }

    public String getRemindType() {
        return remindType;
    }

    public void setRemindType(String remindType) {
        this.remindType = remindType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject toJSONObject(){
        JSONObject object = new JSONObject();
        try {
            if (advanceTimeSpan != -1){
                object.put("advanceTimeSpan",advanceTimeSpan);
            }
                object.put("remindType",remindType);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  object;
    }

}

