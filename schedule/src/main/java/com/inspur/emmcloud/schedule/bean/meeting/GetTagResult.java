package com.inspur.emmcloud.schedule.bean.meeting;

import com.inspur.emmcloud.schedule.bean.task.TaskColorTag;

import org.json.JSONArray;

import java.util.ArrayList;

public class GetTagResult {
    private ArrayList<TaskColorTag> arrayList = new ArrayList<TaskColorTag>();
    private JSONArray jsonArray;
    private String response;

    public GetTagResult(String response) {
        this.response = response;
        try {
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                TaskColorTag tag = new TaskColorTag(jsonArray.getJSONObject(i));
                arrayList.add(tag);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<TaskColorTag> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<TaskColorTag> arrayList) {
        this.arrayList = arrayList;
    }

    public String getResponse() {
        return response;
    }
}
