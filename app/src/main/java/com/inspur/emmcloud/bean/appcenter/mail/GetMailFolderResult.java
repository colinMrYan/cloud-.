package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/24.
 */

public class GetMailFolderResult {
    private List<MailFolder> mailFolderList = new ArrayList<>();
    public GetMailFolderResult(String response){
        JSONArray array = JSONUtils.getJSONArray(response,new JSONArray());
        for (int i=0;i<array.length();i++){
            JSONObject object = JSONUtils.getJSONObject(array,i,new JSONObject());
            mailFolderList.add(new MailFolder(object,i));
        }
    }

    public List<MailFolder> getMailFolderList() {
        return mailFolderList;
    }

    public void setMailFolderList(List<MailFolder> mailFolderList) {
        this.mailFolderList = mailFolderList;
    }
}
