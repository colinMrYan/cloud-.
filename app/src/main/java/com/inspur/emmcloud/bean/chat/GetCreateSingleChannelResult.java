package com.inspur.emmcloud.bean.chat;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetCreateSingleChannelResult {
    private String name = "";
    private String cid = "";
    private String type = "";
    private JSONArray memberArray;

    public GetCreateSingleChannelResult(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            cid = obj.getString("cid");
            type = obj.getString("type");
            memberArray = obj.getJSONArray("channelMembers");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public String getCid() {
        return cid;
    }

    public String getType() {
        return type;
    }

    public String getName(Context context) {
        String myUid = PreferencesUtils.getString(context, "userID");
        String otherUid = "";
        try {
            if (memberArray.getString(0).equals(myUid)) {
                otherUid = memberArray.getString(1);
            } else {
                otherUid = memberArray.getString(0);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String name = ContactUserCacheUtils.getUserName(otherUid);
        return name;
    }
}
