package com.inspur.emmcloud.bean.work;

import java.util.ArrayList;

import org.json.JSONArray;

public class GetTaskListResult {

	private ArrayList<TaskResult> taskList = new ArrayList<TaskResult>();
	public GetTaskListResult(String response){
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				TaskResult taskResult = new TaskResult(jsonArray.getJSONObject(i));
				taskList.add(taskResult);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		
	}
	public ArrayList<TaskResult> getTaskList() {
		return taskList;
	}
	public void setTaskList(ArrayList<TaskResult> taskList) {
		this.taskList = taskList;
	}
	
	
}
