package com.inspur.emmcloud.bean.chat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 获取所有robot
 */
public class GetAllRobotsResult {

    private ArrayList<Robot> robotList = new ArrayList<Robot>();

    public GetAllRobotsResult(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                robotList.add(new Robot(jsonObject.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Robot> getRobotList() {
        return robotList;
    }
}