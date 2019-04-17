package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/4/6.
 */

public class RemindEvent {
    private int advanceTimeSpan; //提前多久提醒，单位是 秒
    private String remindType;

    public RemindEvent(String json) {
        this(JSONUtils.getJSONObject(json));
    }

    public RemindEvent(JSONObject obj) {
        advanceTimeSpan = JSONUtils.getInt(obj, "advanceTimeSpan", -1);
        remindType = JSONUtils.getString(obj, "remindType", "");
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
}
