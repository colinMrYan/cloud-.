package com.inspur.emmcloud.bean.schedule.calendar;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/7/11.
 */

public class GetScheduleBasicDataResult {
    private boolean isEnableExchange = false;
    private List<Holiday> holidayList = new ArrayList<>();
    private String command;
    private String version;

    public GetScheduleBasicDataResult(String response) {
        JSONObject object = JSONUtils.getJSONObject(response);
        command = JSONUtils.getString(object, "command", "STANDBY");
        if (command.equals("FORWARD")) {
            JSONArray array = JSONUtils.getJSONArray(object, "holiday", new JSONArray());
            for (int i = 0; i < array.length(); i++) {
                holidayList.add(new Holiday(JSONUtils.getJSONObject(array, i, new JSONObject())));
            }
            isEnableExchange = JSONUtils.getBoolean(object, "enableExchange", false);
        }
        this.version = JSONUtils.getString(object, "version", "0");
    }

    public boolean isEnableExchange() {
        return isEnableExchange;
    }

    public void setEnableExchange(boolean enableExchange) {
        isEnableExchange = enableExchange;
    }

    public List<Holiday> getHolidayList() {
        return holidayList;
    }

    public void setHolidayList(List<Holiday> holidayList) {
        this.holidayList = holidayList;
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
