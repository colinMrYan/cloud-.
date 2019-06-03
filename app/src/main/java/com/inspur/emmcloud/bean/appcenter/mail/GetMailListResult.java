package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/25.
 */

public class GetMailListResult {
    private List<Mail> MailList = new ArrayList<>();

    public GetMailListResult(String response, String folderId) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = JSONUtils.getJSONObject(array, i, new JSONObject());
            MailList.add(new Mail(object, folderId));
        }
    }

    public List<Mail> getMailList() {
        return MailList;
    }

    public void setMailList(List<Mail> mailList) {
        MailList = mailList;
    }
}
