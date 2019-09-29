package com.inspur.emmcloud.basemodule.bean;

/**
 * Created by chenmch on 2018/9/19.
 */

public class SimpleEventMessage {
    private String action;
    private Object messageObj;
    private Object extraObj;
    public SimpleEventMessage(String action, Object messageObj) {
        this.action = action;
        this.messageObj = messageObj;
    }

    public SimpleEventMessage(String action) {
        this.action = action;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getMessageObj() {
        return messageObj;
    }

    public void setMessageObj(Object messageObj) {
        this.messageObj = messageObj;
    }

    public Object getExtraObj() {
        return extraObj;
    }

    public void setExtraObj(Object extraObj) {
        this.extraObj = extraObj;
    }
}
