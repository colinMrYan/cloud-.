package com.inspur.emmcloud.bean.chat;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetNewMsgsResult {
    private JSONObject msgsJsonObj;

    public GetNewMsgsResult(String response) {
        try {
            msgsJsonObj = new JSONObject(response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Msg> getNewMsgList(String cid) {
        List<Msg> msgList = new ArrayList<Msg>();
        try {
            if (msgsJsonObj.has(cid)) {
                JSONArray jsonArray = (JSONArray) msgsJsonObj.get(cid);
                for (int i = 0; i < jsonArray.length(); i++) {
                    msgList.add(new Msg(jsonArray.getJSONObject(i)));
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return msgList;
    }
}
