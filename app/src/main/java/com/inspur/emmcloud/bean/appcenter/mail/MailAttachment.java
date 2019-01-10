package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/1/1.
 */

public class MailAttachment {
    private String id;
    private String name;
    private String data;
    private boolean IsAttachment;


    public MailAttachment(JSONObject obj){
        id= JSONUtils.getString(obj,"id","");
        name= JSONUtils.getString(obj,"name","");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isAttachment() {
        return IsAttachment;
    }

    public void setAttachment(boolean attachment) {
        IsAttachment = attachment;
    }


    public JSONObject getMailAttachment(){
        JSONObject jsonObject = new JSONObject();

        return  jsonObject;
    }

}
