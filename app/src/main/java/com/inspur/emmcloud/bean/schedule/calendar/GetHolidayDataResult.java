package com.inspur.emmcloud.bean.schedule.calendar;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/5/2.
 */

public class GetHolidayDataResult {
    private List<Holiday> holidayList = new ArrayList<>();
    private int year;
    public GetHolidayDataResult(String response,int year){
        JSONArray array = JSONUtils.getJSONArray(response,new JSONArray());
        for (int i=0;i<array.length();i++){
            holidayList.add(new Holiday(JSONUtils.getJSONObject(array,i,new JSONObject())));
        }
        this.year = year;

    }

    public List<Holiday> getHolidayList() {
        return holidayList;
    }

    public void setHolidayList(List<Holiday> holidayList) {
        this.holidayList = holidayList;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
