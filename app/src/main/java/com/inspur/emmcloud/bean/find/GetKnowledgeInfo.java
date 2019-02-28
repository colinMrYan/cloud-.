package com.inspur.emmcloud.bean.find;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetKnowledgeInfo {

    private List<KnowledgeInfo> knowLedgeLists = new ArrayList<KnowledgeInfo>();

    public GetKnowledgeInfo(String response) {

        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                knowLedgeLists.add(new KnowledgeInfo(obj));

            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public List<KnowledgeInfo> getKnowLedgeLists() {
        return knowLedgeLists;
    }


}
