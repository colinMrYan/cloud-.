package com.inspur.emmcloud.basemodule.bean;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChannelMessageStates {

    public static final String SENT = "sent";
    public static final String DELIVERED = "delivered";
    public static final String READ = "read";

    public String channel;
    public String message;
    public Map<String, Set<String>> statesMap = new HashMap<>(); //消息的已读未读列表


    public ChannelMessageStates(String contentStr){
        try {
            JSONObject jsonObject = new JSONObject(contentStr);
            channel = jsonObject.optString("channel");
            message = jsonObject.optString("message");
            JSONObject statesJson = new JSONObject(jsonObject.optString("states"));
            if(statesJson.has(SENT)){
                JSONArray sentArray = statesJson.optJSONArray(SENT);
                Set<String> sentList = new HashSet<>();
                for(int i = 0; i < sentArray.length(); i++){
                    sentList.add(sentArray.getString(i));
                }
                statesMap.put(SENT, sentList);
            }
            if(statesJson.has(DELIVERED)){
                JSONArray deliveredArray = statesJson.optJSONArray(DELIVERED);
                Set<String> deliveredList = new HashSet<>();
                for(int i = 0; i < deliveredArray.length(); i++){
                    deliveredList.add(deliveredArray.getString(i));
                }
                statesMap.put(DELIVERED, deliveredList);
            }
            if(statesJson.has(READ)){
                JSONArray readArray = statesJson.optJSONArray(READ);
                Set<String> readList = new HashSet<>();
                for(int i = 0; i < readArray.length(); i++){
                    readList.add(readArray.getString(i));
                }
                statesMap.put(READ, readList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
