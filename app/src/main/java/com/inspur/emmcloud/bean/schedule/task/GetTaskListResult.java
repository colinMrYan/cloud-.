package com.inspur.emmcloud.bean.schedule.task;

import org.json.JSONArray;

import java.util.ArrayList;

public class GetTaskListResult {

    private ArrayList<Task> taskList = new ArrayList<Task>();

    public GetTaskListResult(String response) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                Task taskResult = new Task(jsonArray.getJSONObject(i));
                taskList.add(taskResult);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }


    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }


}
